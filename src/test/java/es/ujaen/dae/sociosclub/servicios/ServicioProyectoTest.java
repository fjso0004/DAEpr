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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = es.ujaen.dae.sociosclub.app.SociosClub.class)
@ActiveProfiles("test")
public class ServicioProyectoTest {

    @Autowired
    private ServicioProyecto servicioProyecto;

    @Test
    @DirtiesContext
    void testCrearUsuarioExitoso() {
        var usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        var result = servicioProyecto.crearUsuario(usuario);
        assertThat(result).isEqualTo(usuario);
    }

    @Test
    @DirtiesContext
    void testCrearUsuarioYaRegistrado() {
        var usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        assertThatThrownBy(() -> servicioProyecto.crearUsuario(usuario))
                .isInstanceOf(UsuarioYaRegistrado.class);
    }

    @Test
    @DirtiesContext
    void testAsignarPlaza() {
        var actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 10,
                LocalDate.now().plusDays(2), LocalDate.now(), LocalDate.now().plusDays(5));

        var usuario = servicioProyecto.crearUsuario(new Usuario("12345678B", "nombre", "apellido",
                "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", true));

        servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 0);


        assertThat(actividad.getSolicitudes()).hasSize(1);
        assertThat(actividad.getNumPlazas()).isEqualTo(9);
    }

    @Test
    @DirtiesContext
    void testAutenticarClaveoUsuarioIncorrecto() {
        var usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        assertThatThrownBy(() -> servicioProyecto.login("12345678B", "claveIncorrecta"))
                .isInstanceOf(ClaveoUsuarioIncorrecto.class);
    }

    @Test
    @DirtiesContext
    void testCrearActividadExitoso() {
        var actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));

        assertThat(actividad).isNotNull();
        assertThat(actividad.getTituloCorto()).isEqualTo("titulo");
        assertThat(actividad.getNumPlazas()).isEqualTo(20);
        assertThat(actividad.getTemporada()).isNotNull(); // Verifica que la temporada no es null
        assertThat(actividad.getTemporada().getAnio()).isEqualTo(LocalDate.now().plusDays(1).getYear());
    }

    @Test
    @DirtiesContext
    void testBuscarActividades() {
        servicioProyecto.crearActividad("titulo1", "descripcion", 10.0, 20,
                LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(2));

        servicioProyecto.crearActividad("titulo2", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(6));

        var actividades = servicioProyecto.buscarActividades();

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
        var usuario = new Usuario("12345678B", "nombre", "apellido", "Calle Falsa 123", "600000000", "email@domain.com", "claveSegura", false);
        servicioProyecto.crearUsuario(usuario);

        var actividad = servicioProyecto.crearActividad("titulo", "descripcion", 10.0, 20,
                LocalDate.now().plusDays(1), LocalDate.now(), LocalDate.now().plusDays(2));

        servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 3);
        assertThatThrownBy(() -> servicioProyecto.crearSolicitud(actividad.getId(), usuario.getDni(), 3))
                .isInstanceOf(UsuarioYaRegistrado.class);
    }
}

