package com.example.registroautosqr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarIngresoActivity extends AppCompatActivity {


    private DatabaseManager dbManager;
    private Button btnEscanearQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_ingreso);

        dbManager = new DatabaseManager(this);
        btnEscanearQR = findViewById(R.id.btnEscanearQR);

        btnEscanearQR.setOnClickListener(view -> {
            Log.d("EscanearQR", "Botón presionado, iniciando escaneo...");
            new IntentIntegrator(RegistrarIngresoActivity.this).initiateScan();
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Obtener la placa del auto escaneado
                String placa = result.getContents();

                // Verificar si la placa está registrada en la base de datos
                if (esAutoRegistrado(placa)) {
                    // Registrar la hora de ingreso
                    String horaIngreso = obtenerHoraActual();
                    boolean ingresoExitoso = registrarIngreso(placa, horaIngreso);

                    if (ingresoExitoso) {
                        Toast.makeText(this, "Ingreso registrado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al registrar el ingreso", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Auto no registrado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se escaneó ningún QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Verificar si el auto está registrado en la base de datos
    private boolean esAutoRegistrado(String placa) {
        Cursor cursor = dbManager.obtenerAutoPorPlaca(placa);
        return cursor != null && cursor.moveToFirst();
    }

    // Obtener la hora actual en formato adecuado
    private String obtenerHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Registrar el ingreso en la base de datos
    @SuppressLint("Range")
    private boolean registrarIngreso(String placa, String horaIngreso) {
        // Obtener el id del auto usando la placa
        Cursor cursor = dbManager.obtenerAutoPorPlaca(placa);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int idAuto = cursor.getInt(cursor.getColumnIndex("id_auto"));
            return dbManager.registrarIngreso(idAuto, horaIngreso);
        }
        return false;
    }
}