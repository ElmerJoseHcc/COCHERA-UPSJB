package com.example.registroautosqr;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHandler {

    // Método para buscar un auto por su placa
    public String buscarAuto(String placa) throws SQLException {
        String idAuto = null;

        try (Connection connection = new ConexionSQL().conectionclass();
             PreparedStatement stmt = connection.prepareStatement("SELECT id_auto FROM auto WHERE placa = ?")) {

            stmt.setString(1, placa);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    idAuto = resultSet.getString("id_auto");
                }
            }
        } catch (SQLException e) {
            Log.e("DB", "Error al buscar auto", e);
            throw e;
        }

        return idAuto;
    }

    // Método para consultar la última hora de salida
    public String consultarUltimaHoraSalida(String idAuto) throws SQLException {
        String horaSalida = null;

        String query = "SELECT hora_salida FROM registro WHERE id_auto = ? ORDER BY id_registro DESC LIMIT 1";
        try (Connection connection = new ConexionSQL().conectionclass();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, idAuto);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    horaSalida = resultSet.getString("hora_salida");
                }
            }
        } catch (SQLException e) {
            Log.e("DB", "Error al consultar última hora de salida", e);
            throw e;
        }

        return horaSalida;
    }

    // Método para actualizar la hora de salida
    public void actualizarHoraSalida(String idAuto, String horaSalida) throws SQLException {
        String query = "UPDATE registro SET hora_salida = ? WHERE id_auto = ? AND hora_salida IS NULL";

        try (Connection connection = new ConexionSQL().conectionclass();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, horaSalida);
            stmt.setString(2, idAuto);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.e("DB", "Error al actualizar la hora de salida", e);
            throw e;
        }
    }

    // Método para registrar un nuevo ingreso
    public void registrarNuevoIngreso(String idAuto, String horaIngreso) throws SQLException {
        String query = "INSERT INTO registro (id_auto, hora_ingreso, estado) VALUES (?, ?, ?)";

        try (Connection connection = new ConexionSQL().conectionclass();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, idAuto);
            stmt.setString(2, horaIngreso);
            stmt.setString(3, "INGRESADO");
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.e("DB", "Error al registrar nuevo ingreso", e);
            throw e;
        }
    }

    // Método para obtener la hora actual
    public String obtenerHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
