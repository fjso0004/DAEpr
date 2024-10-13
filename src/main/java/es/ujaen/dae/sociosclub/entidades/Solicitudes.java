package es.ujaen.dae.sociosclub.entidades;

public class Solicitudes {
    int id;
    int num_acomp;


    public Solicitudes(int id, int num_acomp) {
        this.id = id;
        this.num_acomp= num_acomp;
    }

    public int getId() {
        return id;
    }

    public int getNumAcomp() {
        return num_acomp;
    }
}
