package com.example.controlasistencias.Modelos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.R;

import java.util.List;

public class ZonaAdapter extends RecyclerView.Adapter<ZonaAdapter.ViewHolder> {

    private Context context;
    private List<String> zonas;
    private OnZonaClickListener listener;

    public interface OnZonaClickListener {
        void onZonaClick(String zona);
    }

    public ZonaAdapter(Context context, List<String> zonas, OnZonaClickListener listener) {
        this.context = context;
        this.zonas = zonas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_zona, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String zona = zonas.get(position);
        holder.nombreZona.setText(zona);

        switch (zona.toLowerCase()) {
            case "departamento":
                holder.imagenZona.setImageResource(R.drawable.departamentos);
                break;
            case "edificio 1":
                holder.imagenZona.setImageResource(R.drawable.edificio1);
                break;
            case "edificio 2":
                holder.imagenZona.setImageResource(R.drawable.edificio2);
                break;
            case "edificio 3":
                holder.imagenZona.setImageResource(R.drawable.edificio3);
                break;
            case "edificio 4":
                holder.imagenZona.setImageResource(R.drawable.edificio4);
                break;
            case "sotano":
                holder.imagenZona.setImageResource(R.drawable.sotano);
                break;
            default:
                holder.imagenZona.setImageResource(R.drawable.uasfondo);
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onZonaClick(zona));
    }

    @Override
    public int getItemCount() {
        return zonas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreZona;
        ImageView imagenZona;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreZona = itemView.findViewById(R.id.nombreZona);
            imagenZona = itemView.findViewById(R.id.imagenZona);
        }
    }
}
