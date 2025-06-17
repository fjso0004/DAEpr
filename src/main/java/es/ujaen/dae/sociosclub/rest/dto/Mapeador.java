package es.ujaen.dae.sociosclub.rest.dto;
import es.ujaen.dae.sociosclub.entidades.*;
import es.ujaen.dae.sociosclub.excepciones.ActividadNoRegistrada;
import es.ujaen.dae.sociosclub.excepciones.UsuarioNoRegistrado;
import es.ujaen.dae.sociosclub.repositorios.RepositorioActividad;
import es.ujaen.dae.sociosclub.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Mapeador {

    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private RepositorioActividad repositorioActividad;
    @Autowired
    PasswordEncoder codificadorClaves;


    public DUsuario dto(Usuario usuario) {
        // No exponer la clave real: usamos empty o null según convención
        String claveResponse = "";
        return new DUsuario(
                usuario.getDni(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getDireccion(),
                usuario.getTlf(),
                usuario.getEmail(),
                usuario.isAdministrador(),
                usuario.isCuotaPagada(),
                claveResponse
        );
    }

    public Usuario entidad(DUsuario dUsuario) {
        // El constructor de Usuario que definimos ahora es:
        // Usuario(String dni, String nombre, String apellidos, String direccion, String tlf, String email, String clave, boolean administrador, boolean cuotaPagada)
        return new Usuario(
                dUsuario.dni(),
                dUsuario.nombre(),
                dUsuario.apellidos(),
                dUsuario.direccion(),
                dUsuario.tlf(),
                dUsuario.email(),
                dUsuario.clave(),
                dUsuario.cuotaPagada(),
                dUsuario.administrador()
        );
    }

    public DActividad dto(Actividad actividad) {
        return new DActividad(
                actividad.getId(),
                actividad.getTituloCorto(),
                actividad.getDescripcion(),
                actividad.getPrecio(),
                actividad.getNumPlazas(),
                actividad.getFechaCelebracion(),
                actividad.getFechaInicio(),
                actividad.getFechaFin()
        );
    }

    public Actividad entidad(DActividad dActividad) {
        return new Actividad(
                dActividad.tituloCorto(),
                dActividad.descripcion(),
                dActividad.precio(),
                dActividad.numPlazas(),
                dActividad.fechaCelebracion(),
                dActividad.fechaInicio(),
                dActividad.fechaFin()
        );
    }

    public DSolicitud dto(Solicitudes solicitud) {
        return new DSolicitud(
                solicitud.getId(),
                solicitud.getNumAcomp(),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado().name(),
                solicitud.getUsuario().getDni(),
                solicitud.getActividad().getId()
        );
    }

    public Solicitudes entidad(DSolicitud dSolicitud) {
        Usuario usuario = repositorioUsuario.buscarPorDni(dSolicitud.dniUsuario())
                .orElseThrow(UsuarioNoRegistrado::new);

        Actividad actividad = repositorioActividad.buscarPorId(dSolicitud.idActividad())
                .orElseThrow(() -> new ActividadNoRegistrada());

        return new Solicitudes(actividad, usuario, dSolicitud.numAcomp());
    }

    public DTemporada dto(Temporada temporada) {
        return new DTemporada(temporada.getAnio(), temporada.getActividades().size());
    }

    public Temporada entidad(DTemporada dTemporada) {
        return new Temporada(dTemporada.anio());
    }

    public DUsuario dtoDeUsuario(Usuario u) {
        // No exponer la clave real en la respuesta; usar empty o null según convención
        String claveResponse = "";
        return new DUsuario(
                u.getDni(),
                u.getNombre(),
                u.getApellidos(),
                u.getDireccion(),
                u.getTlf(),
                u.getEmail(),
                u.isAdministrador(),
                u.isCuotaPagada(),
                claveResponse
        );
    }
}