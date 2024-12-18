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
        var solicitud = new DSolicitud(0, 2, LocalDate.now(), "PENDIENTE", "12345678B",
                1);
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

        // Paso 3: Verificar que la cuota ha sido marcada como pagada
        var respuestaUsuario = adminRestTemplate.getForEntity("/sociosclub/usuarios/{dni}", DUsuario.class, "12345678B");
        assertThat(respuestaUsuario.getStatusCode()).isEqualTo(HttpStatus.OK);
        var usuarioRecuperado = respuestaUsuario.getBody();
        assertThat(usuarioRecuperado).isNotNull();
        assertThat(usuarioRecuperado.cuota()).isTrue();  // Verifica que la cuota esté marcada como pagada
    }
*/


    @Test
    @DirtiesContext
    void testBorrarSolicitud() {


        var usuario = new DUsuario("12345678B", "Ana", "López", "Calle Luna 34", "611301025", "ana@gmail.com", false, "suClave1234");
        var registroRespuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10, LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadRespuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividadCreada = actividadRespuesta.getBody();
        assertThat(actividadCreada).isNotNull();


        var userAuthenticatedTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var solicitud = new DSolicitud(actividadCreada.id(), 1, LocalDate.now(), "PENDIENTE", usuario.dni(), actividadCreada.id());
        var respuesta = userAuthenticatedTemplate.postForEntity("/solicitudes?idActividad=" + actividadCreada.id(), solicitud, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long idSolicitud = solicitud.id();


        restTemplate.delete("/solicitudes/{idSolicitud}", idSolicitud);


        var respuestaBorrado = restTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, idSolicitud);
        assertThat(respuestaBorrado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }



}