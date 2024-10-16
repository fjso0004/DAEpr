package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.entidades.Usuario;

import es.ujaen.dae.sociosclub.excepciones.ActividadNoRegistrada;
import es.ujaen.dae.sociosclub.excepciones.ClaveIncorrecta;
import es.ujaen.dae.sociosclub.excepciones.UsuarioNoRegistrado;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
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
        if (usuario == null) {
            throw new UsuarioNoRegistrado();
        }

        if (!usuario.claveValida(clave)){
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
        actividades.put(act.generarIdActividad(), act);
        return act;
    }

    private String normalizar(String texto){
        String str = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        return str.trim().toLowerCase();
    }

    public List<Actividad> buscarActividades(@NotBlank String tituloCorto, @NotNull LocalDate fechaCelebracion){
        List<Actividad> actividadesFiltradas = new ArrayList<>();
        for (Actividad actividad : actividades.values()) {
            if (actividadValida(actividad, tituloCorto, fechaCelebracion)){
                actividadesFiltradas.add(actividad);
            }
        }
        return actividadesFiltradas;
    }

    private boolean actividadValida(Actividad actividad, String tituloCorto, LocalDate fechaCelebracion){
        return normalizar(actividad.getTituloCorto()).equals(normalizar(tituloCorto)) && actividad.getFechaCelebracion().equals(fechaCelebracion);
    }

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


     public void modificarSolicitud(@NotBlank long idSolicitud, @NotBlank long idActividad, @NotBlank int numacomp){
         Actividad actividad = actividades.get(idActividad);
        if(idSolicitud == null) {
            throw new SolicitudNoRegistrada();
        }else{
           Solicitudes solicitud = actividad.solicitudes.get(idSolicitud);
           solicitud.setNumAcomp(numacomp);
        }
      
    }

    public void marcarCuota(@NotBlank Usuario user) {
        
        if(user.getCuota == false){
            user.setCuota(true);
        }
    }

    public void asignarPlaza(@NotNull Solicitudes solicitudes){
        Actividad actividad = solicitudes.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        Usuario socio = solicitudes.getUsuario();
    
        if (socio.getCuota() && actividad.getNumPlazas() > 0){
            solicitudes.setEstado(Solicitudes.EstadoSolicitud.ACEPTADA);
            actividad.borrarSolicitud(solicitudes);
        }else {
            solicitudes.setEstado(Solicitudes.EstadoSolicitud.PENDIENTE);
        }
    }

    public void rechazarPlaza(@NotNull Solicitudes solicitudes){
        Actividad actividad = solicitudes.getActividad();
        solicitudes.setEstado(Solicitudes.EstadoSolicitud.RECHAZADA);
        actividad.borrarSolicitud(solicitudes);
    }

    public List<Solicitudes> obtenerSolicitudesPendientes(@NotBlank long idActividad){
        Actividad actividad = actividades.get(idActividad);
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }

        return actividad.getSolicitudesPendientes();
    }

}





