package es.ujaen.dae.sociosclub.rest;

import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.excepciones.*;
import es.ujaen.dae.sociosclub.rest.dto.*;
import es.ujaen.dae.sociosclub.servicios.ServicioProyecto;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sociosclub")
public class ControladorSociosClub {

    @Autowired
    private Mapeador mapeador;

    @Autowired
    private ServicioProyecto servicioProyecto;

    private static final Usuario administrador = new Usuario("12345678Z", "admin", "-", "-", "659123456",
            "admin@sociosclub.es", "SuperUser", true);



    // Manejo global de excepciones de validación
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public void manejarExcepcionesValidacion() {}

    // 1. Gestión de usuarios
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@RequestBody DUsuario dUsuario) {
        try {
            servicioProyecto.crearUsuario(mapeador.entidad(dUsuario));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsuarioYaRegistrado e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/usuarios/{dni}")
    public ResponseEntity<DUsuario> obtenerUsuario(@PathVariable String dni) {
        try {
            Usuario usuario = servicioProyecto.buscarUsuario(administrador, dni);
            return ResponseEntity.ok(mapeador.dto(usuario));
        } catch (UsuarioNoRegistrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (OperacionDeAdmin e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 2. Gestión de actividades
    @PostMapping("/actividades")
    public ResponseEntity<DActividad> nuevaActividad(@RequestBody DActividad dActividad) {
        Actividad actividad = servicioProyecto.crearActividad(
                dActividad.tituloCorto(),
                dActividad.descripcion(),
                dActividad.precio(),
                dActividad.numPlazas(),
                dActividad.fechaCelebracion(),
                dActividad.fechaInicio(),
                dActividad.fechaFin()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.dto(actividad));
    }

    @GetMapping("/actividades")
    public ResponseEntity<List<DActividad>> listarActividades() {
        List<Actividad> actividades = servicioProyecto.buscarActividades();
        List<DActividad> actividadesDTO = actividades.stream().map(mapeador::dto).toList();
        return ResponseEntity.ok(actividadesDTO);
    }

    @GetMapping("/actividades/{id}")
    public ResponseEntity<DActividad> obtenerActividad(@PathVariable int id) {
        try {
            Actividad actividad = servicioProyecto.buscarActividadPorId(id);
            return ResponseEntity.ok(mapeador.dto(actividad));
        } catch (ActividadNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 3. Gestión de solicitudes
    @PostMapping("/solicitudes")
    public ResponseEntity<Void> nuevaSolicitud(@RequestParam int idActividad, @RequestBody DSolicitud dSolicitud) {
        try {
            servicioProyecto.crearSolicitud(
                    idActividad,
                    dSolicitud.dniUsuario(),
                    dSolicitud.numAcomp()
            );
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsuarioNoRegistrado | ActividadNoRegistrada | PlazasNoDisponibles e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<DSolicitud> obtenerSolicitud(@PathVariable long id) {
        try {
            Solicitudes solicitud = servicioProyecto.buscarActividadPorId((int) id)
                    .getSolicitudes()
                    .stream()
                    .filter(s -> s.getId() == id)
                    .findFirst()
                    .orElseThrow(SolicitudNoRegistrada::new);
            return ResponseEntity.ok(mapeador.dto(solicitud));
        } catch (SolicitudNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 4. Gestión de temporadas
    @PostMapping("/temporadas")
    public ResponseEntity<Void> nuevaTemporada(@RequestBody DTemporada dTemporada) {
        Temporada temporada = mapeador.entidad(dTemporada);

        LocalDate fechaInicio = LocalDate.of(dTemporada.anio(), 1, 1);
        LocalDate fechaFin = LocalDate.of(dTemporada.anio(), 12, 31);

        servicioProyecto.crearActividad(
                "Temporada " + temporada.getAnio(),
                "Actividades para el año " + temporada.getAnio(),
                0, // Precio
                0, // Número de plazas
                fechaInicio,
                fechaInicio,
                fechaFin
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/temporadas")
    public ResponseEntity<List<DTemporada>> listarTemporadas() {
        List<Temporada> temporadas = servicioProyecto.buscarTodasTemporadas();
        List<DTemporada> temporadasDTO = temporadas.stream().map(mapeador::dto).toList();
        return ResponseEntity.ok(temporadasDTO);
    }
}

