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

    public HorariosAdapter(List<Horario> horarios) {
        this.horarios = horarios;
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
        holder.grupo.setText("Grado y Grupo: " + horario.getGrado_grupo());
        holder.hora.setText("Horas Asignadas: " + horario.getHora());
        holder.turno.setText("Turno: " + horario.getTurno());
        holder.zona.setText("Zona: " + horario.getZona());

        //log para verificar los datos
        System.out.println("Nombre: " + horario.getNombre());
        System.out.println("Asignatura: " + horario.getAsignatura());
        System.out.println("Grupo: " + horario.getGrado_grupo());
        System.out.println("Hora: " + horario.getHora());
        System.out.println("Turno: " + horario.getTurno());
        System.out.println("Zona: " + horario.getZona());


        // Asegurarse de mostrar todos los días
        holder.lunes.setText("Lunes: " + (horario.getLunes().isEmpty() ? "No disponible" : horario.getLunes()));
        holder.martes.setText("Martes: " + (horario.getMartes().isEmpty() ? "No disponible" : horario.getMartes()));
        holder.miercoles.setText("Miércoles: " + (horario.getMiercoles().isEmpty() ? "No disponible" : horario.getMiercoles()));
        holder.jueves.setText("Jueves: " + (horario.getJueves().isEmpty() ? "No disponible" : horario.getJueves()));
        holder.viernes.setText("Viernes: " + (horario.getViernes().isEmpty() ? "No disponible" : horario.getViernes()));
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    public static class HorarioViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, asignatura, grupo, hora, turno, lunes, martes, miercoles, jueves, viernes, zona;

        public HorarioViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.horario_nombre);
            asignatura = itemView.findViewById(R.id.horario_asignatura);
            grupo = itemView.findViewById(R.id.horario_grupo);
            hora = itemView.findViewById(R.id.horario_hora);
            turno = itemView.findViewById(R.id.horario_turno);
            lunes = itemView.findViewById(R.id.horario_lunes);
            martes = itemView.findViewById(R.id.horario_martes);
            miercoles = itemView.findViewById(R.id.horario_miercoles);
            jueves = itemView.findViewById(R.id.horario_jueves);
            viernes = itemView.findViewById(R.id.horario_viernes);
            zona = itemView.findViewById(R.id.horario_zona);
        }
    }
}
