package com.example.registroautosqr;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RegistroPersonaActivity extends AppCompatActivity {

    private Spinner spinnerCarrera;
    private ArrayList<String> listaCarreras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_persona);

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

                if (registrarPersona(nombre, apellido, carrera, ciclo, dni, telefono)) {
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
        listaCarreras = obtenerCarrerasRemotas(); // Obtén la lista de carreras desde la base de datos remota

        // Configurar el adapter del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaCarreras);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarrera.setAdapter(adapter);
    }

    private ArrayList<String> obtenerCarrerasRemotas() {
        ArrayList<String> carreras = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            connection = new ConexionSQL().conectionclass();
            String query = "SELECT nombre_carrera FROM carrera";
            stmt = connection.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                carreras.add(resultSet.getString("nombre_carrera"));
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Error al cargar carreras", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return carreras;
    }

    private boolean registrarPersona(String nombre, String apellido, String carrera, String ciclo, String dni, String telefono) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = new ConexionSQL().conectionclass();
            String query = "INSERT INTO persona (nombre, apellido, carrera, ciclo, dni, telefono) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, carrera);
            stmt.setString(4, ciclo);
            stmt.setString(5, dni);
            stmt.setString(6, telefono);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            Toast.makeText(this, "Error al registrar persona", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
