package es.ujaen.dae.sociosclub.entidades;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

public class TemporadaTest {

    @Test
    void testCrearTemporada() {
        var temporada = new Temporada(2024);
        assertThat(temporada.getAnio()).isEqualTo(2024);
        assertThat(temporada.getActividades()).isEmpty();
    }

    @Test
    void testAgregarActividad() {
        var temporada = new Temporada(2024);

        var actividad = new Actividad("Visita al Museo", "Visita guiada al museo de la ciudad", 20.0, 15,
                LocalDate.of(2024, 11, 15), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 30));

        temporada.agregarActividad(actividad);

        List<Actividad> actividades = temporada.getActividades();
        assertThat(actividades).hasSize(1);
        assertThat(actividades).contains(actividad);

        assertThat(actividad.getTemporada()).isEqualTo(temporada);
    }

    @Test
    void testModificarAnio() {
        var temporada = new Temporada(2023);
        temporada.setAnio(2025);
        assertThat(temporada.getAnio()).isEqualTo(2025);
    }
}
