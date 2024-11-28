package es.ujaen.dae.sociosclub.rest.dto;

import java.time.LocalDate;

public record DActividad(
        int id,
        String tituloCorto,
        String descripcion,
        double precio,
        int numPlazas,
        LocalDate fechaCelebracion,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
