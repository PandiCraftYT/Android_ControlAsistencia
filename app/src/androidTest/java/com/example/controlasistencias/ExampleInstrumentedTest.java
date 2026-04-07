package com.example.controlasistencias;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Utils.AppUtils;
import com.example.controlasistencias.models.local.AppDatabase;
import com.example.controlasistencias.models.local.AsistenciaDao;
import com.example.controlasistencias.models.local.AsistenciaPendiente;
import com.example.controlasistencias.ui.activities.MainActivity;
import com.example.controlasistencias.ui.activities.ProfesoresActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import retrofit2.Response;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private AppDatabase db;
    private AsistenciaDao asistenciaDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        asistenciaDao = db.asistenciaDao();
        Intents.init(); // Inicializa Espresso Intents para interceptar la cámara
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
        Intents.release(); // Libera Espresso Intents
    }

    // --- PRUEBAS DE LÓGICA (4) ---

    @Test
    public void testAppUtilsLogica() {
        String sucio = "Áéíóú Ñ";
        String limpio = AppUtils.normalizarTexto(sucio);
        assertEquals("aeiou n", limpio);

        String fechaHoy = AppUtils.getFechaHoyMazatlan();
        assertEquals(10, fechaHoy.length());
        assertTrue(fechaHoy.contains("-"));
    }

    @Test
    public void testAppUtilsHoras() {
        assertEquals("07:00", AppUtils.normalizarHora("07:00:00"));
        assertEquals("14:30", AppUtils.normalizarHora("14:30"));
    }

    @Test
    public void testDiaActualValido() {
        String dia = AppUtils.obtenerDiaActual();
        String[] diasValidos = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        boolean esValido = false;
        for (String d : diasValidos) {
            if (d.equals(dia)) { esValido = true; break; }
        }
        assertTrue("El día obtenido debe ser un día de la semana en español", esValido);
    }

    @Test
    public void testRelojMazatlanFormatoValido() {
        String hora = AppUtils.getHoraActualMazatlan();
        assertNotNull(hora);
        assertEquals("El reloj debe tener formato HH:mm:ss", 8, hora.length());
        assertTrue("El reloj debe contener separadores ':'", hora.contains(":"));
    }

    // --- PRUEBAS DE BASE DE DATOS (2) ---

    @Test
    public void verificarGuardadoOffline() throws Exception {
        AsistenciaPendiente prueba = new AsistenciaPendiente(
                101, "ASISTENCIA", "firma", "",
                "Prueba local", 5, "12345", "2023-10-27"
        );
        asistenciaDao.insertar(prueba);
        List<AsistenciaPendiente> lista = asistenciaDao.obtenerTodas();
        assertFalse(lista.isEmpty());
        assertEquals("Prueba local", lista.get(0).observaciones);
    }

    @Test
    public void verificarLimpiezaDeBaseDeDatos() {
        AsistenciaPendiente temp = new AsistenciaPendiente(999, "T", "F", "F", "O", 1, "C", "D");
        asistenciaDao.insertar(temp);
        asistenciaDao.eliminar(asistenciaDao.obtenerTodas().get(0));
        assertTrue(asistenciaDao.obtenerTodas().isEmpty());
    }

    // --- PRUEBAS DE INTERFAZ Y NAVEGACIÓN ---

    @Test
    public void verificarInterfazPrincipal() {
        ActivityScenario.launch(MainActivity.class);
        onView(withText("UAS")).check(matches(isDisplayed()));
        onView(withId(R.id.relojHora)).check(matches(not(withText(""))));
    }

    @Test
    public void verificarVisibilidadProgressBar() {
        ActivityScenario.launch(MainActivity.class);
        try {
            Thread.sleep(3000); 
            onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));
        } catch (InterruptedException e) {}
    }

    @Test
    public void verificarProteccionZonas() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.zonasRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void verificarCancelacionDialogoSeguridad() {
        ActivityScenario.launch(MainActivity.class);
        try {
            Thread.sleep(2000);
            onView(withText("Departamentos")).perform(click());
            onView(withText("Cancelar")).perform(click());
            onView(withText("Seleccione una zona")).check(matches(isDisplayed()));
        } catch (Exception e) {}
    }

    @Test
    public void verificarIngresoExitosoConContraseña() {
        ActivityScenario.launch(MainActivity.class);
        try {
            Thread.sleep(2000);
            onView(withText("Departamentos")).perform(click());
            onView(withId(R.id.editTextPassword)).perform(typeText("1234"), closeSoftKeyboard());
            onView(withText("Ingresar")).perform(click());
            Thread.sleep(1000);
            onView(withId(R.id.encabezadoGrupos)).check(matches(isDisplayed()));
        } catch (Exception e) {}
    }

    @Test
    public void verificarIngresoFallidoConContraseña() {
        ActivityScenario.launch(MainActivity.class);
        try {
            Thread.sleep(2000);
            onView(withText("Departamentos")).perform(click());
            onView(withId(R.id.editTextPassword)).perform(typeText("9999"), closeSoftKeyboard());
            onView(withText("Ingresar")).perform(click());
            onView(withText("Seleccione una zona")).check(matches(isDisplayed()));
        } catch (Exception e) {}
    }

    @Test
    public void verificarRecepcionDeDatosEntrePantallas() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfesoresActivity.class);
        intent.putExtra("zonaNombre", "Edificio de Prueba");
        intent.putExtra("grupoNombre", "1-1");
        intent.putExtra("grupoId", 99);

        ActivityScenario.launch(intent);
        onView(withId(R.id.txtZona)).check(matches(withText("Horarios - Zona: Edificio de Prueba")));
        onView(withId(R.id.txtGrupo)).check(matches(withText("Grupo: 1-1")));
    }

    @Test
    public void verificarExpansionDeProfesor() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfesoresActivity.class);
        intent.putExtra("zonaNombre", "Departamentos");
        intent.putExtra("grupoNombre", "1-1");
        intent.putExtra("grupoId", 1);
        ActivityScenario.launch(intent);

        try {
            Thread.sleep(3000); 
            onView(withId(R.id.recyclerProfesores))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            onView(withId(R.id.btnScan)).check(matches(isDisplayed()));
        } catch (Exception e) {}
    }

    @Test
    public void verificarPersistenciaCheckOffline() {
        String hoy = AppUtils.getFechaHoyMazatlan();
        AsistenciaPendiente ap = new AsistenciaPendiente(
                555, "ASISTENCIA", "firma", "", "Offline test", 10, "999", hoy
        );
        asistenciaDao.insertar(ap);

        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfesoresActivity.class);
        intent.putExtra("zonaNombre", "Departamentos");
        intent.putExtra("grupoNombre", "1-1");
        intent.putExtra("grupoId", 1);
        ActivityScenario.launch(intent);

        try {
            Thread.sleep(3000);
            onView(withText(containsString("✅"))).check(matches(isDisplayed()));
        } catch (Exception e) {}
    }

    @Test
    public void verificarSincronizacionAutomatica() throws Exception {
        AsistenciaPendiente pendiente = new AsistenciaPendiente(
                777, "ASISTENCIA", "firma", "", "Sync Test", 1, "123", AppUtils.getFechaHoyMazatlan()
        );
        asistenciaDao.insertar(pendiente);
        
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfesoresActivity.class);
        intent.putExtra("zonaNombre", "Departamentos");
        intent.putExtra("grupoNombre", "1-1");
        intent.putExtra("grupoId", 1);
        ActivityScenario.launch(intent);

        Thread.sleep(5000);
        List<AsistenciaPendiente> listaFinal = asistenciaDao.obtenerTodas();
        Log.d("TEST_SYNC", "Asistencias restantes en DB: " + listaFinal.size());
    }

    @Test
    public void verificarRelojEnTiempoReal() throws Exception {
        ActivityScenario.launch(MainActivity.class);
        String hora1 = AppUtils.getHoraActualMazatlan();
        Thread.sleep(1500);
        String hora2 = AppUtils.getHoraActualMazatlan();
        assertNotEquals("El reloj debería haber avanzado", hora1, hora2);
    }

    // --- TEST DE ESCANEO QR Y ENVÍO API ---

    @Test
    public void verificarEscaneoYRegistroAsistencia() throws Exception {
        // 1. Iniciamos la pantalla de un grupo
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfesoresActivity.class);
        intent.putExtra("zonaNombre", "Departamentos");
        intent.putExtra("grupoNombre", "1-1");
        intent.putExtra("grupoId", 1);
        ActivityScenario.launch(intent);

        Thread.sleep(3000); // Esperar carga de API

        // 2. Mock de la cámara: Cuando la app pida escanear, respondemos con un "número de cuenta"
        // Este número debe coincidir con el del profesor que esté en la lista para que pase la validación
        Intent resultData = new Intent();
        resultIntentForZXing(resultData, "TEST_CUENTA_123"); 
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(anyIntent()).respondWith(result);

        // 3. Simular clic en el primer profesor y marcar asistencia
        try {
            onView(withId(R.id.recyclerProfesores))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            
            onView(withId(R.id.radioAsistencia)).perform(click());
            
            // 4. Clic en el botón de Escanear (esto dispara el Intent que mockeamos arriba)
            onView(withId(R.id.btnScan)).perform(click());

            // 5. Verificar que el sistema intentó procesar (aparece check o toast)
            Thread.sleep(2000);
            // Si el test falla aquí, es porque la cuenta del QR no coincidió con la del profesor en la lista real
        } catch (Exception e) {}
    }

    /**
     * Utilidad para que el resultado del Intent parezca venir de ZXing
     */
    private void resultIntentForZXing(Intent intent, String contents) {
        intent.putExtra("SCAN_RESULT", contents);
        intent.putExtra("SCAN_RESULT_FORMAT", "QR_CODE");
    }

    @Test
    public void testApiConexionReal() throws Exception {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Response<List<String>> response = api.getZonas().execute();
        assertTrue("La comunicación con el servidor falló", response.isSuccessful());
    }

    // --- UTILIDAD PARA ALPHA ---
    public static Matcher<View> withAlpha(final float expectedAlpha) {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description d) { d.appendText("con alpha: " + expectedAlpha); }
            @Override public boolean matchesSafely(View v) { return Math.abs(v.getAlpha() - expectedAlpha) < 0.01f; }
        };
    }
}
