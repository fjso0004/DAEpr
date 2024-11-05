package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Validated
public class ServicioProyecto {
    private Map<String, Usuario> usuarios;
    private Map<Long, Actividad> actividades;
    private static long contadorActividades = 0;

    public ServicioProyecto() {
        usuarios = new TreeMap<>();
        actividades = new TreeMap<>();
    }

    public Usuario crearUsuario(@NotNull @Valid Usuario usuario) {
        if (usuarios.containsKey(usuario.getDni())) {
            throw new UsuarioYaRegistrado();
        }
        usuarios.put(usuario.getDni(), usuario);
        return usuario;
    }

    public Usuario autenticar(@NotBlank String dni, @NotBlank String clave) {
        Usuario usuario = buscarUsuario(dni);
        if (!usuario.claveValida(clave)) {
            throw new ClaveIncorrecta();
        }
        return usuario;
    }

    public Usuario buscarUsuario(@NotBlank String dni) {
        Usuario usuario = usuarios.get(dni);
        if (usuario == null) {
            throw new UsuarioNoRegistrado();
        }
        return usuario;
    }

    public Actividad crearActividad(@NotBlank String tituloCorto, @NotBlank String descripcion, @Positive double precio, @Positive int numPlazas,
                                    @FutureOrPresent LocalDate fechaCelebracion, @FutureOrPresent LocalDate fechaInicio,
                                    @FutureOrPresent LocalDate fechaFinal) {
        Actividad actividad = new Actividad(tituloCorto, descripcion, precio, numPlazas, fechaCelebracion, fechaInicio, fechaFinal);
        actividad.generarIdActividad();
        actividades.put(actividad.getId(), actividad);
        return actividad;
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim().toLowerCase();
    }

    public List<Actividad> buscarActividades(@NotBlank String tituloCorto, @NotNull LocalDate fechaCelebracion) {
        List<Actividad> actividadesFiltradas = new ArrayList<>();
        for (Actividad actividad : actividades.values()) {
            if (actividadValida(actividad, tituloCorto, fechaCelebracion)) {
                actividadesFiltradas.add(actividad);
            }
        }
        return actividadesFiltradas;
    }

    private boolean actividadValida(Actividad actividad, String tituloCorto, LocalDate fechaCelebracion) {
        return normalizar(actividad.getTituloCorto()).equals(normalizar(tituloCorto)) &&
                actividad.getFechaCelebracion().equals(fechaCelebracion);
    }

    public Solicitudes crearSolicitud(@Positive long idActividad, @NotBlank String dniSocio) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        Usuario socio = buscarUsuario(dniSocio);

        for (Solicitudes solicitud : actividad.getSolicitudes()) {
            if (solicitud.getUsuario().getDni().equals(dniSocio)) {
                throw new UsuarioYaRegistrado();
            }
        }

        Solicitudes solicitud = new Solicitudes(actividad, socio);
        actividad.altaSolicitud(solicitud);
        return solicitud;
    }

    public void modificarSolicitud(@Positive long idActividad, @Positive long idSolicitud, @Positive int numAcomp) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }

        Solicitudes solicitud = actividad.getSolicitudes().stream()
                .filter(s -> s.getId() == idSolicitud)
                .findFirst()
                .orElseThrow(SolicitudNoRegistrada::new);

        solicitud.setNumAcomp(numAcomp);
    }

    public void marcarCuota(@NotNull Usuario user) {
        if (!user.getCuota()) {
            user.setCuota(true);
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
            actividad.nuevoSocio(socio);
            actividad.borrarSolicitud(solicitud);
        } else {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);
        }
    }

    public void rechazarPlaza(@NotNull Solicitudes solicitud) {
        solicitud.setEstado(Solicitudes.EstadoSolicitud.RECHAZADA);
        solicitud.getActividad().borrarSolicitud(solicitud);
    }

    public List<Solicitudes> obtenerSolicitudesPendientes(@Positive long idActividad) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        return actividad.getSolicitudesPendientes();
    }
}
