package com.example.gestion_hotel.dao;

import com.example.gestion_hotel.entity.Room;
import com.example.gestion_hotel.entity.TipoHabitacion;
import com.example.gestion_hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao {
    private final DatabaseConnection dbConnection;

    public RoomDao() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public Room create(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (numero, tipo, precio_por_noche, capacidad_maxima, disponible) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, room.getNumero());
            stmt.setString(2, room.getTipo().name());
            stmt.setBigDecimal(3, room.getPrecioPorNoche());
            stmt.setInt(4, room.getCapacidadMaxima());
            stmt.setBoolean(5, room.getDisponible());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al crear la habitación, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al crear la habitación, no se generó ID.");
                }
            }
        }
        return room;
    }

    public Optional<Room> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int numGuests) throws SQLException {
        String sql = """
            SELECT r.* FROM rooms r 
            WHERE r.disponible = true 
            AND r.capacidad_maxima >= ? 
            AND r.id NOT IN (
                SELECT res.room_id FROM reservations res 
                WHERE res.estado = 'ACTIVA' 
                AND ((res.fecha_check_in BETWEEN ? AND ?) 
                OR (res.fecha_check_out BETWEEN ? AND ?)
                OR (res.fecha_check_in <= ? AND res.fecha_check_out >= ?))
            )
            ORDER BY r.precio_por_noche
            """;

        List<Room> availableRooms = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numGuests);
            stmt.setDate(2, Date.valueOf(checkIn));
            stmt.setDate(3, Date.valueOf(checkOut));
            stmt.setDate(4, Date.valueOf(checkIn));
            stmt.setDate(5, Date.valueOf(checkOut));
            stmt.setDate(6, Date.valueOf(checkIn));
            stmt.setDate(7, Date.valueOf(checkOut));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    availableRooms.add(mapResultSetToRoom(rs));
                }
            }
        }
        return availableRooms;
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT * FROM rooms ORDER BY numero";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    public Room update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET numero = ?, tipo = ?, precio_por_noche = ?, capacidad_maxima = ?, disponible = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getNumero());
            stmt.setString(2, room.getTipo().name());
            stmt.setBigDecimal(3, room.getPrecioPorNoche());
            stmt.setInt(4, room.getCapacidadMaxima());
            stmt.setBoolean(5, room.getDisponible());
            stmt.setLong(6, room.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Error al actualizar la habitación, no existe el ID: " + room.getId());
            }
        }
        return room;
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM rooms WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setNumero(rs.getString("numero"));
        room.setTipo(TipoHabitacion.valueOf(rs.getString("tipo")));
        room.setPrecioPorNoche(rs.getBigDecimal("precio_por_noche"));
        room.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
        room.setDisponible(rs.getBoolean("disponible"));
        return room;
    }
}
