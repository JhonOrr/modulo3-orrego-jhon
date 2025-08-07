package com.example.gestion_hotel.entity;

public enum TipoHabitacion {
    SIMPLE("Simple", 1, 2),
    DOBLE("Doble", 2, 4),
    SUITE("Suite", 2, 6);

    private final String descripcion;
    private final int capacidadMinima;
    private final int capacidadMaxima;

    TipoHabitacion(String descripcion, int capacidadMinima, int capacidadMaxima) {
        this.descripcion = descripcion;
        this.capacidadMinima = capacidadMinima;
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getDescripcion() { return descripcion; }
    public int getCapacidadMinima() { return capacidadMinima; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
}
