package com.example.registroautosqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
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

public class RegistrarIngresoActivity extends AppCompatActivity {

    private Button btnEscanearQR;
    private Button btnSubirImagen;

    private static final int PICK_IMAGE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_ingreso);

        btnSubirImagen = findViewById(R.id.btnSubirImagen);
        btnEscanearQR = findViewById(R.id.btnEscanearQR);

        btnEscanearQR.setOnClickListener(view -> {
            Log.d("EscanearQR", "Botón presionado, iniciando escaneo...");
            new IntentIntegrator(RegistrarIngresoActivity.this).initiateScan();
        });

        // Configurar el botón de subir imagen
        btnSubirImagen.setOnClickListener(view -> {
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

            // Verificar si el auto está registrado
            if (esAutoRegistrado(placa)) {
                // Obtener la hora de ingreso
                String horaIngreso = obtenerHoraActual();

                // Registrar el ingreso en la base de datos remota
                if (registrarIngreso(placa, horaIngreso)) {
                    Toast.makeText(this, "Ingreso registrado correctamente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error al registrar el ingreso", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "El auto no está registrado", Toast.LENGTH_SHORT).show();
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

        // Verificar si el auto está registrado
        if (esAutoRegistrado(placa)) {
            // Obtener la hora de ingreso
            String horaIngreso = obtenerHoraActual();

            // Registrar el ingreso en la base de datos remota
            if (registrarIngreso(placa, horaIngreso)) {
                Toast.makeText(this, "Ingreso registrado correctamente", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error al registrar el ingreso", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "El auto no está registrado", Toast.LENGTH_SHORT).show();
        }
    }

    // Verificar si el auto está registrado en la base de datos remota
    private boolean esAutoRegistrado(String placa) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            // Establecer la conexión a la base de datos remota
            connection = new ConexionSQL().conectionclass();

            // Consulta para verificar si el auto está registrado por su placa
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

    // Registrar el ingreso en la base de datos
    private boolean registrarIngreso(String placa, String horaIngreso) {
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

                // Registrar el ingreso en la base de datos remota (solo placa y hora de ingreso)
                return registrarIngresoRemoto(idAuto, horaIngreso, connection);
            }
        } catch (SQLException e) {
            Log.e("DB", "Error al registrar ingreso", e);
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

    private boolean registrarIngresoRemoto(int idAuto, String horaIngreso, Connection connection) {
        PreparedStatement stmt = null;
        try {
            String query = "INSERT INTO registros (id_auto, hora_ingreso, hora_salida, estado) VALUES (?, ?, ?, ?)";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, idAuto);  // id_auto
            stmt.setString(2, horaIngreso);  // hora_ingreso
            stmt.setNull(3, java.sql.Types.NULL);  // hora_salida es NULL en el momento del ingreso
            stmt.setString(4, "INGRESADO");  // Estado de ingreso

            // Ejecutar la inserción en la base de datos remota
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.e("DB", "Error al registrar ingreso remoto", e);
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
