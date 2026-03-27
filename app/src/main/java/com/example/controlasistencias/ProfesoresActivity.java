package com.example.controlasistencias;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.Modelos.Profesor;
import com.example.controlasistencias.Modelos.ProfesorAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfesoresActivity extends AppCompatActivity implements ProfesorAdapter.ProfesorSeleccionado {

    private TextView relojHora;
    private RecyclerView recyclerProfesores;
    private ProfesorAdapter profesorAdapter;
    private Handler handler = new Handler();
    private Runnable relojRunnable;

    private String tipoAsistencia;
    private EditText campoObservacion;
    private Profesor profesorSeleccionado;
    private int grupoId;
    private String zonaNombre;
    private String grupoNombre;

    private ActivityResultLauncher<Intent> qrScanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profesores);
        
        TextView txtZona = findViewById(R.id.txtZona);
        TextView txtGrupo = findViewById(R.id.txtGrupo);
        relojHora = findViewById(R.id.relojHora);
        recyclerProfesores = findViewById(R.id.recyclerProfesores);
        recyclerProfesores.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj();
        zonaNombre = getIntent().getStringExtra("zonaNombre");
        grupoNombre = getIntent().getStringExtra("grupoNombre");
        
        if (zonaNombre != null) txtZona.setText("Horarios - Zona: " + zonaNombre);
        if (grupoNombre != null) txtGrupo.setText("Grupo: " + grupoNombre);
        
        grupoId = getIntent().getIntExtra("grupoId", -1);
        if (grupoId != -1 && zonaNombre != null) {
            obtenerProfesoresConValidacionDeZona(grupoId, zonaNombre);
        } else {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_LONG).show();
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

    private void obtenerProfesoresConValidacionDeZona(int grupoId, String zona) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        
        // PASO 1: Obtener el horario oficial de ESTA zona/edificio
        apiService.getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Horario> horariosEdificio = response.body();
                    Set<String> nombresPermitidos = new HashSet<>();
                    String diaHoy = obtenerDiaActual().toLowerCase();
                    
                    for (Horario h : horariosEdificio) {
                        // Filtramos por nombre de grupo y día
                        if (h.getGrado_grupo() != null && h.getGrado_grupo().trim().equalsIgnoreCase(grupoNombre.trim())) {
                            if (esClaseDeHoy(h, diaHoy)) {
                                nombresPermitidos.add(normalizarTexto(h.getNombre()));
                            }
                        }
                    }
                    // PASO 2: Descargar profesores aplicando el filtro (strict = true)
                    descargarYFiltrarProfesores(grupoId, nombresPermitidos, true);
                } else {
                    Toast.makeText(ProfesoresActivity.this, "Error al consultar horario oficial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Horario>> call, Throwable t) {
                Toast.makeText(ProfesoresActivity.this, "Fallo de red al consultar horario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void descargarYFiltrarProfesores(int grupoId, Set<String> nombresPermitidos, boolean filtroActivo) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        String url = "https://preparatoria.charlystudio.org/android/profesores/porGrupo/" + grupoId;

        apiService.getProfesoresPorGrupo(url).enqueue(new Callback<List<Profesor>>() {
            @Override
            public void onResponse(Call<List<Profesor>> call, Response<List<Profesor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Profesor> todos = response.body();
                    List<Profesor> filtrados = new ArrayList<>();

                    for (Profesor p : todos) {
                        String nombreNorm = normalizarTexto(p.getNombre());
                        // FILTRO ESTRICTO: Solo si el nombre está en la lista blanca de la zona hoy
                        if (filtroActivo && nombresPermitidos.contains(nombreNorm)) {
                            filtrados.add(p);
                        }
                    }

                    if (filtrados.isEmpty()) {
                        Toast.makeText(ProfesoresActivity.this, "No hay clases programadas para hoy en este grupo.", Toast.LENGTH_LONG).show();
                    }

                    runOnUiThread(() -> {
                        profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, filtrados, ProfesoresActivity.this);
                        recyclerProfesores.setAdapter(profesorAdapter);
                        profesorAdapter.iniciarActualizacionPeriodica();
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Profesor>> call, Throwable t) {
                Toast.makeText(ProfesoresActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean esClaseDeHoy(Horario h, String dia) {
        String campo;
        switch (dia) {
            case "lunes":     campo = h.getLunes(); break;
            case "martes":    campo = h.getMartes(); break;
            case "miércoles": campo = h.getMiercoles(); break;
            case "jueves":    campo = h.getJueves(); break;
            case "viernes":   campo = h.getViernes(); break;
            default:          campo = null;
        }
        return campo != null && !campo.trim().isEmpty() && !"null".equalsIgnoreCase(campo);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String string = Normalizer.normalize(texto, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", ""); // Quitar acentos
        return string.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private String obtenerDiaActual() {
        String[] dias = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        return dias[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    @Override
    public void onScanRequested(Profesor profesor, String tipoAsistencia, EditText campoObservacion) {
        this.profesorSeleccionado = profesor;
        this.tipoAsistencia = tipoAsistencia;
        this.campoObservacion = campoObservacion;
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        qrScanLauncher.launch(integrator.createScanIntent());
    }

    private void procesarQR(String contenidoQR) {
        if (tipoAsistencia == null || profesorSeleccionado == null) return;
        try {
            String cuentaEscaneada = contenidoQR.trim();
            String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            if (tipoAsistencia.equalsIgnoreCase("FALTA")) {
                validarJefeYRegistrar(cuentaEscaneada, horaActual);
            } else {
                if (profesorSeleccionado.getNumeroCuenta().equalsIgnoreCase(cuentaEscaneada)) {
                    registrarAsistenciaLocal(tipoAsistencia, profesorSeleccionado, grupoId, horaActual, "", cuentaEscaneada, null);
                } else {
                    Toast.makeText(this, "❌ QR no corresponde al profesor", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) { Log.e("QR", "Error", e); }
    }

    private void validarJefeYRegistrar(String qr, String hora) {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getJefesGrupoPorGrupo(grupoId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().contains(qr)) {
                    registrarAsistenciaLocal("FALTA", profesorSeleccionado, grupoId, hora, "Registrado por Jefe", null, qr);
                } else {
                    Toast.makeText(ProfesoresActivity.this, "❌ QR de Jefe inválido", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) {}
        });
    }

    private void registrarAsistenciaLocal(String t, Profesor p, int g, String h, String o, String fM, String fJ) {
        Asistencia asis = new Asistencia(p.getHorarioId(), t, p.getNumeroCuenta(), fJ, o, p.getId(), p.getNumeroCuenta(), h);
        RetrofitClient.getInstance().create(ApiService.class).registrarAsistencia(asis).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) { 
                if(r.isSuccessful()) Toast.makeText(ProfesoresActivity.this, "✅ Éxito", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void iniciarReloj() {
        relojRunnable = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Mazatlan"));
            relojHora.setText(sdf.format(new Date()));
            handler.postDelayed(relojRunnable, 1000);
        };
        handler.post(relojRunnable);
    }

    @Override protected void onDestroy() { super.onDestroy(); handler.removeCallbacks(relojRunnable); }
    @Override public void onQRResultado(Profesor p, String c) {}
    @Override public String getNumeroCuenta() { return ""; }
    @Override public Profesor getProfesor() { return null; }
}