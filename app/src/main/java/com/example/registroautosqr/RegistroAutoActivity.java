package com.example.registroautosqr;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class RegistroAutoActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private Spinner spinnerPersona;
    private HashMap<String, String> personasMap;
    private ImageView imageViewQR;
    private Bitmap qrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_auto);

        dbManager = new DatabaseManager(this);
        imageViewQR = findViewById(R.id.imageViewQR);

        EditText edtPlaca = findViewById(R.id.edtPlaca);
        spinnerPersona = findViewById(R.id.spinnerPersona);
        Button btnGuardarAuto = findViewById(R.id.btnGuardarAuto);
        Button btnCompartirQR = findViewById(R.id.btnCompartirQR);
        cargarPersonasEnSpinner();
        btnGuardarAuto.setOnClickListener(new View.OnClickListener() {

            // Método para insertar el auto
            private boolean insertarAutoRemoto(String placa, int id_persona) {
                Connection connection = null;
                PreparedStatement stmt = null;

                try {
                    // Establecer conexión
                    connection = new ConexionSQL().conectionclass();

                    // Depurar los datos que se van a insertar
                    Log.d("DB", "Placa: " + placa + ", Id_persona: " + id_persona);

                    // Consulta SQL corregida (sin el modelo)
                    String query = "INSERT INTO auto (placa, id_persona) VALUES (?, ?)";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, placa);
                    stmt.setInt(2, id_persona);

                    // Ejecutar la consulta
                    int rowsInserted = stmt.executeUpdate();

                    // Verificar el resultado
                    if (rowsInserted > 0) {
                        Log.d("DB", "Inserción exitosa.");
                        return true;
                    } else {
                        Log.d("DB", "Inserción fallida. No se insertaron filas.");
                        return false;
                    }
                } catch (SQLException e) {
                    // Registrar el error detallado
                    Log.e("DB", "Error al insertar auto", e);
                    return false;
                } finally {
                    try {
                        if (stmt != null) stmt.close();
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        Log.e("DB", "Error al cerrar recursos", e);
                    }
                }
            }




            @Override
            public void onClick(View view) {
                String placa = edtPlaca.getText().toString().trim();
                String nombrePersona = spinnerPersona.getSelectedItem().toString();
                String idPersona = personasMap.get(nombrePersona); // Cambiado a `id_persona`


                // Convertir `id_persona` a entero antes de pasar al método
                try {
                    int idPersonaInt = Integer.parseInt(idPersona);

                    // Llamar al método para insertar el auto
                    if (insertarAutoRemoto(placa, idPersonaInt)) {
                        qrBitmap = QRUtils.generarQRCode(placa);
                        if (qrBitmap != null) {
                            imageViewQR.setImageBitmap(qrBitmap);
                            Toast.makeText(RegistroAutoActivity.this, "Auto registrado y QR generado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroAutoActivity.this, "Error al generar el QR", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistroAutoActivity.this, "Error al registrar auto", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(RegistroAutoActivity.this, "ID de Persona inválido", Toast.LENGTH_SHORT).show();
                    Log.e("DB", "Error de formato en ID Persona", e);
                }

                // Limpiar campos
                edtPlaca.setText("");
            }
        });




        btnCompartirQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qrBitmap != null) {
                    compartirQR();
                } else {
                    Toast.makeText(RegistroAutoActivity.this, "Primero genera un QR", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarPersonasEnSpinner() {
        ArrayList<String> nombresPersonas = new ArrayList<>();
        personasMap = new HashMap<>();

        // Consulta a la base de datos remota
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            // Establecer la conexión con la base de datos remota
            connection = new ConexionSQL().conectionclass();

            // Consultar personas de la base de datos remota
            String query = "SELECT nombre, id_persona FROM persona";
            stmt = connection.prepareStatement(query);
            resultSet = stmt.executeQuery();

            // Verificar si se encontraron personas
            while (resultSet.next()) {
                String nombre = resultSet.getString("nombre");
                String idPersona = resultSet.getString("id_persona");

                // Agregar nombre al ArrayList y relacionarlo con el DNI en el HashMap
                nombresPersonas.add(nombre);
                personasMap.put(nombre, idPersona);
            }

            // Configurar el Spinner con los nombres de las personas
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresPersonas);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPersona.setAdapter(adapter);

        } catch (SQLException e) {
            Log.e("DB", "Error al cargar personas en el Spinner", e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e("DB", "Error al cerrar recursos", e);
            }
        }
    }


    @SuppressLint("NewApi")
    private void compartirQR() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Guardar el archivo en almacenamiento temporal
        String path = getExternalCacheDir() + "/qr_temp.png";
        Uri fileUri = Uri.parse(path);
        try {
            // Guardar el archivo temporalmente
            java.nio.file.Files.write(java.nio.file.Paths.get(path), byteArray);

            // Guardar la imagen en la galería
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "qr_Registro_UPSJB.png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream imageOutStream = getContentResolver().openOutputStream(imageUri);
            imageOutStream.write(byteArray);
            imageOutStream.close();

            // Crear el Intent para compartir la imagen
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, "Aquí tienes el código QR de tu Vehiculo.");

            // Mostrar el menú de compartir con todas las apps disponibles
            Intent shareIntent = Intent.createChooser(intent, "Compartir código QR");
            startActivity(shareIntent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al compartir el QR", Toast.LENGTH_SHORT).show();
        }
    }

}

