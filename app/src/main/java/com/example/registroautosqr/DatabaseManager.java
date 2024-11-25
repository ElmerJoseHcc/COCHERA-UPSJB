package com.example.registroautosqr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

    public class DatabaseManager {
        private static final String URL = "jdbc:sqlserver://<SERVER>:<PORT>;databaseName=<DATABASE_NAME>";
        private static final String USER = "<USERNAME>";
        private static final String PASSWORD = "<PASSWORD>";

        private Connection connection;

        public DatabaseManager(Context context) {
            // Establecer la conexión a la base de datos remota
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                Log.e("DB", "Error al conectar a la base de datos", e);
            }
        }
    // Método para insertar una persona
    public long insertarPersona(String nombre, String apellido, String carrera, String ciclo, String dni, String telefono) {
        try {
            // Obtener el id_carrera basado en el nombre de la carrera
            int idCarrera = obtenerIdCarrera(carrera); // Cambiar el método aquí
            if (idCarrera == -1) {
                return -1; // Error si no se encuentra la carrera
            }

            String query = "INSERT INTO persona (nombre, apellido, carrera, ciclo, dni, telefono) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, nombre);
                stmt.setString(2, apellido);
                stmt.setInt(3, idCarrera);
                stmt.setString(4, ciclo);
                stmt.setString(5, dni);
                stmt.setString(6, telefono);

                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e("DB", "Error al insertar persona", e);
        }
        return -1;
    }

        public ArrayList<String> obtenerCarreras() {
            ArrayList<String> carreras = new ArrayList<>();
            try {
                String query = "SELECT nombre_carrera FROM carrera";
                try (PreparedStatement stmt = connection.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        carreras.add(rs.getString("nombre_carrera"));
                    }
                }
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener carreras", e);
            }
            return carreras;
        }



        public long insertarAuto(String placa, String modelo, int idPersona) {
            try {
                String query = "INSERT INTO auto (placa, modelo, id_persona) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, placa);
                    stmt.setString(2, modelo);
                    stmt.setInt(3, idPersona);

                    return stmt.executeUpdate();
                }
            } catch (SQLException e) {
                Log.e("DB", "Error al insertar auto", e);
            }
            return -1;
        }


        public long insertarRegistro(int idAuto, String horaIngreso, String estado) {
            try {
                String query = "INSERT INTO registros (id_auto, hora_ingreso, estado) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setInt(1, idAuto);
                    stmt.setString(2, horaIngreso);
                    stmt.setString(3, estado);

                    return stmt.executeUpdate();
                }
            } catch (SQLException e) {
                Log.e("DB", "Error al insertar registro", e);
            }
            return -1;
        }

        private int obtenerIdCarrera(String nombreCarrera) {
            try {
                String query = "SELECT id_carrera FROM carrera WHERE nombre_carrera = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, nombreCarrera);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("id_carrera");
                    }
                }
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener ID de carrera", e);
            }
            return -1;
        }

        // Obtener todas las personas
        public ResultSet obtenerPersonas() {
            try {
                String query = "SELECT * FROM persona";
                PreparedStatement stmt = connection.prepareStatement(query);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener personas", e);
            }
            return null;
        }

        // Obtener autos por persona
        public ResultSet obtenerAutosPorPersona(int idPersona) {
            try {
                String query = "SELECT * FROM auto WHERE id_persona = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, idPersona);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener autos por persona", e);
            }
            return null;
        }

        // Buscar auto por placa
        public ResultSet buscarAutoPorPlaca(String placa) {
            try {
                String query = "SELECT * FROM auto WHERE placa = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, placa);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al buscar auto por placa", e);
            }
            return null;
        }

        // Obtener el último registro de un auto
        public ResultSet obtenerUltimoRegistro(String idAuto) {
            try {
                String query = "SELECT * FROM registro WHERE id_auto = ? ORDER BY id_registro DESC LIMIT 1";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, idAuto);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener último registro", e);
            }
            return null;
        }

        // Actualizar la hora de salida
        public void actualizarHoraSalida(String idAuto, String horaSalida) {
            try {
                String query = "UPDATE registro SET hora_salida = ? WHERE id_auto = ? AND hora_salida IS NULL";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, horaSalida);
                stmt.setString(2, idAuto);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Log.e("DB", "Error al actualizar hora de salida", e);
            }
        }

        // Insertar un nuevo registro
        public long insertarRegistro(String idAuto, String horaIngreso) {
            try {
                String query = "INSERT INTO registro (id_auto, hora_ingreso) VALUES (?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, idAuto);
                stmt.setString(2, horaIngreso);
                return stmt.executeUpdate();
            } catch (SQLException e) {
                Log.e("DB", "Error al insertar registro", e);
            }
            return -1;
        }

        // Registrar ingreso de un auto
        public boolean registrarIngreso(int idAuto, String horaIngreso) {
            try {
                String query = "INSERT INTO registros (id_auto, hora_ingreso, estado) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, idAuto);
                stmt.setString(2, horaIngreso);
                stmt.setString(3, "INGRESADO");
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Log.e("DB", "Error al registrar ingreso", e);
            }
            return false;
        }

        // Registrar salida de un auto
        public long registrarSalida(String placa) {
            try {
                String query = "UPDATE autos SET fecha_salida = ? WHERE placa = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, placa);
                return stmt.executeUpdate();
            } catch (SQLException e) {
                Log.e("DB", "Error al registrar salida", e);
            }
            return -1;
        }

        // Obtener autos dentro
        public ResultSet obtenerAutosDentro() {
            try {
                String query = "SELECT a.placa, r.hora_ingreso FROM registros r " +
                        "JOIN auto a ON r.id_auto = a.id_auto " +
                        "WHERE r.hora_salida IS NULL OR r.estado = 'dentro'";
                PreparedStatement stmt = connection.prepareStatement(query);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener autos dentro", e);
            }
            return null;
        }

        // Obtener auto por placa
        public ResultSet obtenerAutoPorPlaca(String placa) {
            try {
                String query = "SELECT * FROM auto WHERE placa = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, placa);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener auto por placa", e);
            }
            return null;
        }

        // Obtener estado del auto por placa
        public ResultSet obtenerAutoPorPlaca1(String placa) {
            try {
                String query = "SELECT estado FROM registros WHERE id_auto = (SELECT id_auto FROM auto WHERE placa = ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, placa);
                return stmt.executeQuery();
            } catch (SQLException e) {
                Log.e("DB", "Error al obtener estado del auto por placa", e);
            }
            return null;
        }

        // Registrar salida con hora
        public boolean registrarSalida(String placa, String horaSalida) {
            try {
                String query = "UPDATE registros SET hora_salida = ?, estado = 'salido' WHERE id_auto = (SELECT id_auto FROM auto WHERE placa = ? AND estado = 'ingresado')";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, horaSalida);
                stmt.setString(2, placa);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Log.e("DB", "Error al registrar salida con hora", e);
            }
            return false;
        }






}
