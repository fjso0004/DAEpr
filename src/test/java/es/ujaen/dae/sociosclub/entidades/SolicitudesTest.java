package es.ujaen.dae.sociosclub.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudesTest {

    private Actividad actividad;
    private Usuario usuario;

    @BeforeEach
    void configInicial() {

        actividad = new Actividad("Curso de Yoga", "Clases de yoga para principiantes", 100.0, 10, LocalDate.now().plusDays(10), LocalDate.now(), LocalDate.now().plusDays(15));
        usuario = new Usuario("12345678A", "Juan Perez","ajksdak", "clave123","12334","asda", "1jaksd", true);
    }

    @Test
    void testCrearSolicitud() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 0, Solicitudes.EstadoSolicitud.PENDIENTE );

        assertNotNull(solicitud.getId(), "La solicitud debería tener un ID.");
        assertEquals(0, solicitud.getNumAcomp(), "El número de acompañantes debería ser 0 inicialmente.");
        assertEquals(Solicitudes.EstadoSolicitud.PENDIENTE, solicitud.getEstado(), "El estado de la solicitud debería ser PENDIENTE.");
        assertNotNull(solicitud.getFechaSolicitud(), "La solicitud debería tener una fecha de creación.");
        assertEquals(actividad, solicitud.getActividad(), "La actividad de la solicitud no coincide.");
        assertEquals(usuario, solicitud.getUsuario(), "El usuario de la solicitud no coincide.");
    }

    @Test
    void testSetNumAcomp_Valido() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 5, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setNumAcomp(3);

        assertEquals(3, solicitud.getNumAcomp(), "El número de acompañantes debería ser 3.");
    }

    @Test
    void testSetNumAcomp_LimiteMaximo() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 5, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setNumAcomp(5);

        assertEquals(5, solicitud.getNumAcomp(), "El número de acompañantes debería ser el límite máximo de 5.");
    }

    @Test
    void testSetNumAcomp_LimiteMinimo() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 1, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setNumAcomp(0);

        assertEquals(0, solicitud.getNumAcomp(), "El número de acompañantes debería ser el límite mínimo de 0.");
    }

    @Test
    void testSetEstado() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario,3, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);

        assertEquals(Solicitudes.EstadoSolicitud.ACEPTADA, solicitud.getEstado(), "El estado de la solicitud debería ser ACEPTADA.");
    }


    @Test
    void testSolicitudCreacionConActividadYUsuario() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 3, Solicitudes.EstadoSolicitud.PENDIENTE);

        assertEquals("Curso de Yoga", solicitud.getActividad().getTituloCorto(), "El título de la actividad no coincide.");
        assertEquals("Juan Perez", solicitud.getUsuario().getNombre(), "El nombre del usuario no coincide.");
    }

    @Test
    void testCambioEstadoConActividad() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 2, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);

        assertEquals(Solicitudes.EstadoSolicitud.ACEPTADA, solicitud.getEstado(), "El estado de la solicitud debería ser ACEPTADA.");
    }

    @Test
    void testAsignacionNumeroAcompanantesConRestriccion() {
        Solicitudes solicitud = new Solicitudes(actividad, usuario, 2, Solicitudes.EstadoSolicitud.PENDIENTE);
        solicitud.setNumAcomp(5);;
        assertEquals(5,  solicitud.getNumAcomp());
    }
}
