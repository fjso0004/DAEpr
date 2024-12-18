package es.ujaen.dae.sociosclub.rest;

import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.excepciones.*;
import es.ujaen.dae.sociosclub.repositorios.RepositorioTemporada;
import es.ujaen.dae.sociosclub.repositorios.RepositorioUsuario;
import es.ujaen.dae.sociosclub.rest.dto.*;
import es.ujaen.dae.sociosclub.seguridad.ServicioCredencialesUsuario;
import es.ujaen.dae.sociosclub.servicios.ServicioProyecto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sociosclub")
public class ControladorSociosClub {

    @Autowired
    private Mapeador mapeador;

    @Autowired
    private ServicioProyecto servicioProyecto;

    @Autowired
    private ServicioCredencialesUsuario servicioCredencialesUsuario = new ServicioCredencialesUsuario();

    private static final Usuario administrador = new Usuario(
            "12345678Z",
            "admin",
            "-",
            "-",
            "659123456",
            "admin@sociosclub.es",
            new BCryptPasswordEncoder().encode("SuperUser"), // Contraseña codificada
            true
    );

    @Autowired
    private RepositorioTemporada repositorioTemporada;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    public void admin () {
        servicioCredencialesUsuario.loadUserByUsername(administrador.getDni());
        repositorioUsuario.crear(administrador);
    }

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
            Solicitudes solicitud = servicioProyecto.buscarSolicitudPorId(id);
            return ResponseEntity.ok(mapeador.dto(solicitud));
        } catch (SolicitudNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 4. Gestión de temporadas
    @PostMapping("/temporadas")
    public ResponseEntity<Void> nuevaTemporada(@RequestBody DTemporada dTemporada) {
        try {
            servicioProyecto.crearTemporada(dTemporada.anio());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (TemporadaYaExistente e) {

            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/temporadas/{anio}")
    public ResponseEntity<DTemporada> obtenerTemporada(@PathVariable int anio) {
        Optional<Temporada> temporadaOpt = repositorioTemporada.buscarPorAnio(anio);
        if (temporadaOpt.isPresent()) {
            Temporada temporada = temporadaOpt.get();
            DTemporada dTemporada = mapeador.dto(temporada);
            return ResponseEntity.ok(dTemporada);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @GetMapping("/temporadas")
    public ResponseEntity<List<DTemporada>> listarTemporadas() {
        List<Temporada> temporadas = servicioProyecto.buscarTodasTemporadas();
        List<DTemporada> temporadasDTO = temporadas.stream().map(mapeador::dto).toList();
        return ResponseEntity.ok(temporadasDTO);
    }

    @DeleteMapping("/solicitudes/{id}")
    public ResponseEntity<DSolicitud> borrarSolicitud(@PathVariable long id) {
        servicioProyecto.borrarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/{dni}/pagoCuota")
    public ResponseEntity<Void> marcarCuotaPagada(@PathVariable String dni) {
        servicioProyecto.marcarCuotaPagada(dni);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/solicitudes/{idSolicitud}")
    public ResponseEntity<Void> modificarSolicitud(
            @PathVariable long idSolicitud,
            @RequestParam int idActividad,
            @RequestParam int numAcomp) {
        try {
            servicioProyecto.modificarSolicitud(idActividad, idSolicitud, numAcomp);
            return ResponseEntity.ok().build();
        } catch (ActividadNoRegistrada | SolicitudNoRegistrada | PlazasNoDisponibles e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/temporadas/{anio}/actividades")
    public ResponseEntity<List<DActividad>> listarActividadesPorTemporada(@PathVariable int anio) {
        try {
            List<Actividad> actividades = servicioProyecto.buscarActividadesPorTemporada(anio);
            List<DActividad> actividadesDTO = actividades.stream().map(mapeador::dto).toList();
            return ResponseEntity.ok(actividadesDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/actividades/{id}/solicitudes")
    public ResponseEntity<List<DSolicitud>> listarSolicitudesPorActividad(@PathVariable int id) {
        try {
            List<Solicitudes> solicitudes = servicioProyecto.listarSolicitudesDeActividad(id);
            List<DSolicitud> solicitudesDTO = solicitudes.stream().map(mapeador::dto).toList();
            return ResponseEntity.ok(solicitudesDTO);
        } catch (ActividadNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

