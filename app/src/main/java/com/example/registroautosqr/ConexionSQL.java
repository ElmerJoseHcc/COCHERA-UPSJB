package com.example.registroautosqr;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConexionSQL {

    private Connection conexion;
    private String name, pass, ip, port, database;

    // Constructor que define la configuración de conexión
    public ConexionSQL() {
        ip = "192.168.1.103";
        port = "7730";
        database = "COCHERA_UPSJB";
        name = "admin";
        pass = "123";
    }

    // Método para establecer la conexión con la base de datos
    public Connection conectionclass() {
        // Permitir todas las operaciones en el hilo principal (con precaución)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String connectionURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database + ";user=" + name + ";password=" + pass + ";encrypt=false;";
        try {
            // Registrar el driver JDBC
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conexion = DriverManager.getConnection(connectionURL);
        } catch (Exception ex) {
            Log.e("Error", "Error de conexión: " + ex.getMessage());
            ex.printStackTrace();
        }
        return conexion;
    }

    // Método para ejecutar consultas SELECT y retornar un ResultSet
    public ResultSet ejecutarConsulta(String query) {
        Connection conn = conectionclass();
        if (conn == null) {
            Log.e("Error", "No se pudo conectar a SQL Server");
            return null;
        }

        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query); // Ejecuta la consulta SELECT y retorna un ResultSet
        } catch (Exception e) {
            Log.e("Error", "Error al ejecutar la consulta", e);
            return null;
        }
    }

    // Método para ejecutar scripts SQL que no retornen resultados (como INSERT, UPDATE, DELETE)
    public boolean executeScript(String sql) {
        Connection conn = conectionclass();
        if (conn == null) {
            Log.e("Error", "No se pudo conectar a SQL Server");
            return false;
        }

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql); // Ejecuta un script SQL
            return true;
        } catch (Exception e) {
            Log.e("Error", "Error al ejecutar el script", e);
            return false;
        }
    }
}
