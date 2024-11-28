package es.ujaen.dae.sociosclub.entidades;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class ActividadTest {

    private Actividad actividad;

    @BeforeEach
    public void configInicial() {
        actividad = new Actividad(
            "Yoga", 
            "Clase de Yoga para principiantes", 
            10.0, 
            2, 
            LocalDate.now().plusDays(10),
            LocalDate.now(), 
            LocalDate.now().plusDays(1)
        );
    }

    @Test
    public void testCrearActividad() {
        assertEquals("Yoga", actividad.getTituloCorto());
        assertEquals("Clase de Yoga para principiantes", actividad.getDescripcion());
        assertEquals(10.0, actividad.getPrecio());
        assertEquals(2, actividad.getNumPlazas());
        assertNotNull(actividad.getFechaCelebracion());
    }

    @Test
    public void testAgregarSolicitud() {
        Solicitudes solicitud = new Solicitudes();
        actividad.altaSolicitud(solicitud);
        assertEquals(1, actividad.getSolicitudes().size());
    }

    @Test
    public void testBorrarSolicitud() {
        Solicitudes solicitud = new Solicitudes();
        actividad.altaSolicitud(solicitud);
        actividad.borrarSolicitud(solicitud);
        assertEquals(0, actividad.getSolicitudes().size());
    }

    @Test
    public void testObtenerSolicitudesPendientes() {
        actividad.setNumPlazas(10);

        Usuario usuarioPendiente = new Usuario("12345678B", "Carlos", "Gomez", "Calle Real 123",
                "600987654", "carlos@ejemplo.com", "clave123", false);
        Usuario usuarioAprobado = new Usuario("12345678C", "Ana", "Martinez", "Calle Luna 456",
                "600123987", "ana@ejemplo.com", "clave456", true);

        Solicitudes solicitudPendiente = new Solicitudes(actividad, usuarioPendiente, 1); // 1 acompañante
        Solicitudes solicitudAprobada = new Solicitudes(actividad, usuarioAprobado, 0); // Sin acompañantes

        actividad.altaSolicitud(solicitudPendiente);
        actividad.altaSolicitud(solicitudAprobada);

        assertEquals(1, actividad.getSolicitudesPendientes().size(),
                "Debe haber solo una solicitud pendiente.");
        assertEquals(Solicitudes.EstadoSolicitud.PENDIENTE,
                actividad.getSolicitudesPendientes().get(0).getEstado(),
                "La solicitud pendiente debe estar correctamente identificada.");
    }

    @Test
    void testAltaSolicitudUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "Calle Falsa 123", "600123456", "juan@ejemplo.com", "password123", true);
        Solicitudes solicitud1 = new Solicitudes(actividad, usuario, 0);
        Solicitudes solicitud2 = new Solicitudes(actividad, usuario, 0);

        actividad.altaSolicitud(solicitud1);
        assertThrows(UsuarioYaRegistrado.class, () -> actividad.altaSolicitud(solicitud2));
    }
}
