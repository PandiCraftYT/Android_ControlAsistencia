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

        iniciarReloj();

        grupoId = getIntent().getIntExtra("grupoId", -1);
        if (grupoId != -1) {
            obtenerProfesoresPorGrupo(grupoId);
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
        Toast.makeText(this, "🔍 procesarQR() llamado", Toast.LENGTH_SHORT).show();
        Log.d("QR_DEBUG", "📲 Escaneado: " + contenidoQR);

        if (tipoAsistencia == null || profesorSeleccionado == null) {
            Toast.makeText(this, "⚠️ tipoAsistencia o profesorSeleccionado es null", Toast.LENGTH_LONG).show();
            Log.e("QR_DEBUG", "tipoAsistencia = " + tipoAsistencia + ", profesorSeleccionado = " + profesorSeleccionado);
            return;
        }

        try {
            String cuentaEscaneada = contenidoQR.trim();
            String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            Log.d("QR_DEBUG", "Cuenta escaneada = " + cuentaEscaneada);
            String cuentaProfesor = profesorSeleccionado.getNumeroCuenta();

            if (cuentaProfesor == null) {
                Toast.makeText(this, "❌ El profesor no tiene número de cuenta asignado", Toast.LENGTH_LONG).show();
                Log.e("QR_DEBUG", "cuentaProfesor = null");
                return;
            }

            if (tipoAsistencia.equalsIgnoreCase("ASISTENCIA") || tipoAsistencia.equalsIgnoreCase("RETARDO")) {
                if (cuentaProfesor.equalsIgnoreCase(cuentaEscaneada)) {
                    Toast.makeText(this, "✅ Profesor verificado", Toast.LENGTH_SHORT).show();
                    String observacion = tipoAsistencia.equalsIgnoreCase("RETARDO") ? "Hora entrada: " + hora : "";

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
                    Toast.makeText(this, "❌ QR inválido, no coincide con el profesor", Toast.LENGTH_LONG).show();
                }

            } else if (tipoAsistencia.equalsIgnoreCase("FALTA")) {
                if (cuentaEscaneada.startsWith("JG")) {
                    Toast.makeText(this, "✅ Jefe de grupo verificado", Toast.LENGTH_SHORT).show();
                    registrarAsistenciaLocal(
                            tipoAsistencia,
                            profesorSeleccionado,
                            grupoId,
                            hora,
                            campoObservacion != null ? campoObservacion.getText().toString() : "",
                            null,
                            cuentaEscaneada
                    );
                } else {
                    Toast.makeText(this, "❌ Este QR no es del jefe de grupo", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "⚠ Tipo de asistencia desconocido: " + tipoAsistencia, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "❌ Error procesando QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("QR_ERROR", "Excepción capturada", e);
        }

        Toast.makeText(this, "✅ Fin del proceso QR", Toast.LENGTH_SHORT).show();
    }



    private void registrarAsistenciaLocal(String tipo, Profesor profesor, int grupoId,
                                          String hora, String observacion,
                                          String firmaMaestro, String firmaJefeGrupo) {
        Log.d("ASISTENCIA_DEBUG", "cuenta_empleado: " + profesor.getNumeroCuenta());

        String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Asistencia nuevaAsistencia = new Asistencia(
                profesor.getHorarioId(), // ← este también asegúrate de que esté seteado
                tipoAsistencia,
                profesor.getNumeroCuenta(), // firma_maestro
                null,                       // firma_jefe_grupo
                "",                         // observación
                profesor.getId(),
                profesor.getNumeroCuenta(),
                horaActual
        );


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

    private void obtenerProfesoresPorGrupo(int grupoId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        String url = "http://192.168.100.4:8080/api/profesores/porGrupo/" + grupoId;

        apiService.getProfesoresPorGrupo(url).enqueue(new Callback<List<Profesor>>() {
            @Override
            public void onResponse(Call<List<Profesor>> call, Response<List<Profesor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Profesor> profesores = response.body();
                    profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, profesores, ProfesoresActivity.this);
                    recyclerProfesores.setAdapter(profesorAdapter);
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
            String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
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
