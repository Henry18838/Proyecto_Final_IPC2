package com.connectwork.models;

import java.time.LocalDateTime;

public class Entrega {
    private int id;
    private int idContrato;
    private String descripcion;
    private String archivos;
    private String estado; // PENDIENTE, APROBADA, RECHAZADA
    private String motivoRechazo;
    private LocalDateTime fechaEntrega;

    public Entrega() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getArchivos() { return archivos; }
    public void setArchivos(String archivos) { this.archivos = archivos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}