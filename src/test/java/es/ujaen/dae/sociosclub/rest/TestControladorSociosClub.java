package es.ujaen.dae.sociosclub.rest;
import es.ujaen.dae.sociosclub.rest.dto.*;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = es.ujaen.dae.sociosclub.app.SociosClub.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class TestControladorSociosClub {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    @PostConstruct
    void crearRestTemplate() {
        var restTemplateBuilder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + localPort + "/sociosclub");
        restTemplate = new TestRestTemplate(restTemplateBuilder);
    }



    @Test
    @DirtiesContext
    public void testNuevaTemporada() {
        var dTemporada = new DTemporada(2026, 0); // Solo incluye el año

        // Crear una nueva temporada
        var respuesta = restTemplate.postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Intentar crear una temporada con el mismo año
        respuesta = restTemplate.postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Validar que la temporada puede ser recuperada
        var respuestaRecuperacion = restTemplate.getForEntity("/temporadas/{anio}", DTemporada.class, dTemporada.anio());
        assertThat(respuestaRecuperacion.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaRecuperacion.getBody()).isNotNull();
        assertThat(respuestaRecuperacion.getBody().anio()).isEqualTo(dTemporada.anio());
    }




    @Test
    @DirtiesContext
    void testNuevoUsuario() {
        var usuario = new DUsuario("12345678A", "Pedro", "Gómez", "Calle Real 12", "611203025", "pedro@gmail.com", false, "miClave12341");

        // Crear un nuevo usuario
        var respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Intentar crear un usuario con el mismo DNI
        respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Validar que el usuario puede ser recuperado
        var respuestaLogin = restTemplate.getForEntity("/usuarios/{dni}?clave={clave}", DUsuario.class, usuario.dni(), usuario.clave());
        assertThat(respuestaLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaLogin.getBody()).isEqualTo(usuario);
    }

    @Test
    @DirtiesContext
    void testNuevaActividad() {
        var actividad = new DActividad(0, "Yoga", "Clase para principiantes", 10.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));

        // Crear una nueva actividad
        var respuesta = restTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Obtener la lista de actividades y validar que contiene la actividad creada
        var actividades = restTemplate.getForEntity("/actividades", DActividad[].class);
        assertThat(actividades.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividades.getBody()).isNotNull().hasSize(1);
        assertThat(actividades.getBody()[0].tituloCorto()).isEqualTo("Yoga");
    }

    @Test
    @DirtiesContext
    void testNuevaSolicitud() {
        var usuario = new DUsuario("12345678B", "Ana", "López", "Calle Luna 34", "611301025", "ana@gmail.com", false, "suClave1234");
        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10, LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));

        // Crear usuario y actividad
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        var actividadRespuesta = restTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Obtener la actividad para usar su ID
        var actividadCreada = actividadRespuesta.getBody();
        assertThat(actividadCreada).isNotNull();

        // Crear solicitud para la actividad
        var solicitud = new DSolicitud(actividadCreada.id(), 1, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());

        // Enviar la solicitud con el cuerpo correcto
        var respuesta = restTemplate.postForEntity("/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }


    @Test
    @DirtiesContext
    void testListarTemporadas() {
        var temporada1 = new DTemporada(2024, 0);
        var temporada2 = new DTemporada(2025, 0);

        // Crear temporadas
        restTemplate.postForEntity("/temporadas", temporada1, Void.class);
        restTemplate.postForEntity("/temporadas", temporada2, Void.class);

        // Listar temporadas
        var respuesta = restTemplate.getForEntity("/temporadas", DTemporada[].class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull().hasSize(2);
    }
}

