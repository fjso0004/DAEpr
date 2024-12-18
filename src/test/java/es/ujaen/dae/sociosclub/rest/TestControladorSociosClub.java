package es.ujaen.dae.sociosclub.rest;
import es.ujaen.dae.sociosclub.rest.dto.*;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    TestRestTemplate adminRestTemplate;


    @PostConstruct
    void crearRestTemplate() {
        var restTemplateBuilder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + localPort + "/sociosclub");
        restTemplate = new TestRestTemplate(restTemplateBuilder);


        // Registrar un administrador predeterminado
        var admin = new DUsuario("12345678Z", "Admin", "Default", "Calle Principal 1", "600111111",
                "admin@sociosclub.com", true, "superUser");
        var respuestaAdmin = restTemplate.postForEntity("/usuarios", admin, Void.class);
        if (!respuestaAdmin.getStatusCode().equals(HttpStatus.CREATED) &&
                !respuestaAdmin.getStatusCode().equals(HttpStatus.CONFLICT)) {
            throw new RuntimeException("No se pudo configurar el administrador predeterminado");
        }

        // Crear un restTemplate con autenticación de administrador
        adminRestTemplate = restTemplate.withBasicAuth(admin.dni(), admin.clave());


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
    void testNuevaActividadComoAdmin() {

        var actividad = new DActividad(0, "Yoga", "Clase para principiantes", 10.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));

        var respuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Validar que la actividad fue creada correctamente
        var actividades = adminRestTemplate.getForEntity("/actividades", DActividad[].class);
        assertThat(actividades.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividades.getBody()).isNotNull().hasSize(1);
        assertThat(actividades.getBody()[0].tituloCorto()).isEqualTo("Yoga");
    }


    @Test
    @DirtiesContext
    void testNuevaSolicitud() {
        // Registrar un nuevo usuario estándar
        var usuario = new DUsuario("12345678B", "Ana", "López", "Calle Luna 34", "611301025", "ana@gmail.com", false, "suClave1234");
        var registroRespuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Crear actividad como administrador
        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10, LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadRespuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadRespuesta.getBody();
        assertThat(actividadCreada).isNotNull();

        // Realizar una solicitud autenticado como usuario
        var userAuthenticatedTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var solicitud = new DSolicitud(actividadCreada.id(), 1, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());
        var respuesta = userAuthenticatedTemplate.postForEntity("/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
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

        var actividadRespuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadRespuesta.getBody();
        var respuesta = adminRestTemplate.getForEntity("/actividades/{id}", DActividad.class, actividadCreada.id());

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().tituloCorto()).isEqualTo("Zumba");
    }

    @Test
    @DirtiesContext
    void testModificarSolicitud() {
        var usuario = new DUsuario("12345678B", "Ana", "López", "Calle Luna 34", "611301025", "ana@gmail.com", false, "suClave1234");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10, LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadResponse = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadResponse.getBody();
        assertThat(actividadCreada).isNotNull();

        var userAuthenticatedTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var solicitud = new DSolicitud(0, 1, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());
        var solicitudResponse = userAuthenticatedTemplate.postForEntity(
                "/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
        assertThat(solicitudResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var idSolicitud = 1;
        var putResponse = adminRestTemplate.exchange(
                "/solicitudes/" + idSolicitud + "?idActividad=" + actividadCreada.id() + "&numAcomp=2",
                HttpMethod.PUT, null, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        var solicitudModificadaResponse = adminRestTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, idSolicitud);
        assertThat(solicitudModificadaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(solicitudModificadaResponse.getBody()).isNotNull();
        assertThat(solicitudModificadaResponse.getBody().numAcomp()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void testBorrarSolicitud() {
        var usuario = new DUsuario("12345678B", "nombre", "apellido", "Calle Falsa 123",
                "600000000", "email@domain.com", false, "claveSegura");
        ResponseEntity<Void> usuarioResponse = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(usuarioResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividad = new DActividad(0, "Yoga", "Clase de yoga avanzada", 20.0, 10,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadResponse = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadResponse.getBody();
        var userAuthenticatedTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var solicitud = new DSolicitud(0, 0, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());
        var solicitudResponse = userAuthenticatedTemplate.postForEntity(
                "/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
        assertThat(solicitudResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var deleteResponse = userAuthenticatedTemplate.exchange(
                "/solicitudes/" + 1, HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void testMarcarPagoCuota() {
        var usuario = new DUsuario("12345678B", "Ana", "López", "Calle Luna 34", "611301025", "ana@gmail.com", false, "suClave1234");
        var registroRespuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        adminRestTemplate.put("/usuarios/{dni}/pagoCuota", null, "12345678B");

        var respuestaUsuario = adminRestTemplate.getForEntity("/usuarios/{dni}", DUsuario.class, "12345678B");
        assertThat(respuestaUsuario.getStatusCode()).isEqualTo(HttpStatus.OK);

        var usuarioRecuperado = respuestaUsuario.getBody();
        assertThat(usuarioRecuperado).isNotNull();
        assertThat(usuarioRecuperado.cuotaPagada()).isTrue();
    }

    @Test
    @DirtiesContext
    void testListarActividadesPorTemporadaUsuarioComun() {
        // Registrar usuario estándar
        var usuario = new DUsuario("12345678B", "Carlos", "Pérez", "Calle Real 10", "611203025", "carlos@gmail.com", false, "clave123");
        var registroUsuario = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroUsuario.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Crear temporada
        var temporada = new DTemporada(2025, 0);
        var registroTemporada = adminRestTemplate.postForEntity("/temporadas", temporada, Void.class);
        assertThat(registroTemporada.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Crear actividades asociadas a la temporada
        var actividad1 = new DActividad(0, "Natación", "Clases de natación", 15.0, 20, LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividad2 = new DActividad(0, "Yoga", "Clases de yoga", 10.0, 15, LocalDate.now().plusDays(8), LocalDate.now(), LocalDate.now().plusDays(12));

        var actividad1Response = adminRestTemplate.postForEntity("/actividades", actividad1, DActividad.class);
        var actividad2Response = adminRestTemplate.postForEntity("/actividades", actividad2, DActividad.class);

        assertThat(actividad1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actividad2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Asociar las actividades a la temporada (si no se hace automáticamente)
        adminRestTemplate.postForEntity("/temporadas/{anio}/actividades/{idActividad}", null, Void.class, temporada.anio(), actividad1Response.getBody().id());
        adminRestTemplate.postForEntity("/temporadas/{anio}/actividades/{idActividad}", null, Void.class, temporada.anio(), actividad2Response.getBody().id());

        // Validar que las actividades están asociadas como administrador
        var actividadesTemporada = adminRestTemplate.getForEntity("/temporadas/{anio}/actividades", DActividad[].class, temporada.anio());
        assertThat(actividadesTemporada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividadesTemporada.getBody()).isNotNull().hasSize(2);

        // Verificar acceso como usuario común
        var userAuthenticatedTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var actividadesResponse = userAuthenticatedTemplate.getForEntity("/temporadas/{anio}/actividades", DActividad[].class, temporada.anio());
        assertThat(actividadesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividadesResponse.getBody()).isNotNull().hasSize(2);
    }
}