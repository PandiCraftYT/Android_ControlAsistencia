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

public class ZonaAdapter extends RecyclerView.Adapter<ZonaAdapter.ViewHolder> {

    private Context context;
    private List<String> zonas;
    private OnZonaClickListener listener;

    public interface OnZonaClickListener {
        void onZonaClick(String zonaSeleccionada);
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

        holder.itemView.setOnClickListener(v -> {
            listener.onZonaClick(zona);
        });
    }

    @Override
    public int getItemCount() {
        return zonas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreZona;

        public ViewHolder(View itemView) {
            super(itemView);
            nombreZona = itemView.findViewById(R.id.nombreZona); // Asegúrate que este ID esté en tu item_zona.xml
        }
    }
}
