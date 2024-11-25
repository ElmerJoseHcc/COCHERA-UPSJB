package com.example.registroautosqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar los botones de la interfaz
        configurarBotones();
    }

    // Método para configurar las acciones de los botones
    private void configurarBotones() {
        Button btnRegistroPersona = findViewById(R.id.btnRegistroPersona);
        btnRegistroPersona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, RegistroPersonaActivity.class);
                startActivity(intent);
            }
        });

        Button btnRegistroAuto = findViewById(R.id.btnRegistroAuto);
        btnRegistroAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, RegistroAutoActivity.class);
                startActivity(intent);
            }
        });

        Button btnRegistroQR = findViewById(R.id.btnRegistroQR);
        btnRegistroQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, RegistrarIngresoActivity.class);
                startActivity(intent);
            }
        });

        Button btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);
        btnRegistrarSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, RegistrarSalidaActivity.class);
                startActivity(intent);
            }
        });

        Button btnVerRegistros = findViewById(R.id.btnVerRegistros);

        btnVerRegistros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, VerRegistrosActivity.class);
                startActivity(intent);
            }
        });

    }
}
