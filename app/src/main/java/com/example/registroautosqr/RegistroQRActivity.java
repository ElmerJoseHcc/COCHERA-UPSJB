package com.example.registroautosqr;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistroQRActivity extends AppCompatActivity {

    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_qractivity);

        dbManager = new DatabaseManager(this);

        Button btnScanQR = findViewById(R.id.btnRegistroQR);
        btnScanQR.setOnClickListener(view -> iniciarEscaneoQR());
    }

    private void iniciarEscaneoQR() {
        // Iniciar el escaneo de QR
        IntentIntegrator integrator = new IntentIntegrator(RegistroQRActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR del auto");
        integrator.setCameraId(0); // Cámara trasera
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Obtener el resultado del escaneo
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String qrContent = result.getContents(); // Contenido del QR (Placa)
                buscarAuto(qrContent);
            } else {
                Toast.makeText(this, "No se pudo escanear el QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscarAuto(String qrContent) {
        // Buscar auto por placa en la base de datos
        Cursor cursor = dbManager.buscarAutoPorPlaca(qrContent);

        if (cursor != null && cursor.moveToFirst()) {
            String idAuto = cursor.getString(cursor.getColumnIndex("id_auto"));
            registrarIngresoOSalida(idAuto);
        } else {
            Toast.makeText(this, "Auto no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void registrarIngresoOSalida(String idAuto) {
        // Verificar si hay un registro de ingreso pendiente de salida
        Cursor cursor = dbManager.obtenerUltimoRegistro(idAuto);

        if (cursor != null && cursor.moveToFirst()) {
            String horaSalida = cursor.getString(cursor.getColumnIndex("hora_salida"));

            if (horaSalida == null) {
                // Registrar la hora de salida
                String horaActual = obtenerHoraActual();
                dbManager.actualizarHoraSalida(idAuto, horaActual);
                Toast.makeText(this, "Hora de salida registrada: " + horaActual, Toast.LENGTH_SHORT).show();
            } else {
                // Registrar un nuevo ingreso
                registrarNuevoIngreso(idAuto);
            }
        } else {
            // Registrar un nuevo ingreso si no hay registros previos
            registrarNuevoIngreso(idAuto);
        }
    }

    private void registrarNuevoIngreso(String idAuto) {
        // Registrar un nuevo ingreso
        String horaActual = obtenerHoraActual();
        long resultado = dbManager.insertarRegistro(idAuto, horaActual);

        if (resultado > 0) {
            Toast.makeText(this, "Hora de ingreso registrada: " + horaActual, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al registrar hora de ingreso", Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerHoraActual() {
        // Obtener la hora actual en formato adecuado
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
