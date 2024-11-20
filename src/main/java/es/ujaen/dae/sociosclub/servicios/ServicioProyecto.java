package es.ujaen.dae.sociosclub.servicios;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import es.ujaen.dae.sociosclub.repositorios.RepositorioActividad;
import es.ujaen.dae.sociosclub.repositorios.RepositorioSolicitudes;
import es.ujaen.dae.sociosclub.repositorios.RepositorioTemporada;
import es.ujaen.dae.sociosclub.repositorios.RepositorioUsuario;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDate;
import java.util.*;

@Service
@Validated
public class ServicioProyecto {
    @Autowired
    RepositorioUsuario repositorioUsuario;
    @Autowired
    RepositorioActividad repositorioActividad;
    @Autowired
    RepositorioSolicitudes repositorioSolicitudes;
    @Autowired
    RepositorioTemporada repositorioTemporada;

    private Map<String, Usuario> usuarios;
    private Map<Integer, Actividad> actividades;


    private static final Usuario administrador = new Usuario("12345678Z", "admin", "-", "-", "659123456",
             "admin@sociosclub.es", "SuperUser", true);

    public ServicioProyecto() {
        usuarios = new TreeMap<>();
        actividades = new TreeMap<>();
    }

    public Usuario crearUsuario(@NotNull @Valid Usuario usuario) {
        if (usuario.getDni().equals(administrador.getDni()))
            throw new UsuarioYaRegistrado();
/*
        if (usuarios.containsKey(usuario.getDni())) {
            throw new UsuarioYaRegistrado();
        }
        usuarios.put(usuario.getDni(), usuario);
        return usuario;
 */
        repositorioUsuario.crear(usuario);
        return usuario;
    }

    public boolean login(@NotBlank String dni, @NotBlank String clave) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorDni(dni);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getClave().equals(clave)) {
            throw new ClaveoUsuarioIncorrecto("Usuario o clave incorrecto");
        }
        return true;
        /*
        Usuario usuario = usuarios.get(dni);
        if (usuario == null || !usuario.getDni().equals(dni) || !usuario.getClave().equals(clave)) {
            throw new ClaveoUsuarioIncorrecto("Usuario o clave incorrecto");
        }
        return true;
        */
    }

    public Usuario buscarUsuario(Usuario administrador, @NotBlank String dni) {
        if (administrador.getNombre().equals("admin")) {
            return repositorioUsuario.buscarPorDni(dni)
                    .orElseThrow(UsuarioNoRegistrado::new);
        } else {
            throw new OperacionDeAdmin();
        }
        /*
        if (administrador.getNombre().equals("admin")){
            Usuario usuario = usuarios.get(dni);
            if (usuario == null) {
                throw new UsuarioYaRegistrado();
            }
            return usuario;

        }else {
            throw new OperacionDeAdmin();
        }
        */
    }

    public Actividad crearActividad(@NotBlank String tituloCorto, @NotBlank String descripcion, @Positive double precio, @Positive int numPlazas,
                                    @FutureOrPresent LocalDate fechaCelebracion, @FutureOrPresent LocalDate fechaInicio,
                                    @FutureOrPresent LocalDate fechaFinal) {
        int anioTemporada = fechaCelebracion.getYear();
        Optional<Temporada> temporadaOpt = repositorioTemporada.buscarPorAnio(anioTemporada);
        Temporada temporada;
        if (temporadaOpt.isPresent()) {
            temporada = temporadaOpt.get();
        } else {
            temporada = new Temporada(anioTemporada);
            repositorioTemporada.crear(temporada);
        }
        Actividad actividad = new Actividad(tituloCorto, descripcion, precio, numPlazas, fechaCelebracion, fechaInicio, fechaFinal);
        actividad.setTemporada(temporada);
        if (actividad.getTemporada() == null) {
            throw new IllegalStateException("La temporada no puede ser null antes de persistir la actividad.");
        }
        repositorioActividad.crear(actividad);
        return actividad;
        /*
        Actividad actividad = new Actividad(tituloCorto, descripcion, precio, numPlazas, fechaCelebracion, fechaInicio, fechaFinal);
        //actividad.generarIdActividad();
        actividades.put(actividad.getId(), actividad);
        return actividad;
        */
    }


    public List<Actividad> buscarActividades() {
        /*
        List<Actividad> actividadesFiltradas = new ArrayList<>();
        for (Actividad actividad : actividades.values()) {
            if (actividadValida(actividad)) {
                actividadesFiltradas.add(actividad);
            }
        }
        return actividadesFiltradas;
        */
        return repositorioActividad.buscarTodas().stream()
                .filter(this::actividadValida)
                .toList();
    }
    

    private boolean actividadValida(Actividad actividad) {
        return actividad.getFechaCelebracion().isAfter(LocalDate.now());
    }

    public void crearSolicitud(@Positive long idActividad, @NotBlank String dniSocio, @Min(0) @Max(5) int num_acomp) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        Usuario socio = repositorioUsuario.buscarPorDni(dniSocio)
                .orElseThrow(UsuarioNoRegistrado::new);

        boolean solicitudPrevia = actividad.getSolicitudes().stream()
                .anyMatch(solicitud -> solicitud.getUsuario().getDni().equals(dniSocio));
        if (solicitudPrevia) {
            throw new UsuarioYaRegistrado();
        }

        Solicitudes.EstadoSolicitud estadoSolicitud = socio.getCuota() ?
                Solicitudes.EstadoSolicitud.ACEPTADA :
                Solicitudes.EstadoSolicitud.PENDIENTE;

        Solicitudes solicitud = new Solicitudes(actividad, socio, num_acomp, estadoSolicitud);
        actividad.altaSolicitud(solicitud);
        repositorioActividad.actualizar(actividad);
        /*
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }

        Usuario socio = usuarios.get(dniSocio);

        if (socio == null) {
            throw new UsuarioNoRegistrado();
        }

        for (Solicitudes solicitud : actividad.getSolicitudes()) {
            if (solicitud.getUsuario().getDni().equals(dniSocio)) {
                throw new UsuarioYaRegistrado();
            }
        }

        Solicitudes.EstadoSolicitud estadoSolicitud;
        if (socio.getCuota()) {
           estadoSolicitud = Solicitudes.EstadoSolicitud.ACEPTADA;
        } else {
            estadoSolicitud = Solicitudes.EstadoSolicitud.PENDIENTE;
        }

        Solicitudes solicitud = new Solicitudes(actividad, socio, num_acomp, estadoSolicitud);
        actividad.altaSolicitud(solicitud);
        */
    }


    public void modificarSolicitud(@Positive long idActividad, @Positive long idSolicitud, @Positive int numAcomp) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);

        Solicitudes solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(SolicitudNoRegistrada::new);

        solicitud.setNumAcomp(numAcomp);
        repositorioSolicitudes.actualizar(solicitud);
        /*
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
         */
    }

    public void marcarCuota(@NotNull Usuario user) {
        if (!user.getCuota()) {
            user.setCuota(true);
            repositorioUsuario.actualizar(user);
        }
    }

    public void asignarPlaza(@NotNull Solicitudes solicitud) {
        Actividad actividad = solicitud.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        Usuario socio = solicitud.getUsuario();

        if (socio.getCuota() && actividad.getNumPlazas() > 0) {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);
            actividad.borrarSolicitud(solicitud);
            actividad.setNumPlazas(actividad.getNumPlazas() - 1);
            repositorioActividad.actualizar(actividad);
            repositorioSolicitudes.actualizar(solicitud);
        } else {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);
            repositorioSolicitudes.actualizar(solicitud);
        }
        /*
        Actividad actividad = solicitud.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        Usuario socio = solicitud.getUsuario();

        if (socio.getCuota() && actividad.getNumPlazas() > 0) {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);
            actividad.borrarSolicitud(solicitud);
        } else {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);
        }
        */
    }

    public void rechazarPlaza(@NotNull Solicitudes solicitud) {
        solicitud.setEstado(Solicitudes.EstadoSolicitud.RECHAZADA);
        solicitud.getActividad().borrarSolicitud(solicitud);
        repositorioSolicitudes.actualizar(solicitud);
        repositorioActividad.actualizar(solicitud.getActividad());
        /*
        solicitud.setEstado(Solicitudes.EstadoSolicitud.RECHAZADA);
        solicitud.getActividad().borrarSolicitud(solicitud);
        */
    }

    public List<Solicitudes> obtenerSolicitudesPendientes(@Positive long idActividad) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        return actividad.getSolicitudesPendientes();
        /*
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        return actividad.getSolicitudesPendientes();
         */
    }
}
