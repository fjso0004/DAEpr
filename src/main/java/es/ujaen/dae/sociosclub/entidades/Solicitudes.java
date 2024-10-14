package es.ujaen.dae.sociosclub.entidades;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class Solicitudes {
    private long id = 0;

    @Min(value = 0)
    @Max(value = 5)
    private int num_acomp;


    public Solicitudes(long id, int num_acomp) {
        this.id = generarIdSolicitud();
        this.num_acomp= num_acomp;
    }

    public long getId() {
        return id;
    }

    public int getNumAcomp() {
        return num_acomp;
    }

    private long generarIdSolicitud(){
        id++;
        return id;
    }
}
