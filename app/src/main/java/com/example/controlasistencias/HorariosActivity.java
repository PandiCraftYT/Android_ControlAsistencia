package com.example.controlasistencias;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorariosActivity extends AppCompatActivity {

    private RecyclerView horariosRecyclerView;
    private HorariosAdapter horariosAdapter;
    private ImageView btnExportar;
    private View btnAtras;
    private TextView relojHora;
    private Handler handler = new Handler();
    private Runnable runnable;
    private String diaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        horariosRecyclerView = findViewById(R.id.horariosRecyclerView);
        btnExportar = findViewById(R.id.btnExportarExcel);
        btnAtras = findViewById(R.id.btnAtras);
        relojHora = findViewById(R.id.relojHora);

        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 1);

        iniciarRelojEnVivo();
        diaActual = obtenerDiaActual();

        btnAtras.setOnClickListener(v -> finish());

        String zona = getIntent().getStringExtra("zona");

        RetrofitClient.getInstance().create(ApiService.class)
                .getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
                    @Override
                    public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Horario> horarios = filtrarHorariosDelDia(response.body());
                            horariosAdapter = new HorariosAdapter(horarios, diaActual);
                            horariosRecyclerView.setAdapter(horariosAdapter);

                            btnExportar.setOnClickListener(v -> exportarHorariosAExcel(horarios));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Horario>> call, Throwable t) {
                        Toast.makeText(HorariosActivity.this, "Error al obtener horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = java.text.DateFormat.getTimeInstance(java.text.DateFormat.MEDIUM).format(new java.util.Date());
                relojHora.setText(horaActual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private String obtenerDiaActual() {
        String[] dias = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        Calendar calendar = Calendar.getInstance();
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);
        return dias[diaSemana - 1];
    }

    private List<Horario> filtrarHorariosDelDia(List<Horario> lista) {
        List<Horario> filtrados = new ArrayList<>();
        for (Horario h : lista) {
            String horaDia = "";
            switch (diaActual) {
                case "lunes": horaDia = h.getLunes(); break;
                case "martes": horaDia = h.getMartes(); break;
                case "miércoles": horaDia = h.getMiercoles(); break;
                case "jueves": horaDia = h.getJueves(); break;
                case "viernes": horaDia = h.getViernes(); break;
            }
            if (horaDia != null && !horaDia.trim().isEmpty() && !horaDia.equals("null")) {
                filtrados.add(h);
            }
        }
        return filtrados;
    }

    private void exportarHorariosAExcel(List<Horario> listaHorarios) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Horarios del Día");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Profesor");
            header.createCell(1).setCellValue("Materia");
            header.createCell(2).setCellValue("Grupo");
            header.createCell(3).setCellValue("Horario");

            for (int i = 0; i < listaHorarios.size(); i++) {
                Horario h = listaHorarios.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(h.getNombre());
                row.createCell(1).setCellValue(h.getAsignatura());
                row.createCell(2).setCellValue(h.getGrado_grupo());

                String horario = "";
                switch (diaActual) {
                    case "lunes": horario = h.getLunes(); break;
                    case "martes": horario = h.getMartes(); break;
                    case "miércoles": horario = h.getMiercoles(); break;
                    case "jueves": horario = h.getJueves(); break;
                    case "viernes": horario = h.getViernes(); break;
                }
                row.createCell(3).setCellValue(horario != null ? horario : "No disponible");
            }

            File file = new File(getExternalFilesDir(null), "HorariosDia_" + System.currentTimeMillis() + ".xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            Toast.makeText(this, "Excel creado:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
