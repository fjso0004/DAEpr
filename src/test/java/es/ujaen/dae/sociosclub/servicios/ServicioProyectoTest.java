package es.ujaen.dae.sociosclub.servicios;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ServicioProyectoTest {

    @InjectMocks
    private ServicioProyecto servicioProyecto;

    @BeforeEach
    void setUp() {
        servicioProyecto = new ServicioProyecto();
    }

 
    @Test
    public void testCrearUsuarioExitoso() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        Usuario result = servicioProyecto.crearUsuario(usuario);
        assertEquals(usuario, result);
    }

    @Test
    public void testCrearUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        assertThrows(UsuarioYaRegistrado.class, () -> servicioProyecto.crearUsuario(usuario));
    }



 
     @Test
     void testAsignarPlaza() {
         Actividad actividad = new Actividad("titulo", "descripcion", 10.0, 10, LocalDate.now().plusDays(2),
                 LocalDate.now(), LocalDate.now().plusDays(5));
         Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", true);
         Solicitudes solicitud = new Solicitudes(actividad, usuario, 0, Solicitudes.EstadoSolicitud.PENDIENTE);
 
         actividad.altaSolicitud(solicitud);
         servicioProyecto.asignarPlaza(solicitud);
 
         assertEquals(Solicitudes.EstadoSolicitud.ACEPTADA, solicitud.getEstado());
         assertEquals(9, actividad.getNumPlazas());
     }
 


    @Test
    public void testAutenticarClaveoUsuarioIncorrecto() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        assertThrows(ClaveoUsuarioIncorrecto.class, () -> servicioProyecto.login("12345678B", "claveIncorrecta"));
    }

 
    @Test
    public void testCrearActividadExitoso() {
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));
        assertNotNull(actividad);
        assertEquals("titulo", actividad.getTituloCorto());
        assertEquals(20, actividad.getNumPlazas());
    }


    @Test
    public void testBuscarActividades() {
        servicioProyecto.crearActividad("titulo1", "descripcion", 10.0, 20, LocalDate.now().minusDays(1), LocalDate.now().minusDays(2), LocalDate.now());
        servicioProyecto.crearActividad("titulo2", "descripcion", 10.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(6));
        
        List<Actividad> actividades = servicioProyecto.buscarActividades();
        assertEquals(1, actividades.size());
        assertEquals("titulo2", actividades.get(0).getTituloCorto());
    }


    @Test
    public void testCrearSolicitudActividadNoRegistrada() {
        assertThrows(ActividadNoRegistrada.class, () -> servicioProyecto.crearSolicitud(999L, "12345678B", 4));
    }


    @Test
    public void testCrearSolicitudUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));
        
        servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 3);
        assertThrows(UsuarioYaRegistrado.class, () -> servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(),3));
    }
}
