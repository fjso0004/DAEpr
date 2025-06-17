package es.ujaen.dae.sociosclub.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
public record DSolicitudActualizar(
        @Min(0) @Max(5) int numAcomp
) {}

