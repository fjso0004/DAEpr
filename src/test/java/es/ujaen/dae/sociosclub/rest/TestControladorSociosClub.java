package es.ujaen.dae.sociosclub.rest;

import es.ujaen.dae.sociosclub.rest.dto.*;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;

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

        // Registrar un administrador predeterminado: ahora DUsuario tiene 9 parámetros:
        // (dni, nombre, apellidos, direccion, tlf, email, administrador, cuotaPagada, clave)
        var admin = new DUsuario(
                "12345678Z",
                "Admin",
                "Default",
                "Calle Principal 1",
                "600111111",
                "admin@sociosclub.com",
                true,   // administrador
                false,  // cuotaPagada inicial
                "superUser"
        );
        var respuestaAdmin = restTemplate.postForEntity("/usuarios", admin, Void.class);
        if (!respuestaAdmin.getStatusCode().equals(HttpStatus.CREATED) &&
                !respuestaAdmin.getStatusCode().equals(HttpStatus.CONFLICT)) {
            throw new RuntimeException("No se pudo configurar el administrador predeterminado");
        }
        adminRestTemplate = restTemplate.withBasicAuth(admin.dni(), admin.clave());
    }

    @Test
    @DirtiesContext
    public void testNuevaTemporada() {
        var dTemporada = new DTemporada(2026, 0);

        var respuesta = restTemplate.withBasicAuth("12345678Z", "superUser")
                .postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        respuesta = restTemplate.withBasicAuth("12345678Z", "superUser")
                .postForEntity("/temporadas", dTemporada, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var respuestaRecuperacion = restTemplate.getForEntity("/temporadas/{anio}", DTemporada.class, dTemporada.anio());
        assertThat(respuestaRecuperacion.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaRecuperacion.getBody()).isNotNull();
        assertThat(respuestaRecuperacion.getBody().anio()).isEqualTo(dTemporada.anio());
    }

    @Test
    @DirtiesContext
    void testNuevoUsuario() {
        // Ahora DUsuario tiene (dni, nombre, apellidos, direccion, tlf, email, administrador, cuotaPagada, clave)
        var usuario = new DUsuario(
                "12345678A",
                "Pedro",
                "Gómez",
                "Calle Real 12",
                "611203025",
                "pedro@gmail.com",
                false, // administrador
                false, // cuotaPagada inicial
                "miClave12341"
        );
        var respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        respuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var respuestaLogin = restTemplate.withBasicAuth(usuario.dni(), usuario.clave())
                .getForEntity("/usuarios/{dni}", DUsuario.class, usuario.dni());
        assertThat(respuestaLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaLogin.getBody().dni()).isEqualTo(usuario.dni());
    }

    @Test
    @DirtiesContext
    void testNuevaActividadComoAdmin() {
        var actividad = new DActividad(0, "Yoga", "Clase para principiantes", 10.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));

        var respuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var actividades = adminRestTemplate.getForEntity("/actividades", DActividad[].class);
        assertThat(actividades.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividades.getBody()).isNotNull().hasSize(1);
        assertThat(actividades.getBody()[0].tituloCorto()).isEqualTo("Yoga");
    }

    @Test
    @DirtiesContext
    void testNuevaSolicitud() {
        // Registrar un nuevo usuario estándar
        var usuario = new DUsuario(
                "12345678B",
                "Ana",
                "López",
                "Calle Luna 34",
                "611301025",
                "ana@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "suClave1234"
        );
        var registroRespuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Crear actividad como administrador
        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10,
                LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadRespuesta = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var actividadCreada = actividadRespuesta.getBody();
        assertThat(actividadCreada).isNotNull();

        // Realizar una solicitud autenticado como usuario
        var userTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        // Construir body para creación de solicitud
        Map<String, Object> bodySolicitud = Map.of(
                "dniUsuario", usuario.dni(),
                "numAcomp", 1
        );
        var respuesta = userTemplate.postForEntity(
                "/actividades/" + actividadCreada.id() + "/solicitudes",
                bodySolicitud,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verificar que la solicitud existe: GET /solicitudes/{id} devolverá datos (id asumido 1)
        var solicitudGet = userTemplate.getForEntity("/solicitudes/{id}", DSolicitud.class, 1L);
        assertThat(solicitudGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(solicitudGet.getBody()).isNotNull();
        assertThat(solicitudGet.getBody().dniUsuario()).isEqualTo(usuario.dni());
        assertThat(solicitudGet.getBody().numAcomp()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testListarTemporadas() {
        var temporada1 = new DTemporada(2024, 0);
        var temporada2 = new DTemporada(2025, 0);

        restTemplate.withBasicAuth("12345678Z", "superUser").postForEntity("/temporadas", temporada1, Void.class);
        restTemplate.withBasicAuth("12345678Z", "superUser").postForEntity("/temporadas", temporada2, Void.class);

        var respuesta = restTemplate.getForEntity("/temporadas", DTemporada[].class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull().hasSize(2);
    }

    @Test
    @DirtiesContext
    void testObtenerActividad() {
        var actividad = new DActividad(0, "Zumba", "Clase energética", 12.0, 15,
                LocalDate.now().plusDays(3), LocalDate.now(), LocalDate.now().plusDays(7));

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
        // Registrar usuario y crear actividad y solicitud
        var usuario = new DUsuario(
                "12345678C",
                "Luis",
                "Martínez",
                "Calle Sol 5",
                "611401026",
                "luis@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "claveLuis"
        );
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var actividad = new DActividad(0, "Pilates", "Clase avanzada", 15.0, 10,
                LocalDate.now().plusDays(7), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadResponse = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var actividadCreada = actividadResponse.getBody();
        assertThat(actividadCreada).isNotNull();

        var userTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        Map<String, Object> bodyCrear = Map.of("dniUsuario", usuario.dni(), "numAcomp", 1);
        var solicitudResponse = userTemplate.postForEntity(
                "/actividades/" + actividadCreada.id() + "/solicitudes",
                bodyCrear, Void.class);
        assertThat(solicitudResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Modificar solicitud (suponiendo id = 1)
        Map<String, Object> bodyModificar = Map.of("numAcomp", 2);
        var putResponse = userTemplate.exchange(
                "/solicitudes/{idSolicitud}", HttpMethod.PUT,
                new HttpEntity<>(bodyModificar), Void.class, 1L);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verificar cambio
        var solicitudModificadaResponse = userTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, 1L);
        assertThat(solicitudModificadaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(solicitudModificadaResponse.getBody()).isNotNull();
        assertThat(solicitudModificadaResponse.getBody().numAcomp()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void testBorrarSolicitud() {
        // Registrar usuario y crear actividad y solicitud
        var usuario = new DUsuario(
                "12345678D",
                "María",
                "García",
                "Calle Norte 9",
                "611501027",
                "maria@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "claveMaria"
        );
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var actividad = new DActividad(0, "Yoga", "Clase de yoga avanzada", 20.0, 10,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadResponse = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var actividadCreada = actividadResponse.getBody();

        var userTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        Map<String, Object> bodyCrear = Map.of("dniUsuario", usuario.dni(), "numAcomp", 0);
        var solicitudResponse = userTemplate.postForEntity(
                "/actividades/" + actividadCreada.id() + "/solicitudes",
                bodyCrear, Void.class);
        assertThat(solicitudResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Borrar solicitud (id = 1)
        var deleteResponse = userTemplate.exchange(
                "/solicitudes/{idSolicitud}", HttpMethod.DELETE,
                null, Void.class, 1L);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar: GET devuelve 404
        var getAfterDelete = userTemplate.getForEntity("/solicitudes/{idSolicitud}", DSolicitud.class, 1L);
        assertThat(getAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void testMarcarPagoCuota() {
        // Registrar usuario normal
        var usuario = new DUsuario(
                "12345678E",
                "Ana",
                "López",
                "Calle Luna 34",
                "611301025",
                "ana@gmail.com",
                false, // administrador
                false, // cuotaPagada inicial
                "suClave1234"
        );
        var registroRespuesta = restTemplate.postForEntity("/usuarios", usuario, Void.class);
        assertThat(registroRespuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Obtener usuario actual para rellenar todos los campos en el PUT
        var getResp = adminRestTemplate.getForEntity("/usuarios/{dni}", DUsuario.class, usuario.dni());
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        DUsuario usuarioActual = getResp.getBody();
        assertThat(usuarioActual).isNotNull();
        assertThat(usuarioActual.cuotaPagada()).isFalse();

        // Construir DTO completo para PUT marcando cuotaPagada=true
        DUsuario usuarioModificado = new DUsuario(
                usuarioActual.dni(),
                usuarioActual.nombre(),
                usuarioActual.apellidos(),
                usuarioActual.direccion(),
                usuarioActual.tlf(),
                usuarioActual.email(),
                usuarioActual.administrador(), // false
                true,                         // marcar cuotaPagada
                ""                            // clave vacía: se ignora cambio de clave en servicio
        );
        var putResponse = adminRestTemplate.exchange(
                "/usuarios/{dni}", HttpMethod.PUT,
                new HttpEntity<>(usuarioModificado),
                Void.class,
                usuario.dni()
        );
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar con GET
        var respuestaUsuario = adminRestTemplate.getForEntity("/usuarios/{dni}", DUsuario.class, usuario.dni());
        assertThat(respuestaUsuario.getStatusCode()).isEqualTo(HttpStatus.OK);
        var usuarioRecuperado = respuestaUsuario.getBody();
        assertThat(usuarioRecuperado).isNotNull();
        assertThat(usuarioRecuperado.cuotaPagada()).isTrue();
    }

    @Test
    @DirtiesContext
    void testListarActividadesPorTemporada() {
        // Crear usuario, temporada y actividades
        var usuario = new DUsuario(
                "12345678F",
                "Carlos",
                "Pérez",
                "Calle Real 10",
                "611203025",
                "carlos@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "clave123"
        );
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var temporada = new DTemporada(2025, 0);
        adminRestTemplate.postForEntity("/temporadas", temporada, Void.class);

        var actividad1 = new DActividad(0, "Natación", "Clases de natación", 15.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividad2 = new DActividad(0, "Yoga", "Clases de yoga", 10.0, 15,
                LocalDate.now().plusDays(8), LocalDate.now(), LocalDate.now().plusDays(12));

        adminRestTemplate.postForEntity("/actividades", actividad1, DActividad.class);
        adminRestTemplate.postForEntity("/actividades", actividad2, DActividad.class);

        var userTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        var actividadesResponse = userTemplate.getForEntity("/temporadas/{anio}/actividades", DActividad[].class, temporada.anio());

        assertThat(actividadesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actividadesResponse.getBody()).isNotNull().hasSize(2);
    }

    @Test
    @DirtiesContext
    void testListarSolicitudesDeActividad() {
        // Crear actividad y una solicitud
        var actividad = new DActividad(0, "Futbol", "Entrenamiento avanzado", 10.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividadResponse = adminRestTemplate.postForEntity("/actividades", actividad, DActividad.class);
        assertThat(actividadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var actividadCreada = actividadResponse.getBody();

        var usuario = new DUsuario(
                "12345678G",
                "Juan",
                "Pérez",
                "Calle Centro 14",
                "611203023",
                "juan@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "clave123"
        );
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var userTemplate = restTemplate.withBasicAuth(usuario.dni(), usuario.clave());
        Map<String, Object> bodyCrear = Map.of("dniUsuario", usuario.dni(), "numAcomp", 2);
        var solicitudResponse = userTemplate.postForEntity(
                "/actividades/" + actividadCreada.id() + "/solicitudes",
                bodyCrear, Void.class);
        assertThat(solicitudResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Listar solicitudes de actividad (ADMIN)
        var solicitudesResponse = adminRestTemplate.getForEntity(
                "/actividades/{id}/solicitudes", DSolicitud[].class, actividadCreada.id());
        assertThat(solicitudesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(solicitudesResponse.getBody()).isNotNull().hasSize(1);
        assertThat(solicitudesResponse.getBody()[0].dniUsuario()).isEqualTo(usuario.dni());
    }

    @Test
    @DirtiesContext
    void testListarActividadesFiltroPorTemporada() {
        // Igual que tu test original: GET /actividades?anio=...
        var usuario = new DUsuario(
                "12345678H",
                "Carlos",
                "Pérez",
                "Calle Real 10",
                "611203025",
                "carlos@gmail.com",
                false, // administrador
                false, // cuotaPagada
                "clave123"
        );
        restTemplate.postForEntity("/usuarios", usuario, Void.class);

        var temporada = new DTemporada(2025, 0);
        adminRestTemplate.postForEntity("/temporadas", temporada, Void.class);

        var actividad1 = new DActividad(0, "Natación", "Clases de natación", 15.0, 20,
                LocalDate.now().plusDays(5), LocalDate.now(), LocalDate.now().plusDays(10));
        var actividad2 = new DActividad(0, "Yoga", "Clases de yoga", 10.0, 15,
                LocalDate.now().plusDays(8), LocalDate.now(), LocalDate.now().plusDays(12));

        adminRestTemplate.postForEntity("/actividades", actividad1, DActividad.class);
        adminRestTemplate.postForEntity("/actividades", actividad2, DActividad.class);

        var response = adminRestTemplate.getForEntity("/actividades?anio=2025", DActividad[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DirtiesContext
    void testListarActividadesFiltroPorRango() {
        LocalDate hoy = LocalDate.now();
        var act1 = new DActividad(0, "A", "desc", 15.0, 10, hoy.plusDays(5), hoy, hoy.plusDays(6));
        var act2 = new DActividad(0, "B", "desc", 15.0, 10, hoy.plusDays(15), hoy, hoy.plusDays(16));

        adminRestTemplate.postForEntity("/actividades", act1, DActividad.class);
        adminRestTemplate.postForEntity("/actividades", act2, DActividad.class);

        String fechaDesde = hoy.plusDays(3).toString();
        String fechaHasta = hoy.plusDays(10).toString();
        var response = adminRestTemplate.getForEntity("/actividades?fechaDesde=" + fechaDesde + "&fechaHasta=" + fechaHasta, DActividad[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].tituloCorto()).isEqualTo("A");
    }

    @Test
    @DirtiesContext
    void testListarActividadesSinParametros() {
        LocalDate hoy = LocalDate.now();
        var act1 = new DActividad(0, "X", "desc", 15.0, 10, hoy.plusDays(5), hoy, hoy.plusDays(6));
        var act2 = new DActividad(0, "Y", "desc", 15.0, 10, hoy.plusDays(10), hoy, hoy.plusDays(11));

        adminRestTemplate.postForEntity("/actividades", act1, DActividad.class);
        adminRestTemplate.postForEntity("/actividades", act2, DActividad.class);

        var response = adminRestTemplate.getForEntity("/actividades", DActividad[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
