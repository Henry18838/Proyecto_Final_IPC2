package com.connectwork.models;

import java.time.LocalDateTime;

public class Propuesta {
    private int id;
    private int idProyecto;
    private String tituloProyecto;
    private int idFreelancer;
    private String nombreFreelancer;
    private double calificacionFreelancer;
    private double montoOfertado;
    private int plazoDias;
    private String carta;
    private String estado; // PENDIENTE, ACEPTADA, RECHAZADA, RETIRADA
    private LocalDateTime fechaEnvio;

    public Propuesta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }

    public int getIdFreelancer() { return idFreelancer; }
    public void setIdFreelancer(int idFreelancer) { this.idFreelancer = idFreelancer; }

    public String getNombreFreelancer() { return nombreFreelancer; }
    public void setNombreFreelancer(String nombreFreelancer) { this.nombreFreelancer = nombreFreelancer; }

    public double getCalificacionFreelancer() { return calificacionFreelancer; }
    public void setCalificacionFreelancer(double calificacionFreelancer) { this.calificacionFreelancer = calificacionFreelancer; }

    public double getMontoOfertado() { return montoOfertado; }
    public void setMontoOfertado(double montoOfertado) { this.montoOfertado = montoOfertado; }

    public int getPlazoDias() { return plazoDias; }
    public void setPlazoDias(int plazoDias) { this.plazoDias = plazoDias; }

    public String getCarta() { return carta; }
    public void setCarta(String carta) { this.carta = carta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}
