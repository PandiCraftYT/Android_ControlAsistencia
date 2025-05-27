package com.example.controlasistencias.Modelos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.R;

import java.util.List;

public class ProfesorAdapter extends RecyclerView.Adapter<ProfesorAdapter.ViewHolder> {

    private Context context;
    private List<Profesor> profesores;

    public ProfesorAdapter(Context context, List<Profesor> profesores) {
        this.context = context;
        this.profesores = profesores;
    }

    @NonNull
    @Override
    public ProfesorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profesor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfesorAdapter.ViewHolder holder, int position) {
        Profesor profesor = profesores.get(position);
        holder.nombreProfesor.setText(profesor.getNombre());
    }

    @Override
    public int getItemCount() {
        return profesores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreProfesor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProfesor = itemView.findViewById(R.id.nombreProfesor);
        }
    }
}
