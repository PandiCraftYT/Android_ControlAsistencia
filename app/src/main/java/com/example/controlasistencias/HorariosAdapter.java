package com.example.controlasistencias;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Modelos.Horario;

import java.util.List;

public class HorariosAdapter extends RecyclerView.Adapter<HorariosAdapter.HorarioViewHolder> {

    private List<Horario> horarios;
    private String diaActual;

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
        switch (diaActual.toLowerCase()) {
            case "lunes":
                holder.hora.setText(horario.getLunes());
                break;
            case "martes":
                holder.hora.setText(horario.getMartes());
                break;
            case "miércoles":
                holder.hora.setText(horario.getMiercoles());
                break;
            case "jueves":
                holder.hora.setText(horario.getJueves());
                break;
            case "viernes":
                holder.hora.setText(horario.getViernes());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

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
