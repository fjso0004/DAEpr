package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.entidades.Usuario;

import es.ujaen.dae.sociosclub.excepciones.ActividadNoRegistrada;
import es.ujaen.dae.sociosclub.excepciones.ClaveIncorrecta;
import es.ujaen.dae.sociosclub.excepciones.UsuarioNoRegistrado;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
import es.ujaen.dae.sociosclub.excepciones.SolicitudNoRegistrada;
import es.ujaen.dae.sociosclub.repositorios.ActividadRepositorio;
import es.ujaen.dae.sociosclub.repositorios.UsuarioRepositorio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
    Map<String, Usuario> usuarios;
    Map<Long, Actividad> actividades;

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
        Actividad act = new Actividad(tituloCorto, descripcion, precio, numPlazas, fechaCelebracion, fechaInicio, fechaFinal);
        actividades.put(act.getId(), act);
        return act;
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

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim().toLowerCase();
    }

    private boolean actividadValida(Actividad actividad, String tituloCorto, LocalDate fechaCelebracion) {
        return normalizar(actividad.getTituloCorto()).equals(normalizar(tituloCorto)) && actividad.getFechaCelebracion().equals(fechaCelebracion);
    }

    public Solicitudes crearSolicitud(@NotNull long idActividad, @NotBlank String dniSocio) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) throw new ActividadNoRegistrada();

        // if (actividad.getNumPlazas() <= 0){
        //     throw new PlazasNoDisponibles();
        // }

        Usuario socio = buscarUsuario(dniSocio);

        boolean solicitudExistente = actividad.getSolicitudes().stream()
                .anyMatch(solicitud -> solicitud.getUsuario().getDni().equals(dniSocio));
        if (solicitudExistente) throw new UsuarioYaRegistrado();

        // for(i=0; i< actividad.getSolicitudes.size(); i++) {

        //         if(actividad..getDni() == dniSocio) {
        //             throw new UsuarioYaRegistrado();
        //         }
        // }

        Solicitudes solicitud = new Solicitudes(actividad, socio);
        actividad.altaSolicitud(solicitud);
        return solicitud;
    }

    /*
    public Solicitudes crearSolicitud(@NotBlank long idActividad, @NotBlank String dniSocio){
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }

        // if (actividad.getNumPlazas() <= 0){
        //     throw new PlazasNoDisponibles();
        // }

        Usuario socio = buscarUsuario(dniSocio);
        if (socio == null) {
            throw new UsuarioNoRegistrado();
        }

        // for(i=0; i< actividad.getSolicitudes.size(); i++) {

        //         if(actividad..getDni() == dniSocio) {
        //             throw new UsuarioYaRegistrado();
        //         }
        // }
        Solicitudes solicitudes = new Solicitudes(actividad, socio);
        actividad.altaSolicitud(solicitudes);
        return solicitudes;
    }
     */

    public void modificarSolicitud(@NotNull long idSolicitud, @NotNull long idActividad, @PositiveOrZero int numAcomp) {
        Actividad actividad = actividades.get(idActividad);
        Solicitudes solicitud = actividad.getSolicitudes().stream()
                .filter(s -> s.getId() == idSolicitud) // Comparación directa de tipos primitivos
                .findFirst()
                .orElseThrow(SolicitudNoRegistrada::new);

        solicitud.setNumAcomp(numAcomp);
    }

    public void marcarCuota(@NotNull Usuario user) {
        user.setCuota(true);
    }

    public void asignarPlaza(@NotNull Solicitudes solicitud) {
        Actividad actividad = solicitud.getActividad();
        if (actividad.getNumPlazas() > 0 && solicitud.getUsuario().getCuota()) {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);
            actividad.nuevoSocio(solicitud.getUsuario());
            actividad.borrarSolicitud(solicitud);
        } else {
            solicitud.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);
        }
    }

    public void rechazarPlaza(@NotNull Solicitudes solicitud) {
        solicitud.setEstado(Solicitudes.EstadoSolicitud.RECHAZADA);
        solicitud.getActividad().borrarSolicitud(solicitud);
    }

    public List<Solicitudes> obtenerSolicitudesPendientes(@NotNull long idActividad) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) throw new ActividadNoRegistrada();
        return actividad.getSolicitudesPendientes();
    }

}
