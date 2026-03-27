package com.example.controlasistencias;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Horario;

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
    private static final String TAG = "HorariosActivity";

    private RecyclerView horariosRecyclerView;
    private HorariosAdapter adapter;

    private View btnAtras;
    private TextView relojHora;
    private TextView textViewZonaGrupo;
    private Handler handler = new Handler();
    private Runnable runnable;
    private String diaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        horariosRecyclerView = findViewById(R.id.horariosRecyclerView);
        btnAtras = findViewById(R.id.btnAtras);
        relojHora = findViewById(R.id.relojHora);
        textViewZonaGrupo = findViewById(R.id.textViewZonaGrupo);

        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAtras.setOnClickListener(v -> finish());

        iniciarRelojEnVivo();

        diaActual = obtenerDiaActual();

        String zona = getIntent().getStringExtra("zona");
        String grupo = getIntent().getStringExtra("grupo");

        if (textViewZonaGrupo != null) {
            textViewZonaGrupo.setText("Horarios - Zona: " + zona + "    Grupo: " + grupo);
        }

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Horario> todosLosHorarios = response.body();
                    
                    // Filtrar por ZONA, GRUPO y por DÍA ACTUAL
                    List<Horario> filtrados = filtrarHorarios(todosLosHorarios, grupo, zona);
                    
                    if (filtrados.isEmpty()) {
                        Toast.makeText(HorariosActivity.this, "No hay clases programadas para hoy en este grupo.", Toast.LENGTH_LONG).show();
                    }
                    
                    adapter = new HorariosAdapter(filtrados, diaActual, HorariosActivity.this);
                    horariosRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(HorariosActivity.this, "Error al obtener horarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Horario>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(HorariosActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(hora);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private String obtenerDiaActual() {
        String[] dias = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        int idx = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        return dias[idx];
    }

    private List<Horario> filtrarHorarios(List<Horario> todos, String grupoSeleccionado, String zonaSeleccionada) {
        List<Horario> out = new ArrayList<>();
        for (Horario h : todos) {
            // 1. Filtrar por Zona (MUY IMPORTANTE para evitar maestros de otros edificios)
            boolean esMismaZona = h.getZona() != null && 
                                 h.getZona().trim().equalsIgnoreCase(zonaSeleccionada.trim());
            
            if (!esMismaZona) continue;

            // 2. Filtrar por Grupo
            boolean esMismoGrupo = h.getGrado_grupo() != null && 
                                  h.getGrado_grupo().trim().equalsIgnoreCase(grupoSeleccionado.trim());
            
            if (!esMismoGrupo) continue;

            // 3. Filtrar por Día Actual
            String valorDia;
            switch (diaActual.toLowerCase()) {
                case "lunes":     valorDia = h.getLunes();    break;
                case "martes":    valorDia = h.getMartes();   break;
                case "miércoles": valorDia = h.getMiercoles();break;
                case "jueves":    valorDia = h.getJueves();   break;
                case "viernes":   valorDia = h.getViernes();  break;
                default:          valorDia = null;            break;
            }

            if (valorDia != null && !valorDia.trim().isEmpty() && !"null".equalsIgnoreCase(valorDia)) {
                out.add(h);
            }
        }
        return out;
    }

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