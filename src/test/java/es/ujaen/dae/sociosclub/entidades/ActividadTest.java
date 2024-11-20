package es.ujaen.dae.sociosclub.entidades;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

public class ActividadTest {
    @Test
    void testCrearActividad() {
        var actividad = new Actividad("Paseo en Bicicleta", "Recorrido por el parque", 15.0, 30,
                LocalDate.of(2024, 11, 15), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 30));

        assertThat(actividad.getTituloCorto()).isEqualTo("Paseo en Bicicleta");
        assertThat(actividad.getNumPlazas()).isEqualTo(30);
        assertThat(actividad.getPrecio()).isEqualTo(15.0);

        assertThat(actividad.getFechaInicio()).isBefore(actividad.getFechaFin());
        assertThat(actividad.getFechaFin()).isBefore(actividad.getFechaCelebracion());
    }
}
