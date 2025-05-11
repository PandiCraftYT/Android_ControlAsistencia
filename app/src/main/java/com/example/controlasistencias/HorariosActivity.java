package com.example.controlasistencias;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Horario;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorariosActivity extends AppCompatActivity {
    private static final String KEY_POS = "key_selected_pos";
    private static final String KEY_EST = "key_estado_actual";

    private RecyclerView horariosRecyclerView;
    private HorariosAdapter adapter;
    private ImageButton btnExportar;
    private View btnAtras;
    private TextView relojHora;
    private Handler handler = new Handler();
    private Runnable runnable;
    private String diaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        // 1) Referencia vistas
        horariosRecyclerView = findViewById(R.id.horariosRecyclerView);
        btnExportar         = findViewById(R.id.btnExportarExcel);
        btnAtras            = findViewById(R.id.btnAtras);
        relojHora           = findViewById(R.id.relojHora);

        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2) Pedir permisos para exportar
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 1);

        // 3) Botón atrás
        btnAtras.setOnClickListener(v -> finish());

        // 4) Iniciar reloj
        iniciarRelojEnVivo();

        // 5) Día actual para filtrar
        diaActual = obtenerDiaActual();

        // 6) Petición de datos
        String zona = getIntent().getStringExtra("zona");
        RetrofitClient.getInstance()
                .create(ApiService.class)
                .getHorariosPorZona(zona)
                .enqueue(new Callback<List<Horario>>() {
                    @Override
                    public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(HorariosActivity.this,
                                    "Error al obtener horarios", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Horario> lista = filtrarHorariosDelDia(response.body());

                        adapter = new HorariosAdapter(lista, diaActual, HorariosActivity.this);

                        // 6.1) Restaurar selección tras rotación
                        if (savedInstanceState != null) {
                            int pos = savedInstanceState.getInt(KEY_POS, -1);
                            String est = savedInstanceState.getString(KEY_EST, "");
                            adapter.setSelection(pos, est);
                        }

                        horariosRecyclerView.setAdapter(adapter);

                        // 6.2) Exportar
                        btnExportar.setOnClickListener(v -> exportarHorariosAExcel(lista));
                    }

                    @Override
                    public void onFailure(Call<List<Horario>> call, Throwable t) {
                        Toast.makeText(HorariosActivity.this,
                                "Error de conexión: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Actualiza el reloj cada segundo */
    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(new Date());
                relojHora.setText(hora);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    /** Calcula el día de la semana en minúsculas */
    private String obtenerDiaActual() {
        String[] dias = {"domingo","lunes","martes","miércoles","jueves","viernes","sábado"};
        int idx = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        return dias[idx];
    }

    /** Filtra solo horarios que tengan hora para el día actual */
    private List<Horario> filtrarHorariosDelDia(List<Horario> todos) {
        List<Horario> out = new ArrayList<>();
        for (Horario h : todos) {
            String hora;
            switch (diaActual) {
                case "lunes":     hora = h.getLunes();    break;
                case "martes":    hora = h.getMartes();   break;
                case "miércoles": hora = h.getMiercoles();break;
                case "jueves":    hora = h.getJueves();   break;
                case "viernes":   hora = h.getViernes();  break;
                default:          hora = null;            break;
            }
            if (hora != null && !hora.trim().isEmpty() && !"null".equals(hora)) {
                out.add(h);
            }
        }
        return out;
    }

    /** Exporta la lista a un archivo .xlsx en storage privado */
    private void exportarHorariosAExcel(List<Horario> lista) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Horarios_" + diaActual);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Profesor");
            header.createCell(1).setCellValue("Materia");
            header.createCell(2).setCellValue("Grupo");
            header.createCell(3).setCellValue("Horario");

            for (int i = 0; i < lista.size(); i++) {
                Horario h = lista.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(h.getNombre());
                row.createCell(1).setCellValue(h.getAsignatura());
                row.createCell(2).setCellValue(h.getGrado_grupo());

                String horario;
                switch (diaActual) {
                    case "lunes":     horario = h.getLunes();    break;
                    case "martes":    horario = h.getMartes();   break;
                    case "miércoles": horario = h.getMiercoles();break;
                    case "jueves":    horario = h.getJueves();   break;
                    case "viernes":   horario = h.getViernes();  break;
                    default:          horario = "No disponible"; break;
                }
                row.createCell(3).setCellValue(horario);
            }

            File file = new File(getExternalFilesDir(null),
                    "Horarios_" + diaActual + "_" + System.currentTimeMillis() + ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            Toast.makeText(this,
                    "Excel guardado en:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error al exportar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Guarda selección al rotar pantalla */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putInt(KEY_POS, adapter.getSelectedPosition());
            outState.putString(KEY_EST, adapter.getEstadoActual());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
