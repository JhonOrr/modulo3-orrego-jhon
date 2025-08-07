package com.example.gestion_hotel.dao;

import com.example.gestion_hotel.entity.Guest;
import com.example.gestion_hotel.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuestDao {
    private final DatabaseConnection dbConnection;

    public GuestDao() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public Guest create(Guest guest) throws SQLException {
        String sql = "INSERT INTO guests (nombre, email, telefono, fecha_registro) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, guest.getNombre());
            stmt.setString(2, guest.getEmail());
            stmt.setString(3, guest.getTelefono());
            stmt.setTimestamp(4, Timestamp.valueOf(guest.getFechaRegistro()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al crear el huésped, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    guest.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al crear el huésped, no se generó ID.");
                }
            }
        }
        return guest;
    }

    public Optional<Guest> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM guests WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Guest> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM guests WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Guest> findAll() throws SQLException {
        String sql = "SELECT * FROM guests ORDER BY nombre";
        List<Guest> guests = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
        }
        return guests;
    }

    public Guest update(Guest guest) throws SQLException {
        String sql = "UPDATE guests SET nombre = ?, email = ?, telefono = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getNombre());
            stmt.setString(2, guest.getEmail());
            stmt.setString(3, guest.getTelefono());
            stmt.setLong(4, guest.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al actualizar el huésped, no existe el ID: " + guest.getId());
            }
        }
        return guest;
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM guests WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getLong("id"));
        guest.setNombre(rs.getString("nombre"));
        guest.setEmail(rs.getString("email"));
        guest.setTelefono(rs.getString("telefono"));

        Timestamp timestamp = rs.getTimestamp("fecha_registro");
        if (timestamp != null) {
            guest.setFechaRegistro(timestamp.toLocalDateTime());
        }

        return guest;
    }
}
