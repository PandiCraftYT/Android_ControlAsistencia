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
        btnAtras            = findViewById(R.id.btnAtras);
        relojHora           = findViewById(R.id.relojHora);

        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3) Botón atrás
        btnAtras.setOnClickListener(v -> finish());

        // 4) Iniciar reloj
        iniciarRelojEnVivo();

        // 5) Día actual para filtrar
        diaActual = obtenerDiaActual();

        // 6) Petición de datos
        String zona = getIntent().getStringExtra("zona");
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                // tu código aquí
            }

            @Override
            public void onFailure(Call<List<Horario>> call, Throwable t) {
                // tu código aquí
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
