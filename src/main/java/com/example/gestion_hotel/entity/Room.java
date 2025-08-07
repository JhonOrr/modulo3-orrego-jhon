package com.example.gestion_hotel.entity;

import java.math.BigDecimal;

public class Room {
    private Long id;
    private String numero;
    private TipoHabitacion tipo;
    private BigDecimal precioPorNoche;
    private Integer capacidadMaxima;
    private Boolean disponible;

    public Room() {
        this.disponible = true;
    }

    public Room(String numero, TipoHabitacion tipo, BigDecimal precioPorNoche) {
        this();
        this.numero = numero;
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
        this.capacidadMaxima = tipo.getCapacidadMaxima();
    }

    public BigDecimal calcularPrecioTotal(int noches) {
        if (noches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor a 0");
        }
        return precioPorNoche.multiply(BigDecimal.valueOf(noches));
    }

    public boolean puedeAlojar(int numeroHuespedes) {
        return numeroHuespedes > 0 && numeroHuespedes <= capacidadMaxima;
    }

    public boolean isAvailable() {
        return disponible != null && disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public TipoHabitacion getTipo() { return tipo; }
    public void setTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
        if (tipo != null) {
            this.capacidadMaxima = tipo.getCapacidadMaxima();
        }
    }

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Boolean getDisponible() { return disponible; }

    @Override
    public String toString() {
        return String.format("Room{id=%d, numero='%s', tipo=%s, precio=%s}",
                id, numero, tipo, precioPorNoche);
    }
}
