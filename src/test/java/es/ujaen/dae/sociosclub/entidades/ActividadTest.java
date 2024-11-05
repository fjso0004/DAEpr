package es.ujaen.dae.sociosclub.entidades;
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
    public void testAgregarSocioExitosamente() {
        Usuario usuario1 = new Usuario();
        actividad.nuevoSocio(usuario1);
        assertEquals(1, actividad.getSocios().size());
        assertEquals(1, actividad.getNumPlazas());
    }

    @Test
    public void testAgregarSocioSinPlazasDisponibles() {
        Usuario usuario1 = new Usuario();
        Usuario usuario2 = new Usuario();
        Usuario usuario3 = new Usuario();

        actividad.nuevoSocio(usuario1);
        actividad.nuevoSocio(usuario2);
        actividad.nuevoSocio(usuario3);  

        assertEquals(2, actividad.getSocios().size());  
        assertEquals(0, actividad.getNumPlazas());      
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
        Solicitudes solicitudPendiente = new Solicitudes();
        solicitudPendiente.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);

        Solicitudes solicitudAprobada = new Solicitudes();
        solicitudAprobada.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);

        actividad.altaSolicitud(solicitudPendiente);
        actividad.altaSolicitud(solicitudAprobada);
        assertEquals(1, actividad.getSolicitudesPendientes().size());
    }
}
