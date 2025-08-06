package com.example.controlasistencias;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Asistencia;
import com.example.controlasistencias.Modelos.Profesor;
import com.example.controlasistencias.Modelos.ProfesorAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfesoresActivity extends AppCompatActivity implements ProfesorAdapter.ProfesorSeleccionado {

    private TextView relojHora;
    private RecyclerView recyclerProfesores;
    private ProfesorAdapter profesorAdapter;
    private Handler handler = new Handler();
    private Runnable relojRunnable;
    private Set<String> tipoAsistenciaRegistrada = new HashSet<>();

    private String tipoAsistencia;
    private EditText campoObservacion;
    private Profesor profesorSeleccionado;
    private int grupoId;

    private ActivityResultLauncher<Intent> qrScanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profesores);

        relojHora = findViewById(R.id.relojHora);
        recyclerProfesores = findViewById(R.id.recyclerProfesores);
        recyclerProfesores.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj(); // para actualizar el reloj visible

        grupoId = getIntent().getIntExtra("grupoId", -1);
        if (grupoId != -1) {
            obtenerProfesoresPorGrupo(grupoId); // ⚠️ AQUI es donde debes inicializar el adapter después
        } else {
            Toast.makeText(this, "Datos de grupo no recibidos", Toast.LENGTH_LONG).show();
        }

        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    IntentResult intentResult = IntentIntegrator.parseActivityResult(
                            result.getResultCode(), result.getData()
                    );
                    if (intentResult != null && intentResult.getContents() != null) {
                        procesarQR(intentResult.getContents());
                    } else {
                        Toast.makeText(this, "No se escaneó ningún código", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ❌ Aquí NO pongas adapter.iniciarActualizacionPeriodica(); porque aún no existe
    }


    @Override
    public void onScanRequested(Profesor profesor, String tipoAsistencia, EditText campoObservacion) {
        Log.d("SCAN_REQUESTED", "✅ Profesor recibido: " + (profesor != null ? profesor.getNombre() : "null"));

        this.profesorSeleccionado = profesor;
        this.tipoAsistencia = tipoAsistencia;
        this.campoObservacion = campoObservacion;

        if (profesorSeleccionado == null) {
            Toast.makeText(this, "❌ Profesor no asignado antes del escaneo", Toast.LENGTH_LONG).show();
            return;
        }

        if (tipoAsistencia == null) {
            Toast.makeText(this, "❌ Tipo de asistencia no definido", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("DEBUG", "📤 Llamando al escáner para " + profesor.getNombre());

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        qrScanLauncher.launch(integrator.createScanIntent());
    }

    private void procesarQR(String contenidoQR) {
        // Marcar inicio
        Log.d("QR_DEBUG", "🔍 procesarQR() llamado");
        Log.d("QR_DEBUG", "📲 Escaneado: " + contenidoQR);

        // Comprobar que tenemos tipo y profesor
        if (tipoAsistencia == null || profesorSeleccionado == null) {
            Log.e("QR_DEBUG", "⚠ tipoAsistencia o profesorSeleccionado es null: "
                    + "tipoAsistencia=" + tipoAsistencia
                    + ", profesorSeleccionado=" + profesorSeleccionado);
            Toast.makeText(this, "⚠ Datos incompletos para procesar QR", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String cuentaEscaneada = contenidoQR.trim();
            Log.d("QR_DEBUG", "🔍 cuentaEscaneada: " + cuentaEscaneada);

            String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            Log.d("QR_DEBUG", "⏰ hora: " + hora);

            String cuentaProfesor = profesorSeleccionado.getNumeroCuenta();
            Log.d("QR_DEBUG", "👨‍🏫 cuentaProfesor: " + cuentaProfesor);

            if (cuentaProfesor == null) {
                Log.e("QR_DEBUG", "❌ Profesor sin número de cuenta");
                Toast.makeText(this, "Profesor sin número de cuenta", Toast.LENGTH_LONG).show();
                return;
            }

            // ASISTENCIA / RETARDO
            if (tipoAsistencia.equalsIgnoreCase("ASISTENCIA")
                    || tipoAsistencia.equalsIgnoreCase("RETARDO")) {

                Log.d("QR_DEBUG", "📌 tipoAsistencia: " + tipoAsistencia);
                if (cuentaProfesor.equalsIgnoreCase(cuentaEscaneada)) {
                    Log.d("QR_DEBUG", "✅ Profesor verificado");
                    String observacion = tipoAsistencia.equalsIgnoreCase("RETARDO")
                            ? "Hora entrada: " + hora
                            : "";
                    registrarAsistenciaLocal(
                            tipoAsistencia,
                            profesorSeleccionado,
                            grupoId,
                            hora,
                            observacion,
                            cuentaEscaneada,
                            null
                    );
                } else {
                    Log.w("QR_DEBUG", "❌ QR no coincide con el profesor");
                    Toast.makeText(this,
                            "❌ QR inválido para este profesor", Toast.LENGTH_LONG).show();
                }

                // FALTA → validar Jefe de Grupo
            } else if (tipoAsistencia.equalsIgnoreCase("FALTA")) {
                Log.d("QR_DEBUG", "📌 tipoAsistencia: FALTA");
                Log.d("QR_DEBUG", "🔍 Validando Jefe de Grupo en grupoId=" + grupoId);

                ApiService api = RetrofitClient
                        .getInstance()
                        .create(ApiService.class);

                Call<List<String>> call = api.getJefesGrupoPorGrupo(grupoId);

                // Comprueba la URL que realmente está usando OkHttp
                Log.d("QR_DEBUG", "🌐 URL petición (okhttp): "
                        + call.request().url().toString());

                // Lanza la petición
                call.enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call,
                                           Response<List<String>> response) {
                        Log.d("QR_DEBUG", "📡 Código HTTP jefesGrupo: "
                                + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            List<String> cuentasJefe = response.body();
                            Log.d("QR_DEBUG", "👥 cuentasJefeGrupo: " + cuentasJefe);

                            if (cuentasJefe.contains(cuentaEscaneada)) {
                                Log.d("QR_DEBUG", "✅ QR validado como Jefe de Grupo");

                                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                        .format(new Date());

                                String observacionGenerada = "Falta registrada por: " + cuentaEscaneada + " a la hora: " + horaActual;

                                registrarAsistenciaLocal(
                                        tipoAsistencia,
                                        profesorSeleccionado,
                                        grupoId,
                                        horaActual,
                                        observacionGenerada,
                                        null,
                                        cuentaEscaneada
                                );
                            }
                            else {
                                Log.w("QR_DEBUG", "❌ QR no es de Jefe de Grupo");
                                Toast.makeText(ProfesoresActivity.this,
                                        "❌ Este QR no es del Jefe de Grupo",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("QR_DEBUG", "⚠ Respuesta inválida o body nulo");
                            Toast.makeText(ProfesoresActivity.this,
                                    "⚠ No se pudo validar Jefe de Grupo",
                                    Toast.LENGTH_SHORT).show();
                        }
                        Log.d("QR_DEBUG", "🏁 Fin del proceso QR (onResponse)");
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Log.e("QR_DEBUG", "❌ Error de conexión: " + t.getMessage());
                        Toast.makeText(ProfesoresActivity.this,
                                "❌ Error de conexión con backend",
                                Toast.LENGTH_SHORT).show();
                        Log.d("QR_DEBUG", "🏁 Fin del proceso QR (onFailure)");
                    }
                });

            } else {
                Log.w("QR_DEBUG", "⚠ Tipo de asistencia desconocido: "
                        + tipoAsistencia);
                Toast.makeText(this,
                        "⚠ Tipo de asistencia desconocido: " + tipoAsistencia,
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("QR_ERROR", "💥 Excepción procesando QR", e);
            Toast.makeText(this,
                    "❌ Error procesando QR: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }



    private void registrarAsistenciaLocal(String tipo, Profesor profesor, int grupoId,
                                          String hora, String observacion,
                                          String firmaMaestro, String firmaJefeGrupo) {
        Log.d("ASISTENCIA_DEBUG", "cuenta_empleado: " + profesor.getNumeroCuenta());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Mazatlan")); // Zona horaria correcta
        String horaActual = sdf.format(new Date());



        Asistencia nuevaAsistencia = new Asistencia(
                profesor.getHorarioId(),
                tipoAsistencia,
                profesor.getNumeroCuenta(),  // usa el valor que te llegó de la lectura QR
                firmaJefeGrupo,  // idem
                observacion,
                profesor.getId(),
                profesor.getNumeroCuenta(),
                horaActual);

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Log.d("ASISTENCIA_DEBUG", "Datos:");
        Log.d("ASISTENCIA_DEBUG", "profesor_id = " + profesor.getId());
        Log.d("ASISTENCIA_DEBUG", "cuenta_empleado = " + profesor.getNumeroCuenta());
        Log.d("ASISTENCIA_DEBUG", "hora = " + hora);
        Log.d("ASISTENCIA_DEBUG", "estatus = " + tipo);

        Call<Void> call = apiService.registrarAsistencia(nuevaAsistencia);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfesoresActivity.this, "✅ Asistencia registrada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Cuerpo vacío";
                        Log.e("REGISTRO_ERROR", "Código: " + response.code() + " / Body: " + errorBody);
                        Toast.makeText(ProfesoresActivity.this, "❌ Error al registrar: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ProfesoresActivity.this, "❌ Error crítico al leer respuesta", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfesoresActivity.this, "⚠️ Fallo de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String obtenerDiaActual() {
        Calendar calendar = Calendar.getInstance();
        String[] dias = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        int dia = calendar.get(Calendar.DAY_OF_WEEK); // 1 = domingo
        return dias[dia - 1];
    }

    private void obtenerProfesoresPorGrupo(int grupoId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        String url = "http://192.168.100.4:8080/api/profesores/porGrupo/" + grupoId;

        apiService.getProfesoresPorGrupo(url).enqueue(new Callback<List<Profesor>>() {
            @Override
            public void onResponse(Call<List<Profesor>> call, Response<List<Profesor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Profesor> profesores = response.body();

                    // Mostrar nombres recibidos
                    for (Profesor prof : profesores) {
                        Log.d("PROFESORES_API", "-> " + prof.getNombre());
                    }

                    // Asignar adapter
                    runOnUiThread(() -> {
                        profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, profesores, ProfesoresActivity.this);
                        recyclerProfesores.setAdapter(profesorAdapter);
                        profesorAdapter.notifyDataSetChanged();
                        // ✅ Iniciar actualización periódica cada minuto
                        profesorAdapter.iniciarActualizacionPeriodica();
                    });

                } else {
                    Toast.makeText(ProfesoresActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Profesor>> call, Throwable t) {
                Toast.makeText(ProfesoresActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void iniciarReloj() {
        relojRunnable = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Mazatlan"));
            String horaActual = sdf.format(new Date());
            relojHora.setText(horaActual);
            handler.postDelayed(relojRunnable, 1000);
        };
        handler.post(relojRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(relojRunnable);
    }

    // Métodos no usados pero requeridos por la interfaz
    @Override public void onQRResultado(Profesor profesor, String contenidoQR) {}
    @Override public String getNumeroCuenta() { return ""; }
    @Override public Profesor getProfesor() { return null; }
}
