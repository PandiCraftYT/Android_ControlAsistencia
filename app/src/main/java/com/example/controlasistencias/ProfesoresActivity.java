package com.example.controlasistencias;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Profesor;
import com.example.controlasistencias.Modelos.ProfesorAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfesoresActivity extends AppCompatActivity {

    private TextView relojHora;
    private RecyclerView recyclerProfesores;
    private ProfesorAdapter profesorAdapter;
    private Handler handler = new Handler();
    private Runnable relojRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profesores);

        relojHora = findViewById(R.id.relojHora);
        recyclerProfesores = findViewById(R.id.recyclerProfesores);
        recyclerProfesores.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj();

        int grupoId = getIntent().getIntExtra("grupoId", -1);
        Log.d("ProfesoresActivity", "Recibido grupoId: " + grupoId);

        if (grupoId != -1) {
            obtenerProfesoresPorGrupo(grupoId);
        } else {
            Toast.makeText(this, "Datos de grupo no recibidos", Toast.LENGTH_LONG).show();
        }
    }

    private void obtenerProfesoresPorGrupo(int grupoId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        String url = "http://192.168.100.4:8080/api/profesores/porGrupo/" + grupoId;
        Call<List<Profesor>> call = apiService.getProfesoresPorGrupo(url);


        call.enqueue(new Callback<List<Profesor>>() {
            @Override
            public void onResponse(Call<List<Profesor>> call, Response<List<Profesor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Profesor> profesores = response.body();

                    if (!profesores.isEmpty()) {
                        profesorAdapter = new ProfesorAdapter(ProfesoresActivity.this, profesores);
                        recyclerProfesores.setAdapter(profesorAdapter);
                    } else {
                        Toast.makeText(ProfesoresActivity.this, "No hay profesores para este grupo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API Response", "Código error: " + response.code());
                    Toast.makeText(ProfesoresActivity.this, "Error en la respuesta del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Profesor>> call, Throwable t) {
                Log.e("API Error", "Error de red: " + t.getMessage());
                Toast.makeText(ProfesoresActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void iniciarReloj() {
        relojRunnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(horaActual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(relojRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(relojRunnable);
    }
}
