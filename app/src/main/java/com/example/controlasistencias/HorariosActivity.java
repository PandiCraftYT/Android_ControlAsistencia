package com.example.controlasistencias;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.R;
import com.example.controlasistencias.HorariosAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorariosActivity extends AppCompatActivity {

    private RecyclerView horariosRecyclerView;
    private HorariosAdapter horariosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        horariosRecyclerView = findViewById(R.id.horariosRecyclerView);
        horariosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String zona = getIntent().getStringExtra("zona");

        // Obtener horarios filtrados por zona
        RetrofitClient.getInstance().create(ApiService.class)
                .getHorariosPorZona(zona).enqueue(new Callback<List<Horario>>() {
                    @Override
                    public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Horario> horarios = response.body();
                            // Log para verificar la respuesta del json
                            Log.d("HorariosActivity", "Respuesta exitosa: " + horarios);
                            horariosAdapter = new HorariosAdapter(horarios);
                            horariosRecyclerView.setAdapter(horariosAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Horario>> call, Throwable t) {
                        Log.e("HorariosActivity", "Error al obtener horarios: " + t.getMessage());
                    }
                });
    }
}
