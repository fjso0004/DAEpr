package es.ujaen.dae.sociosclub.rest.dto;
/**
 * Representa una temporada en el sistema.
 *
 * @param anio Año de la temporada.
 * @param numActividades Número de actividades en la temporada.
 */
public record DTemporada(
        int anio,
        int numActividades
) {}
