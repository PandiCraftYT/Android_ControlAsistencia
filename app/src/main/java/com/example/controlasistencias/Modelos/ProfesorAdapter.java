package com.example.controlasistencias.Modelos;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.R;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class ProfesorAdapter extends RecyclerView.Adapter<ProfesorAdapter.ViewHolder> {

    private Context context;
    private List<Profesor> listaProfesores;
    private ProfesorSeleccionado listener;

    public interface ProfesorSeleccionado {
        void onScanRequested(Profesor profesor, String tipoAsistencia, EditText campoObservacion);
        void onQRResultado(Profesor profesor, String contenidoQR);
        String getNumeroCuenta();
        Profesor getProfesor();
    }
    private List<Integer> indicesActivos = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private Set<String> registrosBloqueados = new HashSet<>();
    private Set<Integer> horariosRegistradosHoy = new HashSet<>();

    public ProfesorAdapter(Context context, List<Profesor> listaProfesores, ProfesorSeleccionado listener) {
        this.context = context;
        this.listaProfesores = listaProfesores;
        this.listener = listener;
        actualizarIndicesActivos();
    }

    /**
     * Recibe un set de llaves únicas (ej: profesorId + "_" + horarioId) para bloquear.
     */
    public void setRegistrosBloqueados(Set<String> llaves) {
        this.registrosBloqueados.clear();
        if (llaves != null) {
            this.registrosBloqueados.addAll(llaves);
        }
        Log.d("ADAPTER", "Bloqueados actualizados: " + (llaves != null ? llaves.toString() : "null"));
        notifyDataSetChanged();
    }

    public Set<String> getRegistrosBloqueados() {
        return registrosBloqueados;
    }

    /**
     * Recibe un set de IDs de horario registrados hoy para bloquear.
     */
    public void setHorariosRegistradosHoy(Set<Integer> ids) {
        this.horariosRegistradosHoy.clear();
        if (ids != null) {
            this.horariosRegistradosHoy.addAll(ids);
        }
        notifyDataSetChanged();
    }

    private void actualizarIndicesActivos() {
        indicesActivos.clear();
        LocalTime ahora = LocalTime.now();

        for (int i = 0; i < listaProfesores.size(); i++) {
            Profesor p = listaProfesores.get(i);
            try {
                LocalTime inicio = parseHoraFlexible(p.getHoraInicio());
                LocalTime fin = parseHoraFlexible(p.getHoraFin());

                if (inicio != null && fin != null && !ahora.isBefore(inicio) && !ahora.isAfter(fin)) {
                    indicesActivos.add(i);
                }
            } catch (Exception e) {
                Log.e("ADAPTER", "Error al analizar horario: " + e.getMessage());
            }
        }
    }

    private LocalTime parseHoraFlexible(String hora) {
        if (hora == null || hora.isEmpty()) return null;
        try {
            if (hora.length() == 5) return LocalTime.parse(hora);
            if (hora.length() == 8) return LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            Log.e("ADAPTER", "Error parseando hora: " + hora);
        }
        return null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String string = Normalizer.normalize(texto, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", ""); 
        return string.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private String normalizarHora(String hora) {
        if (hora == null) return "";
        if (hora.length() > 5) return hora.substring(0, 5);
        return hora;
    }

    public void iniciarActualizacionPeriodica() {
        refreshRunnable = () -> {
            actualizarIndicesActivos();
            notifyDataSetChanged();
            handler.postDelayed(refreshRunnable, 60 * 1000);
        };
        handler.post(refreshRunnable);
    }

    public void detenerActualizacion() {
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @NonNull
    @Override
    public ProfesorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profesor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profesor profesor = listaProfesores.get(position);

        holder.textNombre.setText(profesor.getNombre());
        holder.txtHorario.setText("Horario: " + profesor.getHoraInicio() + " - " + profesor.getHoraFin());
        holder.txtMateria.setText("Materia: " + profesor.getMateria());

        // --- ANALISIS DE SEGURIDAD PARA BLOQUEO ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
        String hoy = sdf.format(new Date());
        
        // Generar llave de identidad: nombre|hora|fecha
        String llaveIdentidad = normalizarTexto(profesor.getNombre()) + "|" + 
                               normalizarHora(profesor.getHoraInicio()) + "|" + hoy;

        boolean yaRegistrado = registrosBloqueados.contains(llaveIdentidad) || 
                              horariosRegistradosHoy.contains(profesor.getHorarioId());
        
        boolean estaActivo = indicesActivos.contains(position);

        // RESET UI STATE
        holder.layoutExpandable.setVisibility(View.GONE);
        holder.itemView.setAlpha(1.0f);
        holder.itemView.setEnabled(true);
        holder.radioGroup.clearCheck();
        holder.btnScan.setEnabled(false);

        if (yaRegistrado) {
            holder.itemView.setAlpha(0.6f);
            holder.textNombre.setText(profesor.getNombre() + " ✅");
            holder.layoutExpandable.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Asistencia ya registrada en sistema.", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        if (estaActivo) {
            holder.itemView.setOnClickListener(v -> {
                boolean isVisible = holder.layoutExpandable.getVisibility() == View.VISIBLE;
                holder.layoutExpandable.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            });

            LocalTime ahora = LocalTime.now();
            try {
                LocalTime inicio = parseHoraFlexible(profesor.getHoraInicio());
                if (inicio != null) {
                    long minutosPasados = Duration.between(inicio, ahora).toMinutes();
                    
                    holder.radioAsistencia.setVisibility(View.VISIBLE);
                    holder.radioRetardo.setVisibility(View.VISIBLE);
                    holder.radioFalta.setVisibility(View.VISIBLE);

                    if (minutosPasados > 10) holder.radioAsistencia.setVisibility(View.GONE);
                    if (minutosPasados > 25) holder.radioRetardo.setVisibility(View.GONE);
                }
            } catch (Exception e) {}

            holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                holder.btnScan.setEnabled(checkedId != -1);
                holder.editObservaciones.setVisibility(checkedId == R.id.radioFalta ? View.VISIBLE : View.GONE);
            });

        } else {
            holder.itemView.setAlpha(0.4f);
            holder.itemView.setEnabled(false);
            holder.itemView.setOnClickListener(null);
        }

        holder.btnScan.setOnClickListener(v -> {
            int checkedId = holder.radioGroup.getCheckedRadioButtonId();
            String tipo = null;
            if (checkedId == R.id.radioAsistencia) tipo = "ASISTENCIA";
            else if (checkedId == R.id.radioRetardo) tipo = "RETARDO";
            else if (checkedId == R.id.radioFalta) tipo = "FALTA";

            if (tipo != null) {
                listener.onScanRequested(profesor, tipo, holder.editObservaciones);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProfesores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, txtHorario, txtMateria;
        RadioGroup radioGroup;
        RadioButton radioAsistencia, radioRetardo, radioFalta;
        EditText editObservaciones;
        View btnScan;
        LinearLayout layoutExpandable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtMateria = itemView.findViewById(R.id.txtMateria);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            radioAsistencia = itemView.findViewById(R.id.radioAsistencia);
            radioRetardo = itemView.findViewById(R.id.radioRetardo);
            radioFalta = itemView.findViewById(R.id.radioFalta);
            editObservaciones = itemView.findViewById(R.id.editObservaciones);
            btnScan = itemView.findViewById(R.id.btnScan);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
        }
    }
}
