package es.ujaen.dae.sociosclub.servicios;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioProyectoTest {

    @InjectMocks
    private ServicioProyecto servicioProyecto;

    @BeforeEach
    void setUp() {
        servicioProyecto = new ServicioProyecto();
    }

    // Test para crearUsuario: Caso exitoso
    @Test
    public void testCrearUsuarioExitoso() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        Usuario result = servicioProyecto.crearUsuario(usuario);
        assertEquals(usuario, result);
    }

    // Test para crearUsuario: Usuario ya registrado
    @Test
    public void testCrearUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        assertThrows(UsuarioYaRegistrado.class, () -> servicioProyecto.crearUsuario(usuario));
    }

    // Test para autenticar: Caso exitoso
    @Test
    public void testAutenticarUsuarioExitoso() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        Usuario result = servicioProyecto.autenticar("12345678B", "clave");
        assertEquals(usuario, result);
    }

    // Test para autenticar: Clave incorrecta
    @Test
    public void testAutenticarClaveIncorrecta() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        assertThrows(ClaveIncorrecta.class, () -> servicioProyecto.autenticar("12345678B", "claveIncorrecta"));
    }

    // Test para crearActividad: Caso exitoso
    @Test
    public void testCrearActividadExitoso() {
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));
        assertNotNull(actividad);
        assertEquals("titulo", actividad.getTituloCorto());
        assertEquals(20, actividad.getNumPlazas());
    }

    // Test para buscarActividades: Verificar que solo devuelve actividades futuras
    @Test
    public void testBuscarActividades() {
        // Crear actividades pasadas y futuras
        servicioProyecto.crearActividad("titulo1", "descripcion", 10.0, 20, LocalDate.now().minusDays(1), LocalDate.now().minusDays(2), LocalDate.now());
        servicioProyecto.crearActividad("titulo2", "descripcion", 10.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(6));
        
        List<Actividad> actividades = servicioProyecto.buscarActividades();
        assertEquals(1, actividades.size());
        assertEquals("titulo2", actividades.get(0).getTituloCorto());
    }

    // Test para crearSolicitud: Actividad no registrada
    @Test
    public void testCrearSolicitudActividadNoRegistrada() {
        assertThrows(ActividadNoRegistrada.class, () -> servicioProyecto.crearSolicitud(999L, "12345678B"));
    }

    // Test para crearSolicitud: Usuario ya registrado en la actividad
    @Test
    public void testCrearSolicitudUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "apellido2", "600000000", "email@domain.com", "clave", false);
        servicioProyecto.crearUsuario(usuario);
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));
        
        servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni());
        assertThrows(UsuarioYaRegistrado.class, () -> servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni()));
    }
}
