package com.example.registroautosqr;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegistroPersonaActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private Spinner spinnerCarrera;
    private ArrayList<String> listaCarreras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_persona);

        dbManager = new DatabaseManager(this);

        EditText edtNombre = findViewById(R.id.edtNombre);
        EditText edtApellido = findViewById(R.id.edtApellido);
        EditText edtCiclo = findViewById(R.id.edtCiclo);
        EditText edtDNI = findViewById(R.id.edtDNI);
        EditText edtTelefono = findViewById(R.id.edtTelefono);
        spinnerCarrera = findViewById(R.id.spinnerCarrera);
        Button btnGuardar = findViewById(R.id.btnGuardarPersona);

        // Cargar las carreras en el Spinner
        cargarCarreras();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = edtNombre.getText().toString().trim();
                String apellido = edtApellido.getText().toString().trim();
                String carrera = spinnerCarrera.getSelectedItem().toString(); // Obtener carrera seleccionada
                String ciclo = edtCiclo.getText().toString().trim();
                String dni = edtDNI.getText().toString().trim();
                String telefono = edtTelefono.getText().toString().trim();

                if (nombre.isEmpty() || dni.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || ciclo.isEmpty()) {
                    Toast.makeText(RegistroPersonaActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                long resultado = dbManager.insertarPersona(nombre, apellido, carrera, ciclo, dni, telefono);
                if (resultado > 0) {
                    Toast.makeText(RegistroPersonaActivity.this, "Persona registrada exitosamente", Toast.LENGTH_SHORT).show();
                    edtNombre.setText("");
                    edtApellido.setText("");
                    edtCiclo.setText("");
                    edtDNI.setText("");
                    edtTelefono.setText("");
                    spinnerCarrera.setSelection(0); // Volver al primer elemento
                } else {
                    Toast.makeText(RegistroPersonaActivity.this, "Error al registrar persona", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarCarreras() {
        listaCarreras = dbManager.obtenerCarreras(); // Obtén la lista de carreras usando DatabaseManager

        // Configurar el adapter del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaCarreras);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarrera.setAdapter(adapter);
    }

}
