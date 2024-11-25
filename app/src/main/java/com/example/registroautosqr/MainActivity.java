package com.example.registroautosqr;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class MainActivity extends AppCompatActivity {


    private void inicializarBaseDeDatos() {
        ConexionSQL conexionSQL = new ConexionSQL();

        // Scripts para crear tablas
        String crearTablaUsuarios = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'usuarios') " +
                "CREATE TABLE usuarios (" +
                "id_usuario INT PRIMARY KEY IDENTITY(1,1), " +
                "username NVARCHAR(50) NOT NULL UNIQUE, " +
                "password NVARCHAR(255) NOT NULL, " +
                "rol NVARCHAR(50) NULL);";


// Realiza lo mismo para las demás tablas
        String crearTablaPersona = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'persona') " +
                "CREATE TABLE persona (" +
                "id_persona INT PRIMARY KEY IDENTITY(1,1), " +
                "nombre NVARCHAR(100) NOT NULL, " +
                "apellido NVARCHAR(100) NOT NULL, " +
                "carrera NVARCHAR(100) NOT NULL, " +
                "ciclo NVARCHAR(50) NOT NULL, " +
                "dni NVARCHAR(20) UNIQUE NOT NULL, " +
                "telefono NVARCHAR(20));";

        String crearTablaAuto = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'auto') " +
                "CREATE TABLE auto (" +
                "id_auto INT PRIMARY KEY IDENTITY(1,1), " +
                "placa NVARCHAR(20) UNIQUE NOT NULL, " +
                "modelo NVARCHAR(100) NOT NULL, " +
                "id_persona INT NOT NULL, " +
                "FOREIGN KEY(id_persona) REFERENCES persona(id_persona));";

        String crearTablaRegistros = " IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'registros') " + "" +
                "CREATE TABLE registros (" +
                "id_registro INT PRIMARY KEY IDENTITY(1,1), " +
                "id_auto INT NOT NULL, " +
                "hora_ingreso NVARCHAR(50) NOT NULL, " +
                "hora_salida NVARCHAR(50), " +
                "estado NVARCHAR(50) NOT NULL, " +
                "FOREIGN KEY(id_auto) REFERENCES auto(id_auto));";

        String crearTablaCarrera = " IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'carrera') " +
                "CREATE TABLE carrera (" +
                "id_carrera INT PRIMARY KEY IDENTITY(1,1), " +
                "nombre_carrera NVARCHAR(100) NOT NULL);";


        // Ejecutar los scripts
        if (conexionSQL.executeScript(crearTablaUsuarios)) {
            Log.i("DB", "Tabla usuarios creada correctamente");
        }

        if (conexionSQL.executeScript(crearTablaPersona)) {
            Log.i("DB", "Tabla persona creada correctamente");
        }

        if (conexionSQL.executeScript(crearTablaAuto)) {
            Log.i("DB", "Tabla auto creada correctamente");
        }

        if (conexionSQL.executeScript(crearTablaRegistros)) {
            Log.i("DB", "Tabla registros creada correctamente");
        }

        if (conexionSQL.executeScript(crearTablaCarrera)) {
            Log.i("DB", "Tabla carrera creada correctamente");
        }

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarBaseDeDatos(); //PARA INICIAR CREANDO LA BASE DE DATOS Y LAS TABLAS

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = username.getText().toString().trim();
                String passInput = password.getText().toString().trim();


                if (verificarCredenciales(userInput, passInput)) {
                    Toast.makeText(MainActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                    // Redirigir a la vista principal
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            private boolean verificarCredenciales(String username, String password) {

                ConexionSQL ConexionSQL = new ConexionSQL();

                Connection connection = ConexionSQL.conectionclass();
                if (connection == null) {
                    Log.e("DB", "No se pudo conectar a la base de datos");
                    return false;
                }

                String query = "SELECT COUNT(*) AS total FROM usuarios WHERE username = ? AND password = ?";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        int total = resultSet.getInt("total");
                        return total > 0; // Si el total es mayor a 0, las credenciales son válidas
                    }
                } catch (Exception e) {
                    Log.e("DB", "Error al verificar credenciales: " + e.getMessage());
                }
                return false;
            }


        });
    }
}