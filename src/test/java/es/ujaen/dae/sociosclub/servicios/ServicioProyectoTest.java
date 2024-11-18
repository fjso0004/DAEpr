package es.ujaen.dae.sociosclub.servicios;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = es.ujaen.dae.sociosclub.app.SociosClub.class)
@ActiveProfiles("test")
public class ServicioProyectoTest {

    @Autowired
    private ServicioProyecto servicioProyecto;

    @Test
    @DirtiesContext
    void testCrearUsuarioExitoso() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        Usuario result = servicioProyecto.crearUsuario(usuario);
        assertThat(result).isEqualTo(usuario);
    }

    @Test
    @DirtiesContext
    void testCrearUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        assertThatThrownBy(() -> servicioProyecto.crearUsuario(usuario))
                .isInstanceOf(UsuarioYaRegistrado.class);
    }

    @Test
    @DirtiesContext
    void testAsignarPlaza() {
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 10,
                LocalDate.now().plusDays(2), LocalDate.now(), LocalDate.now().plusDays(5));

        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", true);
        servicioProyecto.crearUsuario(usuario);

        Solicitudes solicitud = new Solicitudes(actividad, usuario, 0, Solicitudes.EstadoSolicitud.PENDIENTE);
        actividad.altaSolicitud(solicitud);

        servicioProyecto.asignarPlaza(solicitud);

        assertThat(solicitud.getEstado()).isEqualTo(Solicitudes.EstadoSolicitud.ACEPTADA);
        assertThat(actividad.getNumPlazas()).isEqualTo(9);
    }

    @Test
    @DirtiesContext
    void testAutenticarClaveoUsuarioIncorrecto() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        assertThatThrownBy(() -> servicioProyecto.login("12345678B", "claveIncorrecta"))
                .isInstanceOf(ClaveoUsuarioIncorrecto.class);
    }

    @Test
    @DirtiesContext
    void testCrearActividadExitoso() {
        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));

        assertThat(actividad).isNotNull();
        assertThat(actividad.getTituloCorto()).isEqualTo("titulo");
        assertThat(actividad.getNumPlazas()).isEqualTo(20);
    }

    @Test
    @DirtiesContext
    void testBuscarActividades() {
        servicioProyecto.crearActividad("titulo1", "descripcion", 10.0, 20,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(2), LocalDate.now());

        servicioProyecto.crearActividad("titulo2", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(6));

        List<Actividad> actividades = servicioProyecto.buscarActividades();

        assertThat(actividades).hasSize(1);
        assertThat(actividades.get(0).getTituloCorto()).isEqualTo("titulo2");
    }

    @Test
    @DirtiesContext
    void testCrearSolicitudActividadNoRegistrada() {
        assertThatThrownBy(() -> servicioProyecto.crearSolicitud(999L, "12345678B", 4))
                .isInstanceOf(ActividadNoRegistrada.class);
    }

    @Test
    @DirtiesContext
    void testCrearSolicitudUsuarioYaRegistrado() {
        Usuario usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        Actividad actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));

        servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 3);

        assertThatThrownBy(() -> servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 3))
                .isInstanceOf(UsuarioYaRegistrado.class);
    }
}
