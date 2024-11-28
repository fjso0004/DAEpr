package es.ujaen.dae.sociosclub.rest.dto;
import java.time.LocalDate;
/**
 * Representa una solicitud en el sistema.
 *
 * @param id Identificador único de la solicitud.
 * @param numAcomp Número de acompañantes.
 * @param fechaSolicitud Fecha de creación de la solicitud.
 * @param estado Estado de la solicitud (PENDIENTE, ACEPTADA, RECHAZADA).
 * @param dniUsuario DNI del usuario asociado.
 * @param idActividad Identificador de la actividad relacionada.
 */
public record DSolicitud(
        long id,
        int numAcomp,
        LocalDate fechaSolicitud,
        String estado,
        String dniUsuario,
        int idActividad
) {}
