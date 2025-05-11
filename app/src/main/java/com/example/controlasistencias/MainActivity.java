package com.example.controlasistencias;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    private Map<String, String> contraseñasPorZona; // <- Contraseñas diferentes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        zonasRecyclerView = findViewById(R.id.zonasRecyclerView);
        relojHora = findViewById(R.id.relojHora);

        zonasRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        iniciarRelojEnVivo();
        inicializarContraseñas(); // Importantísimo

        Log.d(TAG, "Iniciando solicitud a la API para obtener zonas.");

        RetrofitClient.getInstance().create(ApiService.class).getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                Log.d(TAG, "Código de respuesta de la API: " + response.code());

                if (response.isSuccessful()) {
                    List<String> zonas = response.body();
                    Log.d(TAG, "Respuesta exitosa: " + zonas);

                    if (zonas != null) {
                        zonaAdapter = new ZonaAdapter(MainActivity.this, zonas, zonaSeleccionada -> {
                            solicitarContraseña(zonaSeleccionada);
                        });
                        zonasRecyclerView.setAdapter(zonaAdapter);
                    } else {
                        Log.d(TAG, "La respuesta está vacía (zonas es null).");
                    }
                } else {
                    Log.e(TAG, "Error al obtener zonas: Código de respuesta no exitoso: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, "Error al obtener zonas: " + t.getMessage());
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
        contraseñasPorZona.put("Gallinero", "1234");
        contraseñasPorZona.put("Edificio 1", "abcd");
        contraseñasPorZona.put("Edificio 2", "2222");
        contraseñasPorZona.put("Edificio 3", "3333");
        contraseñasPorZona.put("Edificio 4", "4444");
        contraseñasPorZona.put("Sotano", "5555");
        // Puedes agregar más zonas si quieres
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
                // Si la contraseña es correcta
                Intent intent = new Intent(MainActivity.this, HorariosActivity.class);
                intent.putExtra("zona", zonaSeleccionada);
                startActivity(intent);
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
}
