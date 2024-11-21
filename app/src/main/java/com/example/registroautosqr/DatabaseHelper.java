package com.example.registroautosqr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper  extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "registro_autos3.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE persona (" +
                "id_persona INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "apellido TEXT NOT NULL, " +
                "carrera TEXT NOT NULL, " +
                "ciclo TEXT NOT NULL," +
                "dni TEXT UNIQUE NOT NULL, " +
                "telefono TEXT)");


        db.execSQL("CREATE TABLE auto (" +
                "id_auto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "placa TEXT UNIQUE NOT NULL, " +
                "modelo TEXT NOT NULL, " +
                "id_persona INTEGER NOT NULL, " +
                "FOREIGN KEY(id_persona) REFERENCES persona(id_persona))");


        db.execSQL("CREATE TABLE registros (" +
                "id_registro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_auto INTEGER NOT NULL, " +
                "hora_ingreso TEXT NOT NULL, " +
                "hora_salida TEXT, " +
                "estado TEXT NOT NULL, " +
                "FOREIGN KEY(id_auto) REFERENCES auto(id_auto))");

        db.execSQL("CREATE TABLE carrera (" +
                "id_carrera INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre_carrera TEXT NOT NULL)");

        // Insertar carreras predefinidas
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Ingeniería de Sistemas')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Ingeniería Civil')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Ingeniería Agroindustrial')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Ingeniería En Enología y Viticultura')");

        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Derecho')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Contabilidad')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Administración de Empresas')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Turismo Hotelería y Gastronomía')");



        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Psicología')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Laboratorio Clínico y Anatomía Patológica')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Terapia Fisica y Rehabilitación')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Enfermería')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Estomastología')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('medicina veterinaria y Zootecnia')");
        db.execSQL("INSERT INTO carrera (nombre_carrera) VALUES ('Medicina Humana')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS persona");
        db.execSQL("DROP TABLE IF EXISTS auto");
        db.execSQL("DROP TABLE IF EXISTS registros");
        db.execSQL("DROP TABLE IF EXISTS carrera");
        onCreate(db);
    }
}
