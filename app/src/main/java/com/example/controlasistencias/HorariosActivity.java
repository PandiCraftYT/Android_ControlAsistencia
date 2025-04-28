package com.example.controlasistencias;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.HorariosAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorariosActivity extends AppCompatActivity {

    private RecyclerView horariosRecyclerView;
    private HorariosAdapter horariosAdapter;
    private TextView relojHora;
    private Handler handler = new Handler();
    private Runnable runnable;

    private String diaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        horariosRecyclerView = findViewById(R.id.horariosRecyclerView);
        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        relojHora = findViewById(R.id.relojHora);
        iniciarRelojEnVivo();

        LinearLayout btnAtras = findViewById(R.id.btnAtras);
        btnAtras.setOnClickListener(v -> finish());

        diaActual = obtenerDiaActual(); // Lunes, Martes, Miércoles, etc.

        String zona = getIntent().getStringExtra("zona");

        RetrofitClient.getInstance().create(ApiService.class)
                .getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
                    @Override
                    public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Horario> horariosOriginales = response.body();
                            List<Horario> horariosFiltrados = filtrarPorDia(horariosOriginales);

                            if (horariosFiltrados.isEmpty()) {
                                Toast.makeText(HorariosActivity.this, "Hoy no hay clases para mostrar.", Toast.LENGTH_LONG).show();
                            }

                            horariosAdapter = new HorariosAdapter(horariosFiltrados, diaActual);
                            horariosRecyclerView.setAdapter(horariosAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Horario>> call, Throwable t) {
                        Log.e("HorariosActivity", "Error al obtener horarios: " + t.getMessage());
                    }
                });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (relojHora != null) {
                    String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    relojHora.setText(horaActual);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private String obtenerDiaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        return sdf.format(new Date());
    }

    private List<Horario> filtrarPorDia(List<Horario> lista) {
        List<Horario> filtrados = new ArrayList<>();
        for (Horario horario : lista) {
            switch (diaActual.toLowerCase()) {
                case "lunes":
                    if (horario.getLunes() != null && !horario.getLunes().isEmpty()) {
                        filtrados.add(horario);
                    }
                    break;
                case "martes":
                    if (horario.getMartes() != null && !horario.getMartes().isEmpty()) {
                        filtrados.add(horario);
                    }
                    break;
                case "miércoles":
                    if (horario.getMiercoles() != null && !horario.getMiercoles().isEmpty()) {
                        filtrados.add(horario);
                    }
                    break;
                case "jueves":
                    if (horario.getJueves() != null && !horario.getJueves().isEmpty()) {
                        filtrados.add(horario);
                    }
                    break;
                case "viernes":
                    if (horario.getViernes() != null && !horario.getViernes().isEmpty()) {
                        filtrados.add(horario);
                    }
                    break;
            }
        }
        return filtrados;
    }
}
