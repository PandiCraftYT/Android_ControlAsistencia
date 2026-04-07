package com.example.controlasistencias.models.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AsistenciaPendiente.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract AsistenciaDao asistenciaDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "control_asistencia_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
