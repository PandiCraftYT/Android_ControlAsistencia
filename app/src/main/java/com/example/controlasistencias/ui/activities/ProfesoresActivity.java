package com.example.controlasistencias.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.models.Asistencia;
import com.example.controlasistencias.models.Horario;
import com.example.controlasistencias.models.Profesor;
import com.example.controlasistencias.models.local.AppDatabase;
import com.example.controlasistencias.models.local.AsistenciaPendiente;
import com.example.controlasistencias.ui.adapters.ProfesorAdapter;
import com.example.controlasistencias.R;
import com.example.controlasistencias.Utils.AppUtils;
import com.example.controlasistencias.Utils.DialogUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfesoresActivity extends AppCompatActivity implements ProfesorAdapter.ProfesorSeleccionado {

    private static final String TAG = "ProfesoresActivity";
    private TextView relojHora;
    private RecyclerView recyclerProfesores;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
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
    private Set<String> llavesIdentidad = new HashSet<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profesores);
        
        db = AppDatabase.getInstance(this);
        
        TextView txtZona = findViewById(R.id.txtZona);
        TextView txtGrupo = findViewById(R.id.txtGrupo);
        relojHora = findViewById(R.id.relojHora);
        recyclerProfesores = findViewById(R.id.recyclerProfesores);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerProfesores.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj();
        zonaNombre = getIntent().getStringExtra("zonaNombre");
        grupoNombre = getIntent().getStringExtra("grupoNombre");
        
        if (zonaNombre != null) txtZona.setText("Horarios - Zona: " + zonaNombre);
        if (grupoNombre != null) txtGrupo.setText("Grupo: " + grupoNombre);
        
        grupoId = getIntent().getIntExtra("grupoId", -1);
        
        if (grupoId != -1 && zonaNombre != null) {
            obtenerProfesoresConValidacionDeZona();
            sincronizarAsistenciasPendientes();
        } else {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_LONG).show();
        }

        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
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

    private void sincronizarAsistenciasPendientes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AsistenciaPendiente> pendientes = db.asistenciaDao().obtenerTodas();
            if (pendientes != null && !pendientes.isEmpty()) {
                ApiService api = RetrofitClient.getInstance().create(ApiService.class);
                for (AsistenciaPendiente ap : pendientes) {
                    Asistencia asis = new Asistencia(ap.horarioId, ap.tipo, ap.firmaMaestro, ap.firmaJefe, ap.observaciones, ap.profesorId, ap.numeroCuenta, ap.fecha);
                    try {
                        Response<Void> response = api.registrarAsistencia(asis).execute();
                        if (response.isSuccessful()) {
                            db.asistenciaDao().eliminar(ap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error de red al sincronizar");
                    }
                }
            }
        });
    }

    private void obtenerProfesoresConValidacionDeZona() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getHorariosPorZona(zonaNombre).enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Horario> horariosEdificio = response.body();
                    Set<String> nombresPermitidos = new HashSet<>();
                    String diaHoy = AppUtils.obtenerDiaActual().toLowerCase();
                    
                    for (Horario h : horariosEdificio) {
                        if (h.getGrado_grupo() != null && h.getGrado_grupo().trim().equalsIgnoreCase(grupoNombre.trim())) {
                            if (esClaseDeHoy(h, diaHoy)) {
                                String nombreNorm = AppUtils.normalizarTexto(h.getNombre());
                                if (!nombreNorm.isEmpty()) nombresPermitidos.add(nombreNorm);
                            }
                        }
                    }
                    consultarAsistenciasYFiltrar(nombresPermitidos);
                } else {
                    progressBar.setVisibility(View.GONE);
                    DialogUtils.mostrarErrorConexion(ProfesoresActivity.this, () -> obtenerProfesoresConValidacionDeZona());
                }
            }
            @Override public void onFailure(Call<List<Horario>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                DialogUtils.mostrarErrorConexion(ProfesoresActivity.this, () -> obtenerProfesoresConValidacionDeZona());
            }
        });
    }

    private void consultarAsistenciasYFiltrar(Set<String> nombresPermitidos) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getAsistenciasPorGrupo(grupoId).enqueue(new Callback<List<Asistencia>>() {
            @Override
            public void onResponse(Call<List<Asistencia>> call, Response<List<Asistencia>> response) {
                IDsRegistradosHoy.clear();
                llavesIdentidad.clear();
                String hoyNormalizado = AppUtils.getFechaHoyMazatlan();

                if (response.isSuccessful() && response.body() != null) {
                    for (Asistencia a : response.body()) {
                        String fechaAsis = AppUtils.normalizarFormatoFecha(a.getFecha());
                        if (fechaAsis != null && fechaAsis.equals(hoyNormalizado)) {
                            IDsRegistradosHoy.add(a.getHorarioId());
                            String nombre = AppUtils.normalizarTexto(a.getNombreIdentificador());
                            String hora = AppUtils.normalizarHora(a.getHoraInicioIdentificador());
                            if (!nombre.isEmpty() && !hora.isEmpty()) {
                                llavesIdentidad.add(nombre + "|" + hora + "|" + hoyNormalizado);
                            }
                        }
                    }
                }
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<AsistenciaPendiente> locales = db.asistenciaDao().obtenerTodas();
                    for (AsistenciaPendiente lp : locales) {
                        if (lp.fecha.equals(hoyNormalizado)) {
                            IDsRegistradosHoy.add(lp.horarioId);
                        }
                    }
                    runOnUiThread(() -> descargarYFiltrarProfesores(nombresPermitidos, true));
                });
            }

            @Override public void onFailure(Call<List<Asistencia>> call, Throwable t) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    String hoy = AppUtils.getFechaHoyMazatlan();
                    List<AsistenciaPendiente> locales = db.asistenciaDao().obtenerTodas();
                    for (AsistenciaPendiente lp : locales) {
                        if (lp.fecha.equals(hoy)) IDsRegistradosHoy.add(lp.horarioId);
                    }
                    runOnUiThread(() -> descargarYFiltrarProfesores(nombresPermitidos, true));
                });
            }
        });
    }

    private void descargarYFiltrarProfesores(Set<String> nombresPermitidos, boolean filtroActivo) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getProfesoresPorGrupo(grupoId).enqueue(new Callback<List<Profesor>>() {
            @Override
            public void onResponse(Call<List<Profesor>> call, Response<List<Profesor>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Profesor> todos = response.body();
                    List<Profesor> filtrados = new ArrayList<>();

                    for (Profesor p : todos) {
                        String nombreNorm = AppUtils.normalizarTexto(p.getNombre());
                        if (filtroActivo && nombresPermitidos.contains(nombreNorm)) {
                            filtrados.add(p);
                        }
                    }

                    runOnUiThread(() -> {
                        if (filtrados.isEmpty()) {
                            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                            recyclerProfesores.setVisibility(View.GONE);
                        } else {
                            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                            recyclerProfesores.setVisibility(View.VISIBLE);
                            profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, filtrados, ProfesoresActivity.this);
                            profesorAdapter.setHorariosRegistradosHoy(IDsRegistradosHoy);
                            profesorAdapter.setRegistrosBloqueados(llavesIdentidad);
                            recyclerProfesores.setAdapter(profesorAdapter);
                            profesorAdapter.iniciarActualizacionPeriodica();
                        }
                    });
                } else {
                    DialogUtils.mostrarErrorConexion(ProfesoresActivity.this, () -> descargarYFiltrarProfesores(nombresPermitidos, filtroActivo));
                }
            }
            @Override public void onFailure(Call<List<Profesor>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                DialogUtils.mostrarErrorConexion(ProfesoresActivity.this, () -> descargarYFiltrarProfesores(nombresPermitidos, filtroActivo));
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
            String horaActual = AppUtils.getHoraActualMazatlan();
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
        progressBar.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getJefesGrupoPorGrupo(grupoId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().contains(qr)) {
                    registrarAsistenciaLocal("FALTA", profesorSeleccionado, grupoId, hora, campoObservacion.getText().toString(), "", qr);
                } else {
                    Toast.makeText(ProfesoresActivity.this, "❌ QR de Jefe inválido", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                DialogUtils.mostrarErrorConexion(ProfesoresActivity.this, () -> validarJefeYRegistrar(qr, hora));
            }
        });
    }

    private void registrarAsistenciaLocal(String tipo, Profesor p, int g, String h, String o, String fM, String fJ) {
        progressBar.setVisibility(View.VISIBLE);
        Asistencia asis = new Asistencia(p.getHorarioId(), tipo, fM, fJ, o, p.getId(), p.getNumeroCuenta(), h);
        RetrofitClient.getInstance().create(ApiService.class).registrarAsistencia(asis).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) { 
                progressBar.setVisibility(View.GONE);
                if(r.isSuccessful()) {
                    Toast.makeText(ProfesoresActivity.this, "✅ Éxito", Toast.LENGTH_SHORT).show();
                    marcarComoRegistrado(p);
                } else {
                    guardarOffline(tipo, p, h, o, fM, fJ);
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                guardarOffline(tipo, p, h, o, fM, fJ);
            }
        });
    }

    private void guardarOffline(String tipo, Profesor p, String h, String o, String fM, String fJ) {
        AsistenciaPendiente ap = new AsistenciaPendiente(p.getHorarioId(), tipo, fM, fJ, o, p.getId(), p.getNumeroCuenta(), h);
        Executors.newSingleThreadExecutor().execute(() -> {
            db.asistenciaDao().insertar(ap);
            runOnUiThread(() -> {
                Toast.makeText(this, "📡 Sin red. Guardado en el celular.", Toast.LENGTH_LONG).show();
                marcarComoRegistrado(p);
            });
        });
    }

    private void marcarComoRegistrado(Profesor p) {
        IDsRegistradosHoy.add(p.getHorarioId());
        String hoy = AppUtils.getFechaHoyMazatlan();
        String llave = AppUtils.normalizarTexto(p.getNombre()) + "|" + AppUtils.normalizarHora(p.getHoraInicio()) + "|" + hoy;
        llavesIdentidad.add(llave);
        if (profesorAdapter != null) {
            profesorAdapter.setHorariosRegistradosHoy(IDsRegistradosHoy);
            profesorAdapter.setRegistrosBloqueados(llavesIdentidad);
        }
    }

    private void iniciarReloj() {
        relojRunnable = () -> {
            relojHora.setText(AppUtils.getHoraActualMazatlan());
            handler.postDelayed(relojRunnable, 1000);
        };
        handler.post(relojRunnable);
    }

    @Override protected void onDestroy() { super.onDestroy(); handler.removeCallbacks(relojRunnable); }
    @Override public void onQRResultado(Profesor p, String c) {}
    @Override public String getNumeroCuenta() { return ""; }
    @Override public Profesor getProfesor() { return null; }
}
