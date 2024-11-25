package com.example.registroautosqr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarSalidaActivity extends AppCompatActivity {

    private Button btnRegistrarSalida;
    private Button btnSubirImagen2;

    private static final int PICK_IMAGE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_salida);

        btnSubirImagen2 = findViewById(R.id.btnSubirImagen2);
        btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);

        btnRegistrarSalida.setOnClickListener(view -> {
            Log.d("EscanearQR", "Botón presionado, iniciando escaneo...");
            new IntentIntegrator(RegistrarSalidaActivity.this).initiateScan();
        });

        // Configurar el botón de subir imagen
        btnSubirImagen2.setOnClickListener(view -> {
            // Usar el Storage Access Framework para acceder a la galería
            abrirGaleria();
        });
    }

    // Abrir la galería usando el Storage Access Framework
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");  // Solo imágenes
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);  // Permitir solo una imagen
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Manejar el resultado de la selección de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();  // La URI de la imagen seleccionada
            // Aquí puedes hacer lo que desees con la imagen, como escanear el QR o subirla
            Log.d("Galería", "Imagen seleccionada: " + imageUri.toString());
            procesarImagenQR(imageUri);  // Procesar la imagen seleccionada para escanear el QR
        }

        // Resultado del escaneo del QR
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                String contenidoQR = result.getContents();
                procesarQR(contenidoQR);  // Procesar el QR escaneado
            }
        }
    }

    // Procesar la imagen seleccionada para escanear el QR
    private void procesarImagenQR(Uri uriImagen) {
        try {
            // Cargar la imagen seleccionada
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImagen);

            // Usar ZXing para procesar el QR
            com.google.zxing.Reader lectorQR = new com.google.zxing.qrcode.QRCodeReader();
            int ancho = bitmap.getWidth();
            int alto = bitmap.getHeight();

            int[] pixels = new int[ancho * alto];
            bitmap.getPixels(pixels, 0, ancho, 0, 0, ancho, alto);

            com.google.zxing.LuminanceSource source = new com.google.zxing.RGBLuminanceSource(ancho, alto, pixels);
            com.google.zxing.BinaryBitmap binaryBitmap = new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
            com.google.zxing.Result resultado = lectorQR.decode(binaryBitmap);

            // Resultado del QR
            String contenidoQR = resultado.getText();
            Toast.makeText(this, "Placa: " + contenidoQR, Toast.LENGTH_LONG).show();

            // Separar solo la placa desde el QR (asumiendo que solo contiene la placa)
            String placa = contenidoQR.trim();  // La placa es el único dato que esperas

            // Verificar si el auto está registrado y ha ingresado
            if (esAutoIngresado(placa)) {
                // Obtener la hora de salida
                String horaSalida = obtenerHoraActual();

                // Registrar la salida en la base de datos remota
                if (registrarSalida(placa, horaSalida)) {
                    Toast.makeText(this, "Salida registrada correctamente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error, el vehículo no ha ingresado al estacionamiento", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "El auto no está registrado o no ha ingresado", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("EscaneoQR", "Error al escanear QR desde la imagen", e);
            Toast.makeText(this, "No se pudo procesar el QR", Toast.LENGTH_SHORT).show();
        }
    }

    // Procesar el QR escaneado
    private void procesarQR(String contenidoQR) {
        // Separar solo la placa desde el QR (asumiendo que solo contiene la placa)
        String placa = contenidoQR.trim();

        // Verificar si el auto está registrado y ha ingresado
        if (esAutoIngresado(placa)) {
            // Obtener la hora de salida
            String horaSalida = obtenerHoraActual();

            // Registrar la salida en la base de datos remota
            if (registrarSalida(placa, horaSalida)) {
                Toast.makeText(this, "Salida registrada correctamente", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error, el vehículo no ha ingresado al estacionamiento", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "El auto no está registrado o no ha ingresado", Toast.LENGTH_SHORT).show();
        }
    }

    // Verificar si el auto está registrado y su estado es "INGRESADO"
    private boolean esAutoIngresado(String placa) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            // Establecer la conexión a la base de datos remota
            connection = new ConexionSQL().conectionclass();

            // Consulta para verificar si el auto está registrado y tiene el estado 'INGRESADO'
            String query = "SELECT id_auto FROM auto WHERE placa = ?";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, placa);

            // Ejecutar la consulta
            resultSet = stmt.executeQuery();

            // Verificar si el resultado contiene al menos una fila (auto registrado)
            return resultSet.next();
        } catch (SQLException e) {
            Log.e("DB", "Error al verificar si el auto está registrado", e);
            return false;
        } finally {
            // Asegúrate de cerrar los recursos
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e("DB", "Error al cerrar los recursos", e);
            }
        }
    }

    // Obtener la hora actual en formato adecuado
    private String obtenerHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Registrar la salida en la base de datos
    private boolean registrarSalida(String placa, String horaSalida) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            // Obtener el id del auto usando la placa desde la base de datos remota
            connection = new ConexionSQL().conectionclass();

            String query = "SELECT id_auto FROM auto WHERE placa = ?";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, placa);

            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int idAuto = resultSet.getInt("id_auto");

                // Registrar la salida en la base de datos remota (solo placa y hora de salida)
                return registrarSalidaRemota(idAuto, horaSalida, connection);
            }
        } catch (SQLException e) {
            Log.e("DB", "Error al registrar salida, el auto no ha ingresado", e);
        } finally {
            // Asegúrate de cerrar los recursos
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Log.e("DB", "Error al cerrar los recursos", e);
            }
        }
        return false;
    }

    private boolean registrarSalidaRemota(int idAuto, String horaSalida, Connection connection) {
        PreparedStatement stmt = null;
        try {
            String query = "UPDATE registros SET hora_salida = ?, estado = 'SALIDO' WHERE id_auto = ? AND estado = 'INGRESADO'";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, horaSalida);  // hora_salida
            stmt.setInt(2, idAuto);  // id_auto

            // Ejecutar la actualización en la base de datos remota
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.e("DB", "Error al registrar salida remoto", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                Log.e("DB", "Error al cerrar el PreparedStatement", e);
            }
        }
        return false;
    }
}
