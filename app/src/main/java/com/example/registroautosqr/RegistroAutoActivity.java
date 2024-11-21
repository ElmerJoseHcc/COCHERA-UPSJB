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
        EditText edtModelo = findViewById(R.id.edtModelo);
        spinnerPersona = findViewById(R.id.spinnerPersona);
        Button btnGuardarAuto = findViewById(R.id.btnGuardarAuto);
        Button btnCompartirQR = findViewById(R.id.btnCompartirQR);

        cargarPersonasEnSpinner();

        btnGuardarAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String placa = edtPlaca.getText().toString().trim();
                String modelo = edtModelo.getText().toString().trim();
                String nombrePersona = spinnerPersona.getSelectedItem().toString();
                String dniPersona = personasMap.get(nombrePersona);

                // Validar que todos los campos estén completos
                if (placa.isEmpty() || modelo.isEmpty() || dniPersona == null) {
                    Toast.makeText(RegistroAutoActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Registrar el auto solo si los campos están completos
                long resultado = dbManager.insertarAuto(placa, modelo, Integer.parseInt(dniPersona));
                if (resultado > 0) {
                    // Generar y mostrar el código QR solo si la inserción fue exitosa
                    qrBitmap = QRUtils.generarQRCode(placa);
                    if (qrBitmap != null) {
                        imageViewQR.setImageBitmap(qrBitmap);
                        Toast.makeText(RegistroAutoActivity.this, "Auto registrado y QR generado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistroAutoActivity.this, "Error al generar el QR", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Mostrar un mensaje de error solo si la inserción no fue exitosa
                    Toast.makeText(RegistroAutoActivity.this, "Error al registrar auto", Toast.LENGTH_SHORT).show();
                }

                // Limpiar los campos
                edtPlaca.setText("");
                edtModelo.setText("");
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

        // Consultar personas de la base de datos
        Cursor cursor = dbManager.obtenerPersonas();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                @SuppressLint("Range") String dni = cursor.getString(cursor.getColumnIndex("dni"));
                nombresPersonas.add(nombre);
                personasMap.put(nombre, dni); // Relacionar nombre con DNI
            } while (cursor.moveToNext());
        }

        // Configurar el Spinner con los nombres de las personas
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresPersonas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPersona.setAdapter(adapter);
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

