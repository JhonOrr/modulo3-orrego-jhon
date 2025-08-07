package com.example.gestion_hotel.entity;

public enum EstadoReserva {
    ACTIVA("Activa", "Reserva confirmada y vigente"),
    CANCELADA("Cancelada", "Reserva cancelada por el cliente o hotel"),
    COMPLETADA("Completada", "Estadía finalizada exitosamente");

    private final String nombre;
    private final String descripcion;

    EstadoReserva(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
