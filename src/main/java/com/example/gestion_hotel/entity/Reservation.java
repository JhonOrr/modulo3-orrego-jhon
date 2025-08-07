package com.example.gestion_hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private Long id;
    private Long guestId;
    private Long roomId;
    private LocalDate fechaCheckIn;
    private LocalDate fechaCheckOut;
    private Integer numeroHuespedes;
    private BigDecimal montoTotal;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;

    public Reservation() {
        this.estado = EstadoReserva.ACTIVA;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Reservation(Long guestId, Long roomId, LocalDate checkIn,
                       LocalDate checkOut, Integer numeroHuespedes) {
        this();
        this.guestId = guestId;
        this.roomId = roomId;
        this.fechaCheckIn = checkIn;
        this.fechaCheckOut = checkOut;
        this.numeroHuespedes = numeroHuespedes;
    }

    public long calcularDuracionNoches() {
        if (fechaCheckIn != null && fechaCheckOut != null) {
            return ChronoUnit.DAYS.between(fechaCheckIn, fechaCheckOut);
        }
        return 0;
    }

    public boolean validarFechas() {
        if (fechaCheckIn == null || fechaCheckOut == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return fechaCheckIn.isAfter(hoy.minusDays(1)) &&
                fechaCheckOut.isAfter(fechaCheckIn);
    }

    public void calcularMontoTotal(BigDecimal precioPorNoche) {
        if (precioPorNoche != null && validarFechas()) {
            long noches = calcularDuracionNoches();
            this.montoTotal = precioPorNoche.multiply(BigDecimal.valueOf(noches));
        }
    }

    public boolean puedeSerCancelada() {
        return estado == EstadoReserva.ACTIVA &&
                fechaCheckIn.isAfter(LocalDate.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getFechaCheckIn() { return fechaCheckIn; }
    public void setFechaCheckIn(LocalDate fechaCheckIn) {
        this.fechaCheckIn = fechaCheckIn;
    }

    public LocalDate getFechaCheckOut() { return fechaCheckOut; }
    public void setFechaCheckOut(LocalDate fechaCheckOut) {
        this.fechaCheckOut = fechaCheckOut;
    }

    public Integer getNumeroHuespedes() { return numeroHuespedes; }
    public void setNumeroHuespedes(Integer numeroHuespedes) {
        this.numeroHuespedes = numeroHuespedes;
    }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return String.format("Reservation{id=%d, guest=%d, room=%d, %s to %s}",
                id, guestId, roomId, fechaCheckIn, fechaCheckOut);
    }
}
