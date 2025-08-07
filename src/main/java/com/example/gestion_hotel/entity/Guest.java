package com.example.gestion_hotel.entity;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Guest {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDateTime fechaRegistro;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    public Guest() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Guest(String nombre, String email, String telefono) {
        this();
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public boolean validateEmail() {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean validatePhone() {
        return telefono != null && telefono.matches("^\\+?[0-9]{9,15}$");
    }

    public String getContactInfo() {
        return String.format("%s - %s - %s", nombre, email, telefono);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return String.format("Guest{id=%d, nombre='%s', email='%s'}",
                id, nombre, email);
    }
}
