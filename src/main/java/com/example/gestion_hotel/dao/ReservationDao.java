package com.example.gestion_hotel.dao;

import com.example.gestion_hotel.entity.EstadoReserva;
import com.example.gestion_hotel.entity.Reservation;
import com.example.gestion_hotel.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDao {

    private final DatabaseConnection dbConnection;

    public ReservationDao() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public Reservation create(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (guest_id, room_id, fecha_check_in, fecha_check_out, numero_huespedes, monto_total, estado, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, reservation.getGuestId());
            stmt.setLong(2, reservation.getRoomId());
            stmt.setDate(3, Date.valueOf(reservation.getFechaCheckIn()));
            stmt.setDate(4, Date.valueOf(reservation.getFechaCheckOut()));
            stmt.setInt(5, reservation.getNumeroHuespedes());
            stmt.setBigDecimal(6, reservation.getMontoTotal());
            stmt.setString(7, reservation.getEstado().name());
            stmt.setTimestamp(8, Timestamp.valueOf(reservation.getFechaCreacion()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al crear la reserva, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al crear la reserva, no se generó ID.");
                }
            }
        }
        return reservation;
    }

    public Optional<Reservation> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Reservation> findByGuestId(Long guestId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE guest_id = ? ORDER BY fecha_creacion DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        }
        return reservations;
    }

    public List<Reservation> findByRoomId(Long roomId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE room_id = ? ORDER BY fecha_check_in";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        }
        return reservations;
    }

    public List<Reservation> findActiveReservations() throws SQLException {
        String sql = "SELECT * FROM reservations WHERE estado = 'ACTIVA' ORDER BY fecha_check_in";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    public Reservation update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations SET fecha_check_in = ?, fecha_check_out = ?, numero_huespedes = ?, monto_total = ?, estado = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(reservation.getFechaCheckIn()));
            stmt.setDate(2, Date.valueOf(reservation.getFechaCheckOut()));
            stmt.setInt(3, reservation.getNumeroHuespedes());
            stmt.setBigDecimal(4, reservation.getMontoTotal());
            stmt.setString(5, reservation.getEstado().name());
            stmt.setLong(6, reservation.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al actualizar la reserva, no existe el ID: " + reservation.getId());
            }
        }
        return reservation;
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setGuestId(rs.getLong("guest_id"));
        reservation.setRoomId(rs.getLong("room_id"));

        Date checkInDate = rs.getDate("fecha_check_in");
        if (checkInDate != null) {
            reservation.setFechaCheckIn(checkInDate.toLocalDate());
        }

        Date checkOutDate = rs.getDate("fecha_check_out");
        if (checkOutDate != null) {
            reservation.setFechaCheckOut(checkOutDate.toLocalDate());
        }

        reservation.setNumeroHuespedes(rs.getInt("numero_huespedes"));
        reservation.setMontoTotal(rs.getBigDecimal("monto_total"));
        reservation.setEstado(EstadoReserva.valueOf(rs.getString("estado")));

        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            reservation.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return reservation;
    }
}
