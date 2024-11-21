package com.example.registroautosqr;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarSalidaActivity extends AppCompatActivity {

    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_salida);

        dbManager = new DatabaseManager(this);

        Button btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);

        btnRegistrarSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Escanear el QR
                escanearQR();
            }
        });
    }

    private void escanearQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el QR del auto");
        integrator.setCameraId(0);  // Usar la cámara trasera por defecto
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Obtener la placa del auto escaneado
                String placa = result.getContents();

                // Verificar si la placa está registrada y si el auto ya está ingresado
                if (esAutoIngresado(placa)) {
                    String horaSalida = obtenerHoraActual();
                    boolean salidaExitoso = registrarSalida(placa, horaSalida);

                    if (salidaExitoso) {
                        Toast.makeText(this, "Salida registrada exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al registrar la salida", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "El auto no está registrado o no ha ingresado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se escaneó ningún QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean esAutoIngresado(String placa) {
        // Verificamos si el auto está registrado como ingresado
        Cursor cursor = dbManager.obtenerAutoPorPlaca1(placa);
        if (cursor != null && cursor.moveToFirst()) {
            String estado = cursor.getString(cursor.getColumnIndex("estado"));
            return "ingresado".equals(estado);
        }
        return false;
    }

    private String obtenerHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean registrarSalida(String placa, String horaSalida) {
        // Llamamos al método de DatabaseManager para registrar la salida
        return dbManager.registrarSalida(placa, horaSalida);
    }
}