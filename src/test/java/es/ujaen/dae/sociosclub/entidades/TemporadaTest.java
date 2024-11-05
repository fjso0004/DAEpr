package es.ujaen.dae.sociosclub.entidades;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class TemporadaTest {

    private Temporada temporada;
    private Actividad actividad;

    @BeforeEach
    void configInicial() {
        temporada = new Temporada(2024);
        actividad = new Actividad("Clase de Baile", "Curso de salsa para principiantes", 50.0, 20, 
                                  LocalDate.now().plusDays(10), LocalDate.now(), LocalDate.now().plusDays(15));
    }

    @Test
    void testCrearTemporada() {
        assertEquals(2024, temporada.getAnio(), "El año de la temporada debería ser 2024.");
        assertTrue(temporada.getActividades().isEmpty(), "La temporada debería comenzar sin actividades.");
    }

    @Test
    void testAgregarActividad() {
        temporada.agregarActividad(actividad);

        List<Actividad> actividades = temporada.getActividades();
        assertEquals(1, actividades.size(), "Debería haber una actividad en la temporada.");
        assertEquals(actividad, actividades.get(0), "La actividad agregada no coincide.");
        assertEquals(temporada, actividad.getTemporada(), "La actividad debería estar asignada a la temporada.");
    }

    @Test
    void testSetAnio() {
        temporada.setAnio(2025);
        assertEquals(2025, temporada.getAnio(), "El anio de la temporada debería ser 2025.");
    }

    @Test
    void testSetActividades() {
        List<Actividad> nuevasActividades = List.of(new Actividad("Yoga", "Clase de yoga avanzada", 30.0, 15, 
                                                                 LocalDate.now().plusDays(20), LocalDate.now(), LocalDate.now().plusDays(25)));
        temporada.setActividades(nuevasActividades);

        assertEquals(1, temporada.getActividades().size(), "La lista de actividades debería tener una actividad.");
        assertEquals("Yoga", temporada.getActividades().get(0).getTituloCorto(), "La actividad debería ser la clase de yoga.");
    }
}
