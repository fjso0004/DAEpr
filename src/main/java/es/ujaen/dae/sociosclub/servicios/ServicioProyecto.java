package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.swing.*;
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

    private static final Usuario administrador = new Usuario("12345678A", "admin", "-", "-", "659123456",
            "admin@sociosclub.es", "SuperUser", true);

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

    public boolean login(@NotBlank String dni, @NotBlank String clave) {
        Usuario usuario = usuarios.get(dni);

        if (usuario == null || !usuario.getDni().equals(dni) || !usuario.getClave().equals(clave)) {
            throw new ClaveoUsuarioIncorrecto("Usuario o clave incorrecto");
        }
        return true;
    }

    public Usuario buscarUsuario(Usuario administrador, @NotBlank String dni) {
        if (administrador.getNombre().equals("admin")){
            Usuario usuario = usuarios.get(dni);
            if (usuario == null) {
                throw new UsuarioYaRegistrado();
            }
            return usuario;

        }else {
            throw new OperacionDeAdmin();
        }
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

    public List<Actividad> buscarActividades() {
        List<Actividad> actividadesFiltradas = new ArrayList<>();
        for (Actividad actividad : actividades.values()) {
            if (actividadValida(actividad)) {
                actividadesFiltradas.add(actividad);
            }
        }
        return actividadesFiltradas;
    }
    

    private boolean actividadValida(Actividad actividad) {
        return actividad.getFechaCelebracion().isAfter(LocalDate.now());
        }

    public void crearSolicitud(@Positive long idActividad, @NotBlank String dniSocio, @Min(0) @Max(5) int num_acomp) {
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
        //acumula número acompañantes
        var acompTotales= num_acomp;

        Solicitudes solicitud = new Solicitudes(actividad, socio, num_acomp, estadoSolicitud);
        actividad.altaSolicitud(solicitud);
//        return solicitud;
        }


    public void modificarSolicitud(@Positive long idActividad, @Positive long idSolicitud, @Positive int numAcomp) {
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
/*
        Solicitudes solicitud = actividad.getSolicitudes().stream()
                .filter(s -> s.getId() == idSolicitud)
                .findFirst()
                .orElseThrow(SolicitudNoRegistrada::new);

        solicitud.setNumAcomp(numAcomp);
 */
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
//            actividad.nuevoSocio(socio);
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
