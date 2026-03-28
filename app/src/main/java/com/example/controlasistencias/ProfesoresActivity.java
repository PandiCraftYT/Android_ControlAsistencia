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
import com.google.gson.Gson;
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
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfesoresActivity extends AppCompatActivity implements ProfesorAdapter.ProfesorSeleccionado {

    private static final String TAG = "ProfesoresActivity";
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
    private Set<Integer> IDsRegistradosHoy = new HashSet<>();

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
        Log.d(TAG, "Iniciando ProfesoresActivity para Grupo ID: " + grupoId + " (" + grupoNombre + ")");
        
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
        apiService.getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Horario> horariosEdificio = response.body();
                    Set<String> nombresPermitidos = new HashSet<>();
                    String diaHoy = obtenerDiaActual().toLowerCase();
                    
                    for (Horario h : horariosEdificio) {
                        if (h.getGrado_grupo() != null && h.getGrado_grupo().trim().equalsIgnoreCase(grupoNombre.trim())) {
                            if (esClaseDeHoy(h, diaHoy)) {
                                String nombreNorm = normalizarTexto(h.getNombre());
                                if (!nombreNorm.isEmpty()) nombresPermitidos.add(nombreNorm);
                            }
                        }
                    }
                    consultarAsistenciasYFiltrar(grupoId, nombresPermitidos);
                } else {
                    Log.e(TAG, "Error al obtener horarios: " + response.code());
                    consultarAsistenciasYFiltrar(grupoId, new HashSet<>());
                }
            }
            @Override public void onFailure(Call<List<Horario>> call, Throwable t) {
                Log.e(TAG, "Fallo red horarios: " + t.getMessage());
                consultarAsistenciasYFiltrar(grupoId, new HashSet<>());
            }
        });
    }

    private void consultarAsistenciasYFiltrar(int grupoId, Set<String> nombresPermitidos) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getAsistenciasPorGrupo(grupoId).enqueue(new Callback<List<Asistencia>>() {
            @Override
            public void onResponse(Call<List<Asistencia>> call, Response<List<Asistencia>> response) {
                IDsRegistradosHoy.clear();
                Set<String> llavesIdentidad = new HashSet<>();
                
                SimpleDateFormat sdfHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdfHoy.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
                String hoyNormalizado = sdfHoy.format(new Date());

                Log.d(TAG, "--- INICIO ANALISIS ASISTENCIAS ---");
                Log.d(TAG, "Hoy es: " + hoyNormalizado + " | Grupo ID: " + grupoId);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Registros recibidos: " + response.body().size());
                    Gson gson = new Gson();
                    for (Asistencia a : response.body()) {
                        String rawJson = gson.toJson(a);
                        Log.d(TAG, "Asistencia detectada (JSON): " + rawJson);
                        
                        String fechaAsis = normalizarFormatoFecha(a.getFecha());
                        if (fechaAsis != null && fechaAsis.equals(hoyNormalizado)) {
                            // BLOQUEO POR ID
                            IDsRegistradosHoy.add(a.getHorarioId());
                            
                            // BLOQUEO POR IDENTIDAD
                            String nombre = normalizarTexto(a.getNombreIdentificador());
                            String hora = normalizarHora(a.getHoraInicioIdentificador());
                            
                            if (!nombre.isEmpty() && !hora.isEmpty()) {
                                String llave = nombre + "|" + hora + "|" + hoyNormalizado;
                                llavesIdentidad.add(llave);
                                Log.d(TAG, ">>> BLOQUEANDO: " + llave);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error en respuesta asistencias: " + response.code());
                }
                Log.d(TAG, "--- FIN ANALISIS ASISTENCIAS ---");
                descargarYFiltrarProfesores(grupoId, nombresPermitidos, true, llavesIdentidad);
            }

            @Override public void onFailure(Call<List<Asistencia>> call, Throwable t) {
                Log.e(TAG, "Error de red asistencias: " + t.getMessage());
                descargarYFiltrarProfesores(grupoId, nombresPermitidos, true, new HashSet<>());
            }
        });
    }

    private String normalizarHora(String hora) {
        if (hora == null || hora.isEmpty()) return "";
        if (hora.contains(":")) {
            String[] p = hora.split(":");
            if (p.length >= 2) return p[0] + ":" + p[1];
        }
        return hora;
    }

    private String normalizarFormatoFecha(String fecha) {
        if (fecha == null) return null;
        fecha = fecha.split(" ")[0].split("T")[0];
        if (fecha.contains("/")) {
            String[] p = fecha.split("/");
            if (p.length == 3) {
                if (p[0].length() == 4) return p[0] + "-" + p[1] + "-" + p[2];
                return p[2] + "-" + p[1] + "-" + p[0];
            }
        }
        return fecha;
    }

    private void descargarYFiltrarProfesores(int grupoId, Set<String> nombresPermitidos, boolean filtroActivo, Set<String> llavesIdentidad) {
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
                        if (filtroActivo && nombresPermitidos.contains(nombreNorm)) {
                            filtrados.add(p);
                        }
                    }

                    runOnUiThread(() -> {
                        profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, filtrados, ProfesoresActivity.this);
                        profesorAdapter.setHorariosRegistradosHoy(IDsRegistradosHoy);
                        profesorAdapter.setRegistrosBloqueados(llavesIdentidad);
                        recyclerProfesores.setAdapter(profesorAdapter);
                        profesorAdapter.iniciarActualizacionPeriodica();
                    });
                }
            }
            @Override public void onFailure(Call<List<Profesor>> call, Throwable t) {
                Toast.makeText(ProfesoresActivity.this, "Error de conexión profesores", Toast.LENGTH_SHORT).show();
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
        string = string.replaceAll("[^\\p{ASCII}]", ""); 
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
                    registrarAsistenciaLocal(tipoAsistencia, profesorSeleccionado, grupoId, horaActual, campoObservacion.getText().toString(), cuentaEscaneada, "");
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
                    registrarAsistenciaLocal("FALTA", profesorSeleccionado, grupoId, hora, campoObservacion.getText().toString(), "", qr);
                } else {
                    Toast.makeText(ProfesoresActivity.this, "❌ QR de Jefe inválido", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) {}
        });
    }

    private void registrarAsistenciaLocal(String t, Profesor p, int g, String h, String o, String fM, String fJ) {
        Asistencia asis = new Asistencia(p.getHorarioId(), t, fM, fJ, o, p.getId(), p.getNumeroCuenta(), h);
        RetrofitClient.getInstance().create(ApiService.class).registrarAsistencia(asis).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) { 
                if(r.isSuccessful()) {
                    Toast.makeText(ProfesoresActivity.this, "✅ Éxito", Toast.LENGTH_SHORT).show();
                    IDsRegistradosHoy.add(p.getHorarioId());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
                    String llave = normalizarTexto(p.getNombre()) + "|" + normalizarHora(p.getHoraInicio()) + "|" + sdf.format(new Date());
                    Set<String> llavesActuales = new HashSet<>(profesorAdapter.getRegistrosBloqueados());
                    llavesActuales.add(llave);
                    if (profesorAdapter != null) {
                        profesorAdapter.setHorariosRegistradosHoy(IDsRegistradosHoy);
                        profesorAdapter.setRegistrosBloqueados(llavesActuales);
                    }
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void iniciarReloj() {
        relojRunnable = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
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