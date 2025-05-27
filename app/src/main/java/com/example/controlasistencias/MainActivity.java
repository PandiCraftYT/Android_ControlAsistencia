package com.example.controlasistencias;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.ZonaAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView zonasRecyclerView;
    private ZonaAdapter zonaAdapter;
    private TextView relojHora;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final String TAG = "MainActivity";

    private Map<String, String> contraseñasPorZona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        zonasRecyclerView = findViewById(R.id.zonasRecyclerView);
        relojHora = findViewById(R.id.relojHora);

        zonasRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        iniciarRelojEnVivo();
        inicializarContraseñas();

        Log.d(TAG, "Iniciando solicitud a la API para obtener zonas.");

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> zonas = response.body();

                    zonaAdapter = new ZonaAdapter(MainActivity.this, zonas, zonaSeleccionada -> {
                        solicitarContraseña(zonaSeleccionada);
                    });

                    zonasRecyclerView.setAdapter(zonaAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "Error en la respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fallo en conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(horaActual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void inicializarContraseñas() {
        contraseñasPorZona = new HashMap<>();
        contraseñasPorZona.put("Departamentos", "1234");
        contraseñasPorZona.put("Edificio 1", "abcd");
        contraseñasPorZona.put("Edificio 2", "2222");
        contraseñasPorZona.put("Edificio 3", "3333");
        contraseñasPorZona.put("Edificio 4", "4444");
        contraseñasPorZona.put("Sotano", "5555");
    }

    private void solicitarContraseña(String zonaSeleccionada) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acceso restringido");
        builder.setMessage("Ingresa la contraseña para: " + zonaSeleccionada);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Contraseña");
        input.setPadding(40, 30, 40, 30);

        builder.setView(input);

        builder.setPositiveButton("Ingresar", (dialog, which) -> {
            String contraseñaIngresada = input.getText().toString().trim();
            if (verificarContraseña(zonaSeleccionada, contraseñaIngresada)) {
                int zonaId = obtenerIdZonaDesdeNombre(zonaSeleccionada);
                if (zonaId != -1) {
                    Intent intent = new Intent(MainActivity.this, com.example.controlasistencias.GruposActivity.class);
                    intent.putExtra("zonaNombre", zonaSeleccionada);

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Zona no reconocida.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_700));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
    }

    private boolean verificarContraseña(String zona, String contraseñaIngresada) {
        String contraseñaCorrecta = contraseñasPorZona.get(zona);
        return contraseñaCorrecta != null && contraseñaCorrecta.equals(contraseñaIngresada);
    }

    private int obtenerIdZonaDesdeNombre(String zonaNombre) {
        switch (zonaNombre.toLowerCase()) {
            case "departamentos": return 1;
            case "edificio 1": return 2;
            case "edificio 2": return 3;
            case "edificio 3": return 4;
            case "edificio 4": return 5;
            case "sotano": return 6;
            default: return -1;
        }
    }
}
