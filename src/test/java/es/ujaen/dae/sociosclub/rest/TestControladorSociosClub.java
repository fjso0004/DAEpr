package es.ujaen.dae.sociosclub.rest;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.rest.dto.*;
import es.ujaen.dae.sociosclub.servicios.ServicioProyecto;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

        var respuesta = restTemplate.postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        respuesta = restTemplate.postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var respuestaRecuperacion = restTemplate.getForEntity("/temporadas/{anio}", DTemporada.class, dTemporada.anio());
        assertThat(respuestaRecuperacion.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaRecuperacion.getBody()).isNotNull();
        assertThat(respuestaRecuperacion.getBody().anio()).isEqualTo(dTemporada.anio());
    }

    @Test
    @DirtiesContext
    void testNuevoUsuario() {
        var usuario = new DUsuario("12345678A", "Pedro", "Gómez", "Calle Real 12", "611203025", "pedro@gmail.com",
                false, "miClave12341");
        var respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var respuestaLogin = restTemplate.withBasicAuth(usuario.dni(), usuario.clave()).getForEntity(
                "/usuarios/{dni}", DUsuario.class, usuario.dni());

          //      getForEntity("/usuarios/{dni}?clave={clave}", DUsuario.class, usuario.dni(), usuario.clave());
        assertThat(respuestaLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaLogin.getBody().dni()).isEqualTo(usuario.dni());
    }

    @Test
    @DirtiesContext
    void testNuevaActividad() {
        var actividad = new DActividad(0, "Yoga", "Clase para principiantes", 10.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));

        var respuesta = restTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

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
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var actividadRespuesta = restTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadRespuesta.getBody();
        assertThat(actividadCreada).isNotNull();

        var solicitud = new DSolicitud(actividadCreada.id(), 1, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());

        var respuesta = restTemplate.postForEntity("/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DirtiesContext
    void testListarTemporadas() {
        var temporada1 = new DTemporada(2024, 0);
        var temporada2 = new DTemporada(2025, 0);

        restTemplate.postForEntity("/temporadas", temporada1, Void.class);
        restTemplate.postForEntity("/temporadas", temporada2, Void.class);

        var respuesta = restTemplate.getForEntity("/temporadas", DTemporada[].class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull().hasSize(2);
    }

    @Test
    @DirtiesContext
    void testObtenerActividad() {
        var actividad = new DActividad(0, "Zumba", "Clase energética", 12.0, 15, LocalDate.now().plusDays(3), LocalDate.now(), LocalDate.now().plusDays(7));

        var actividadRespuesta = restTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadRespuesta.getBody();
        var respuesta = restTemplate.getForEntity("/actividades/{id}", DActividad.class, actividadCreada.id());

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().tituloCorto()).isEqualTo("Zumba");
    }

    @Test
    @DirtiesContext
    void testModificarSolicitud() {
        var solicitud = new DSolicitud(0, 2, LocalDate.now(), "PENDIENTE", "12345678B", 1);
        long idSolicitud = 1;

        restTemplate.put("/solicitudes/{idSolicitud}", solicitud, idSolicitud);

        var respuesta = restTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, idSolicitud);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
    }

    /*
    @Test
    @DirtiesContext
    void testBorrarSolicitud() {
        // Crear actividad y solicitud previo al borrado
        var actividad = new DActividad(0,
                "Título de prueba", "Descripción de prueba", 20.0, 10,
                LocalDate.now().plusDays(10), LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        // Eliminar la solicitud
        //long idSolicitud = actividad.; // Obtener ID dinámico
        //restTemplate.delete("/solicitudes/{idSolicitud}", idSolicitud);

        // Validar que ya no existe
        //var respuesta = restTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, idSolicitud);
        //assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
     */

    @Test
    @DirtiesContext
    void testMarcarPagoCuota() {
        String dni = "12345678B";
        restTemplate.put("/socios/{dni}/pago", null, dni);

        var respuesta = restTemplate.getForEntity("/usuarios/{dni}", DUsuario.class, dni);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().cuotaPagada()).isTrue();
    }
}

