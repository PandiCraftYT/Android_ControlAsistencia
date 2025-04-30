package com.example.controlasistencias;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.R;

import java.util.List;

public class HorariosAdapter extends RecyclerView.Adapter<HorariosAdapter.HorarioViewHolder> {

    private List<Horario> horarios;
    private String diaActual;

    // Constructor actualizado con el día actual
    public HorariosAdapter(List<Horario> horarios, String diaActual) {
        this.horarios = horarios;
        this.diaActual = diaActual;
    }

    @Override
    public HorarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HorarioViewHolder holder, int position) {
        Horario horario = horarios.get(position);

        holder.nombre.setText(horario.getNombre());
        holder.asignatura.setText(horario.getAsignatura());
        holder.grupo.setText(horario.getGrado_grupo());

        // Mostrar solo el horario del día actual
        String horaDia = "";
        switch (diaActual.toLowerCase()) {
            case "lunes":
                horaDia = horario.getLunes();
                break;
            case "martes":
                horaDia = horario.getMartes();
                break;
            case "miércoles":
            case "miercoles":  // por si viene sin tilde
                horaDia = horario.getMiercoles();
                break;
            case "jueves":
                horaDia = horario.getJueves();
                break;
            case "viernes":
                horaDia = horario.getViernes();
                break;
        }

        if (horaDia != null && !horaDia.trim().isEmpty()) {
            holder.hora.setText(horaDia);
        } else {
            holder.hora.setText("No disponible");
        }
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    // ViewHolder
    public static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, asignatura, grupo, hora;

        public HorarioViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.horario_nombre);
            asignatura = itemView.findViewById(R.id.horario_asignatura);
            grupo = itemView.findViewById(R.id.horario_grupo);
            hora = itemView.findViewById(R.id.horario_hora);
        }
    }
}
