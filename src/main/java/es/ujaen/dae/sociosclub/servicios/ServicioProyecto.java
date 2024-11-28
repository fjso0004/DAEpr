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


    private static final Usuario administrador = new Usuario("12345678Z", "admin", "-", "-", "659123456",
             "admin@sociosclub.es", "SuperUser", true);

    public ServicioProyecto() {}

    public Usuario crearUsuario(@NotNull @Valid Usuario usuario) {
        if (usuario.getDni().equals(administrador.getDni())){
            throw new UsuarioYaRegistrado();
        }
        repositorioUsuario.crear(usuario);
        return usuario;
    }

    public boolean login(@NotBlank String dni, @NotBlank String clave) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorDni(dni);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getClave().equals(clave)) {
            throw new ClaveoUsuarioIncorrecto("Usuario o clave incorrecto");
        }
        return true;
    }

    public Usuario buscarUsuario(Usuario administrador, @NotBlank String dni) {
        if (administrador.getNombre().equals("admin")) {
            return repositorioUsuario.buscarPorDni(dni)
                    .orElseThrow(UsuarioNoRegistrado::new);
        } else {
            throw new OperacionDeAdmin();
        }
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
        repositorioActividad.crear(actividad);
        return actividad;
    }


    public List<Actividad> buscarActividades() {
        return repositorioActividad.buscarTodas().stream()
                .filter(this::actividadValida)
                .toList();
    }
    

    private boolean actividadValida(Actividad actividad) {
        return actividad.getFechaCelebracion().isAfter(LocalDate.now());
    }

    @Transactional
    public Actividad buscarActividadPorId(int idActividad) {
        return repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
    }

    @Transactional
    public void crearSolicitud(int idActividad, @NotBlank String dniSocio, @Min(0) @Max(5) int num_acomp) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        Usuario socio = repositorioUsuario.buscarPorDni(dniSocio)
                .orElseThrow(UsuarioNoRegistrado::new);

        Solicitudes solicitud = new Solicitudes(actividad, socio, num_acomp);
        repositorioSolicitudes.crear(solicitud);

        actividad.altaSolicitud(solicitud);
        repositorioActividad.actualizar(actividad);
    }

    public void modificarSolicitud(int idActividad, @Positive long idSolicitud, @Positive int numAcomp) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);

        Solicitudes solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(SolicitudNoRegistrada::new);

        int plazasNecesarias = 1 + numAcomp - solicitud.getNumAcomp(); // Cambios en plazas
        if (actividad.getNumPlazas() < plazasNecesarias) {
            throw new PlazasNoDisponibles();
        }

        solicitud.setNumAcomp(numAcomp);
        actividad.setNumPlazas(actividad.getNumPlazas() - plazasNecesarias);
        repositorioSolicitudes.actualizar(solicitud);
        repositorioActividad.actualizar(actividad);
    }

    public void marcarCuota(@NotNull Usuario user) {
        if (!user.getCuota()) {
            user.setCuota(true);
            repositorioUsuario.actualizar(user);
        }
    }

    @Transactional
    public void asignarPlaza(@NotNull Solicitudes solicitud, @Positive int plazasAsignadas) {
        Actividad actividad = solicitud.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }

        actividad.asignarPlaza(solicitud, plazasAsignadas);
        repositorioActividad.actualizar(actividad);
        repositorioSolicitudes.actualizar(solicitud);
    }

    @Transactional
    public List<Solicitudes> obtenerSolicitudesPendientes(@Positive int idActividad) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        return actividad.getSolicitudesPendientes();
    }
}
