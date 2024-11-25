        package com.example.registroautosqr;



        import android.annotation.SuppressLint;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.ArrayAdapter;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.activity.EdgeToEdge;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.graphics.Insets;
        import androidx.core.view.ViewCompat;
        import androidx.core.view.WindowInsetsCompat;

        import java.sql.Connection;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.util.ArrayList;

        public class VerRegistrosActivity extends AppCompatActivity {

            EditText buscarPlaca;
            ImageButton btnBuscar;
            TextView verIngreso, verSalida;
            ListView verRegistros;
            TextView txtConsultarRegistro, txtUltimosRegistros, txtPlaca, txtUltimoIngreso, txtUltimaSalida;


            @SuppressLint("MissingInflatedId")
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                EdgeToEdge.enable(this);
                setContentView(R.layout.activity_ver_registros);

                buscarPlaca = (EditText) findViewById(R.id.BuscarPlaca);
                btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
                verIngreso = (TextView) findViewById(R.id.verIngreso);
                verSalida = (TextView) findViewById(R.id.verSalida);
                verRegistros = (ListView) findViewById(R.id.verRegistros);
                txtConsultarRegistro = (TextView) findViewById(R.id.txtConsultarRegistro);
                txtUltimosRegistros = (TextView) findViewById(R.id.txtUltimosRegistros);
                txtPlaca = (TextView) findViewById(R.id.txtPlaca);
                txtUltimoIngreso = (TextView) findViewById(R.id.txtUltimoIngreso);

                // Configurar el botón de buscar
                btnBuscar.setOnClickListener(view -> {
                    String placa = buscarPlaca.getText().toString().trim();

                    if (placa.isEmpty()) {
                        Toast.makeText(this, "Por favor, ingresa una placa", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Buscar los datos de ingreso y salida de la placa ingresada
                    buscarRegistrosPorPlaca(placa);
                });

                // Cargar los últimos registros en el ListView al iniciar la actividad
                cargarUltimosRegistros();
            }

            private void buscarRegistrosPorPlaca(String placa) {
                Connection connection = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;

                try {
                    // Conexión a la base de datos
                    connection = new ConexionSQL().conectionclass();

                    // Consulta para obtener el último registro
                    String query = "SELECT TOP 1 r.hora_ingreso, r.hora_salida " +
                            "FROM registros r " +
                            "JOIN auto a ON r.id_auto = a.id_auto " +
                            "WHERE a.placa = ? " +
                            "ORDER BY r.hora_ingreso DESC";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, placa);

                    // Ejecutar la consulta
                    resultSet = stmt.executeQuery();

                    if (resultSet.next()) {
                        // Mostrar los datos en los TextView
                        String horaIngreso = resultSet.getString("hora_ingreso");
                        String horaSalida = resultSet.getString("hora_salida");

                        verIngreso.setText(horaIngreso != null ? horaIngreso : "Sin registro");
                        verSalida.setText(horaSalida != null ? horaSalida : "Sin registro");
                    } else {
                        // Mostrar mensaje si no se encuentran registros
                        Toast.makeText(this, "No se encontraron registros para la placa: " + placa, Toast.LENGTH_SHORT).show();
                        verIngreso.setText("Sin registro");
                        verSalida.setText("Sin registro");
                    }
                } catch (SQLException e) {
                    Log.e("DB", "Error al consultar los registros por placa", e);
                    Toast.makeText(this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show();
                } finally {
                    // Cerrar recursos
                    try {
                        if (resultSet != null) resultSet.close();
                        if (stmt != null) stmt.close();
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        Log.e("DB", "Error al cerrar los recursos", e);
                    }
                }
            }

            private void cargarUltimosRegistros() {
                Connection connection = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;

                try {
                    // Conexión a la base de datos
                    connection = new ConexionSQL().conectionclass();

                    // Consulta para obtener los últimos 5 registros
                    String query = "SELECT TOP 5 a.placa, r.hora_ingreso, r.hora_salida " +
                            "FROM registros r " +
                            "JOIN auto a ON r.id_auto = a.id_auto " +
                            "ORDER BY r.hora_ingreso DESC";
                    stmt = connection.prepareStatement(query);

                    // Ejecutar la consulta
                    resultSet = stmt.executeQuery();

                    ArrayList<String> registros = new ArrayList<>();
                    while (resultSet.next()) {
                        String placa = resultSet.getString("placa");
                        String horaIngreso = resultSet.getString("hora_ingreso");
                        String horaSalida = resultSet.getString("hora_salida");

                        // Formatear los datos para mostrar en el ListView
                        registros.add("Placa: " + placa + "\nIngreso: " +
                                (horaIngreso != null ? horaIngreso : "Sin registro") +
                                "\nSalida: " + (horaSalida != null ? horaSalida : "Sin registro"));
                    }

                    // Configurar el adaptador para el ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, registros);
                    verRegistros.setAdapter(adapter);

                } catch (SQLException e) {
                    Log.e("DB", "Error al cargar los últimos registros", e);
                    Toast.makeText(this, "Error al cargar los últimos registros", Toast.LENGTH_SHORT).show();
                } finally {
                    // Cerrar recursos
                    try {
                        if (resultSet != null) resultSet.close();
                        if (stmt != null) stmt.close();
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        Log.e("DB", "Error al cerrar los recursos", e);
                    }
                }
            }


        }