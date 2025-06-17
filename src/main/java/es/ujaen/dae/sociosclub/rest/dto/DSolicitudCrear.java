package es.ujaen.dae.sociosclub.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DSolicitudCrear(
        @NotBlank String dniUsuario,
        @Min(0) @Max(5) int numAcomp
) {}