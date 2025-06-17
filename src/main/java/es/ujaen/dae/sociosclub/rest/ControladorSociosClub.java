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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sociosclub")
public class ControladorSociosClub {

    @Autowired
    private Mapeador mapeador;

    @Autowired
    private ServicioProyecto servicioProyecto;
/*
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
 */

    @Autowired
    private RepositorioTemporada repositorioTemporada;

    @Autowired
    private RepositorioUsuario repositorioUsuario;
/*
    public void admin () {
        servicioCredencialesUsuario.loadUserByUsername(administrador.getDni());
        repositorioUsuario.crear(administrador);
    }

 */

    // Manejo global de excepciones de validación
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public void manejarExcepcionesValidacion() {}

    // 1. Usuarios
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@RequestBody DUsuario dUsuario) {
        try {
            Usuario entidad = mapeador.entidad(dUsuario);
            servicioProyecto.crearUsuario(entidad);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsuarioYaRegistrado e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/usuarios/{dni}")
    public ResponseEntity<DUsuario> obtenerUsuario(
            @PathVariable String dni,
            Authentication authentication
    ) {
        try {
            // Solo admin o el mismo usuario pueden ver
            String dniAuth = authentication.getName();
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin && !dniAuth.equals(dni)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Usuario usuario = servicioProyecto.buscarUsuario(dni);
            return ResponseEntity.ok(mapeador.dto(usuario));
        } catch (UsuarioNoRegistrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/usuarios/{dni}")
    public ResponseEntity<Void> actualizarUsuario(
            @PathVariable String dni,
            @Valid @RequestBody DUsuario dto,
            Authentication authentication
    ) {
        // Solo ADMIN puede actualizar
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!dni.equals(dto.dni())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            servicioProyecto.actualizarUsuarioDesdeDto(dto);
            return ResponseEntity.noContent().build();
        } catch (UsuarioNoRegistrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 2. Actividades
    @PostMapping("/actividades")
    public ResponseEntity<DActividad> nuevaActividad(@RequestBody DActividad dActividad,
                                                     Authentication authentication) {
        // Seguridad: solo ADMIN puede invocar. (Config de seguridad permitirá solo ROLE_ADMIN)
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
    public ResponseEntity<List<DActividad>> listarActividades(
            @RequestParam(name="anio", required=false) Integer anio,
            @RequestParam(name="fechaDesde", required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name="fechaHasta", required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            return ResponseEntity.badRequest().build();
        }
        List<Actividad> actividades;
        try {
            if (anio != null) {
                actividades = servicioProyecto.buscarActividadesPorTemporada(anio);
            } else if (fechaDesde != null || fechaHasta != null) {
                actividades = servicioProyecto.buscarActividadesPorRango(fechaDesde, fechaHasta);
            } else {
                actividades = servicioProyecto.buscarActividades();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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

    // 3. Solicitudes
    @PostMapping("/actividades/{idActividad}/solicitudes")
    public ResponseEntity<Void> nuevaSolicitud(
            @PathVariable int idActividad,
            @Valid @RequestBody DSolicitudCrear dto,
            Authentication authentication
    ) {
        try {
            // Solo USUARIO autenticado puede invocar; la seguridad config lo asegurará.
            // Opcional: podrías validar que authentication.getName().equals(dto.dniUsuario()), si quieres forzar.
            servicioProyecto.crearSolicitud(idActividad, dto.dniUsuario(), dto.numAcomp());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsuarioNoRegistrado | ActividadNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UsuarioYaRegistrado e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PlazasNoDisponibles e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<DSolicitud> obtenerSolicitud(
            @PathVariable long id,
            Authentication authentication
    ) {
        try {
            Solicitudes sol = servicioProyecto.buscarSolicitudPorId(id);
            // Permitir propietario o admin
            String dniAuth = authentication.getName();
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin && !sol.getUsuario().getDni().equals(dniAuth)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(mapeador.dto(sol));
        } catch (SolicitudNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/solicitudes/{idSolicitud}")
    public ResponseEntity<Void> modificarSolicitud(
            @PathVariable long idSolicitud,
            @Valid @RequestBody DSolicitudActualizar dto,
            Authentication authentication
    ) {
        try {
            Solicitudes sol = servicioProyecto.buscarSolicitudPorId(idSolicitud);
            String dniAuth = authentication.getName();
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin && !sol.getUsuario().getDni().equals(dniAuth)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            servicioProyecto.modificarSolicitudNumAcomp(idSolicitud, dto.numAcomp());
            return ResponseEntity.ok().build();
        } catch (SolicitudNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PlazasNoDisponibles e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ActividadNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/solicitudes/{idSolicitud}")
    public ResponseEntity<Void> borrarSolicitud(
            @PathVariable long idSolicitud,
            Authentication authentication
    ) {
        try {
            Solicitudes sol = servicioProyecto.buscarSolicitudPorId(idSolicitud);
            String dniAuth = authentication.getName();
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin && !sol.getUsuario().getDni().equals(dniAuth)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            servicioProyecto.borrarSolicitud(idSolicitud);
            return ResponseEntity.noContent().build();
        } catch (SolicitudNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 4. Temporadas
    @PostMapping("/temporadas")
    public ResponseEntity<Void> nuevaTemporada(@RequestBody DTemporada dTemporada, Authentication authentication) {
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
            return ResponseEntity.ok(mapeador.dto(temporadaOpt.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/temporadas")
    public ResponseEntity<List<DTemporada>> listarTemporadas() {
        List<Temporada> temporadas = servicioProyecto.buscarTodasTemporadas();
        List<DTemporada> temporadasDTO = temporadas.stream().map(mapeador::dto).toList();
        return ResponseEntity.ok(temporadasDTO);
    }

    @GetMapping("/temporadas/{anio}/actividades")
    public ResponseEntity<List<DActividad>> listarActividadesPorTemporada(@PathVariable int anio) {
        try {
            List<Actividad> actividades = servicioProyecto.buscarActividadesPorTemporada(anio);
            List<DActividad> actividadesDTO = actividades.stream().map(mapeador::dto).toList();
            return ResponseEntity.ok(actividadesDTO);
        } catch (TemporadaNoEncontrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Marcar cuota pagada (solo ADMIN)
    @PutMapping("/usuarios/{dni}/pagoCuota")
    public ResponseEntity<Void> marcarCuotaPagada(@PathVariable String dni, Authentication authentication) {
        try {
            servicioProyecto.marcarCuotaPagada(dni);
            return ResponseEntity.ok().build();
        } catch (UsuarioNoRegistrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Listar solicitudes de una actividad dado su id.
     * Sólo ADMIN puede invocar (la seguridad debe permitirlo).
     */
    @GetMapping("/actividades/{id}/solicitudes")
    public ResponseEntity<List<DSolicitud>> listarSolicitudesPorActividad(
            @PathVariable("id") int idActividad,
            Authentication authentication
    ) {
        // Verificar rol ADMIN (opcional si tu SecurityFilterChain ya lo restringe):
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Solicitudes> solicitudes = servicioProyecto.listarSolicitudesDeActividad(idActividad);
            List<DSolicitud> dtoList = solicitudes.stream()
                    .map(mapeador::dto)
                    .toList();
            return ResponseEntity.ok(dtoList);
        } catch (ActividadNoRegistrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

