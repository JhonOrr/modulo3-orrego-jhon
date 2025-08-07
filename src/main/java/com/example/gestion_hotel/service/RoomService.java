package com.example.gestion_hotel.service;

import com.example.gestion_hotel.dao.RoomDao;
import com.example.gestion_hotel.entity.Room;
import com.example.gestion_hotel.entity.TipoHabitacion;
import com.example.gestion_hotel.exception.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RoomService {
    private final RoomDao roomDAO;
    private static final BigDecimal PRECIO_MINIMO = new BigDecimal("50.00");

    public RoomService() {
        this.roomDAO = new RoomDao();
    }

    public Room createRoom(String numero, TipoHabitacion tipo, BigDecimal precioPorNoche) throws ServiceException {
        // Validaciones de negocio
        if (numero == null || numero.trim().isEmpty()) {
            throw new ServiceException("El número de habitación es obligatorio");
        }

        if (tipo == null) {
            throw new ServiceException("El tipo de habitación es obligatorio");
        }

        if (precioPorNoche == null || precioPorNoche.compareTo(PRECIO_MINIMO) < 0) {
            throw new ServiceException("El precio por noche debe ser al menos " + PRECIO_MINIMO);
        }

        Room room = new Room(numero.trim(), tipo, precioPorNoche);

        try {
            return roomDAO.create(room);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new ServiceException("Ya existe una habitación con este número");
            }
            throw new ServiceException("Error al crear la habitación en la base de datos", e);
        }
    }

    public Optional<Room> findRoomById(Long id) throws ServiceException {
        if (id == null || id <= 0) {
            throw new ServiceException("El ID de la habitación debe ser un número positivo");
        }

        try {
            return roomDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la habitación", e);
        }
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int numGuests) throws ServiceException {
        // Validaciones
        if (checkIn == null || checkOut == null) {
            throw new ServiceException("Las fechas de check-in y check-out son obligatorias");
        }

        if (checkIn.isAfter(checkOut) || checkIn.isBefore(LocalDate.now())) {
            throw new ServiceException("Las fechas no son válidas");
        }

        if (numGuests <= 0 || numGuests > 10) {
            throw new ServiceException("El número de huéspedes debe estar entre 1 y 10");
        }

        try {
            return roomDAO.findAvailableRooms(checkIn, checkOut, numGuests);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar habitaciones disponibles", e);
        }
    }

    public List<Room> getAllRooms() throws ServiceException {
        try {
            return roomDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener la lista de habitaciones", e);
        }
    }

    public Room updateRoom(Room room) throws ServiceException {
        if (room == null || room.getId() == null) {
            throw new ServiceException("La habitación y su ID son obligatorios para actualizar");
        }

        // Verificar que la habitación existe
        Optional<Room> existingRoom = findRoomById(room.getId());
        if (!existingRoom.isPresent()) {
            throw new ServiceException("No existe una habitación con ID: " + room.getId());
        }

        // Validaciones
        if (room.getPrecioPorNoche().compareTo(PRECIO_MINIMO) < 0) {
            throw new ServiceException("El precio por noche debe ser al menos " + PRECIO_MINIMO);
        }

        try {
            return roomDAO.update(room);
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar la habitación", e);
        }
    }

    public boolean deleteRoom(Long id) throws ServiceException {
        if (id == null || id <= 0) {
            throw new ServiceException("El ID de la habitación debe ser un número positivo");
        }

        Optional<Room> existingRoom = findRoomById(id);
        if (!existingRoom.isPresent()) {
            throw new ServiceException("No existe una habitación con ID: " + id);
        }

        try {
            return roomDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar la habitación", e);
        }
    }

    public BigDecimal calculateTotalPrice(Long roomId, LocalDate checkIn, LocalDate checkOut) throws ServiceException {
        Optional<Room> room = findRoomById(roomId);
        if (!room.isPresent()) {
            throw new ServiceException("Habitación no encontrada");
        }

        if (checkIn == null || checkOut == null || checkIn.isAfter(checkOut)) {
            throw new ServiceException("Fechas inválidas");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.get().getPrecioPorNoche().multiply(BigDecimal.valueOf(nights));
    }
}
