package com.example.registroautosqr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertarPersona(String nombre, String apellido, String carrera, String ciclo, String dni, String telefono) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Obtener el id_carrera basado en el nombre de la carrera
        int idCarrera = obtenerIdCarrera(carrera); // Cambiamos el método aquí
        if (idCarrera == -1) {
            return -1; // Error si no se encuentra la carrera
        }

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("apellido", apellido);
        values.put("carrera", idCarrera); // Almacenar el ID de la carrera
        values.put("ciclo", ciclo);
        values.put("dni", dni);
        values.put("telefono", telefono);

        return db.insert("persona", null, values);
    }


    public ArrayList<String> obtenerCarreras() {
        ArrayList<String> carreras = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre_carrera FROM carrera", null);

        while (cursor.moveToNext()) {
            carreras.add(cursor.getString(0)); // Obtén el nombre de la carrera
        }
        cursor.close();
        return carreras;
    }



    public long insertarAuto(String placa, String modelo, int idPersona) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("placa", placa);
        values.put("modelo", modelo);
        values.put("id_persona", idPersona);

        return db.insert("auto", null, values);
    }

    public long insertarRegistro(int idAuto, String horaIngreso, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_auto", idAuto);
        values.put("hora_ingreso", horaIngreso);
        values.put("estado", estado);

        return db.insert("registros", null, values);
    }

    public Cursor obtenerPersonas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM persona", null);
    }

    public Cursor obtenerAutosPorPersona(int idPersona) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM auto WHERE id_persona = ?", new String[]{String.valueOf(idPersona)});
    }

    // Buscar auto por placa
    public Cursor buscarAutoPorPlaca(String placa) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM auto WHERE placa = ?", new String[]{placa});
    }

    // Obtener el último registro del auto
    public Cursor obtenerUltimoRegistro(String idAuto) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM registro WHERE id_auto = ? ORDER BY id_registro DESC LIMIT 1", new String[]{idAuto});
    }

    // Actualizar la hora de salida
    public void actualizarHoraSalida(String idAuto, String horaSalida) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        db.execSQL("UPDATE registro SET hora_salida = ? WHERE id_auto = ? AND hora_salida IS NULL", new Object[]{horaSalida, idAuto});
    }

    // Insertar un nuevo registro
    public long insertarRegistro(String idAuto, String horaIngreso) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_auto", idAuto);
        values.put("hora_ingreso", horaIngreso);
        return db.insert("registro", null, values);
    }

    public boolean registrarIngreso(int idAuto, String horaIngreso) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_auto", idAuto);
        values.put("hora_ingreso", horaIngreso);
        values.put("estado", "INGRESADO");

        long result = db.insert("registros", null, values);
        return result != -1;
    }


    public long registrarSalida(String placa) {
        // Lógica para registrar la salida en la base de datos (por ejemplo, actualizar la fecha de salida)
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha_salida", System.currentTimeMillis());  // Registra la fecha de salida

        return db.update("autos", values, "placa=?", new String[]{placa});
    }

    public Cursor obtenerAutosDentro() {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        // Consulta para obtener los registros con estado 'dentro' o sin hora_salida
        return db.rawQuery("SELECT a.placa, r.hora_ingreso FROM registros r " +
                "JOIN auto a ON r.id_auto = a.id_auto " +
                "WHERE (r.hora_salida IS NULL OR r.estado = 'dentro')", null);
    }


    public Cursor obtenerAutoPorPlaca(String placa) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        // Definimos la consulta SQL
        String query = "SELECT * FROM auto WHERE placa = ?";
        // Ejecutamos la consulta pasando la placa como parámetro
        return db.rawQuery(query, new String[]{placa});
    }

    public Cursor obtenerAutoPorPlaca1(String placa) {
        Log.d("DB", "Buscando auto con placa: " + placa);
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        // Asegúrate de que la consulta esté buscando bien en la tabla 'registros' y 'auto'
        return db.rawQuery("SELECT estado FROM registros WHERE id_auto = (SELECT id_auto FROM auto WHERE placa = ?)", new String[]{placa});
    }


    public boolean registrarSalida(String placa, String horaSalida) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        // Actualizamos el registro de salida para el auto con la placa proporcionada
        ContentValues contentValues = new ContentValues();
        contentValues.put("hora_salida", horaSalida);
        contentValues.put("estado", "salido");  // Marcamos como "salido"

        // Actualizamos el registro en la base de datos
        int rowsUpdated = db.update("registros", contentValues, "id_auto = (SELECT id_auto FROM auto WHERE placa = ? AND estado = 'ingresado')", new String[]{placa});

        // Si se actualizó alguna fila, la salida se registró correctamente
        return rowsUpdated > 0;
    }

    private int obtenerIdCarrera(String nombreCarrera) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_carrera FROM carrera WHERE nombre_carrera = ?", new String[]{nombreCarrera});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0); // Obtén el id_carrera
            cursor.close();
            return id;
        }
        cursor.close();
        return -1; // Retorna -1 si no se encuentra la carrera
    }




}
