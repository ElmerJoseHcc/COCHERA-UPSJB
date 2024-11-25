package com.example.registroautosqr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;

public class RegistroQRActivity extends AppCompatActivity {

    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_qractivity);

        dbHandler = new DatabaseHandler();

        Button btnScanQR = findViewById(R.id.btnRegistroQR);
        btnScanQR.setOnClickListener(view -> iniciarEscaneoQR());
    }

    private void iniciarEscaneoQR() {
        IntentIntegrator integrator = new IntentIntegrator(RegistroQRActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR del auto");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                buscarAuto(result.getContents());
            } else {
                Toast.makeText(this, "No se pudo escanear el QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscarAuto(String placa) {
        new Thread(() -> {
            try {
                String idAuto = dbHandler.buscarAuto(placa);

                if (idAuto != null) {
                    registrarIngresoOSalida(idAuto);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Auto no encontrado en la base de datos", Toast.LENGTH_SHORT).show());
                }
            } catch (SQLException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void registrarIngresoOSalida(String idAuto) {
        new Thread(() -> {
            try {
                String horaSalida = dbHandler.consultarUltimaHoraSalida(idAuto);

                if (horaSalida == null) {
                    String horaActual = dbHandler.obtenerHoraActual();
                    dbHandler.actualizarHoraSalida(idAuto, horaActual);
                    runOnUiThread(() -> Toast.makeText(this, "Hora de salida registrada: " + horaActual, Toast.LENGTH_SHORT).show());
                } else {
                    String horaActual = dbHandler.obtenerHoraActual();
                    dbHandler.registrarNuevoIngreso(idAuto, horaActual);
                    runOnUiThread(() -> Toast.makeText(this, "Ingreso registrado: " + horaActual, Toast.LENGTH_SHORT).show());
                }
            } catch (SQLException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al consultar o registrar datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
