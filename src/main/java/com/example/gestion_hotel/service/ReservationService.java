package com.example.gestion_hotel.service;

import com.example.gestion_hotel.dao.ReservationDao;
import com.example.gestion_hotel.entity.EstadoReserva;
import com.example.gestion_hotel.entity.Guest;
import com.example.gestion_hotel.entity.Reservation;
import com.example.gestion_hotel.entity.Room;
import com.example.gestion_hotel.exception.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationService {
    private final ReservationDao reservationDAO;
    private final GuestService guestService;
    private final RoomService roomService;

    public ReservationService() {
        this.reservationDAO = new ReservationDao();
        this.guestService = new GuestService();
        this.roomService = new RoomService();
    }

    public Reservation createReservation(Long guestId, Long roomId, LocalDate checkIn,
                                         LocalDate checkOut, Integer numGuests) throws ServiceException {

        // Validaciones de entrada
        validateReservationInput(guestId, roomId, checkIn, checkOut, numGuests);

        // Verificar que el huésped existe
        Optional<Guest> guest = guestService.findGuestById(guestId);
        if (!guest.isPresent()) {
            throw new ServiceException("No existe un huésped con ID: " + guestId);
        }

        // Verificar que la habitación existe
        Optional<Room> room = roomService.findRoomById(roomId);
        if (!room.isPresent()) {
            throw new ServiceException("No existe una habitación con ID: " + roomId);
        }

        // Validar capacidad de la habitación
        if (!room.get().puedeAlojar(numGuests)) {
            throw new ServiceException("La habitación no tiene capacidad suficiente para " + numGuests + " huéspedes");
        }

        // Verificar disponibilidad de la habitación en las fechas solicitadas
        List<Room> availableRooms = roomService.findAvailableRooms(checkIn, checkOut, numGuests);
        boolean isRoomAvailable = availableRooms.stream()
                .anyMatch(r -> r.getId().equals(roomId));

        if (!isRoomAvailable) {
            throw new ServiceException("La habitación no está disponible en las fechas solicitadas");
        }

        // Crear la reserva
        Reservation reservation = new Reservation(guestId, roomId, checkIn, checkOut, numGuests);

        // Calcular el monto total
        BigDecimal totalAmount = roomService.calculateTotalPrice(roomId, checkIn, checkOut);
        reservation.setMontoTotal(totalAmount);

        try {
            return reservationDAO.create(reservation);
        } catch (SQLException e) {
            throw new ServiceException("Error al crear la reserva en la base de datos", e);
        }
    }

    public Optional<Reservation> findReservationById(Long id) throws ServiceException {
        if (id == null || id <= 0) {
            throw new ServiceException("El ID de la reserva debe ser un número positivo");
        }

        try {
            return reservationDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la reserva", e);
        }
    }

    public List<Reservation> findReservationsByGuest(Long guestId) throws ServiceException {
        if (guestId == null || guestId <= 0) {
            throw new ServiceException("El ID del huésped debe ser un número positivo");
        }

        try {
            return reservationDAO.findByGuestId(guestId);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar las reservas del huésped", e);
        }
    }

    public List<Reservation> findReservationsByRoom(Long roomId) throws ServiceException {
        if (roomId == null || roomId <= 0) {
            throw new ServiceException("El ID de la habitación debe ser un número positivo");
        }

        try {
            return reservationDAO.findByRoomId(roomId);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar las reservas de la habitación", e);
        }
    }

    public List<Reservation> getActiveReservations() throws ServiceException {
        try {
            return reservationDAO.findActiveReservations();
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener las reservas activas", e);
        }
    }

    public Reservation updateReservation(Long reservationId, LocalDate newCheckIn,
                                         LocalDate newCheckOut, Integer newNumGuests) throws ServiceException {

        // Buscar la reserva existente
        Optional<Reservation> existingReservation = findReservationById(reservationId);
        if (!existingReservation.isPresent()) {
            throw new ServiceException("No existe una reserva con ID: " + reservationId);
        }

        Reservation reservation = existingReservation.get();

        // Verificar que la reserva puede ser modificada
        if (reservation.getEstado() != EstadoReserva.ACTIVA) {
            throw new ServiceException("Solo se pueden modificar reservas activas");
        }

        // Validar nuevas fechas
        if (newCheckIn != null && newCheckOut != null) {
            if (newCheckIn.isAfter(newCheckOut) || newCheckIn.isBefore(LocalDate.now())) {
                throw new ServiceException("Las nuevas fechas no son válidas");
            }

            // Verificar disponibilidad con las nuevas fechas
            List<Room> availableRooms = roomService.findAvailableRooms(newCheckIn, newCheckOut,
                    newNumGuests != null ? newNumGuests : reservation.getNumeroHuespedes());
            boolean isRoomAvailable = availableRooms.stream()
                    .anyMatch(r -> r.getId().equals(reservation.getRoomId()));

            if (!isRoomAvailable) {
                throw new ServiceException("La habitación no está disponible en las nuevas fechas");
            }

            reservation.setFechaCheckIn(newCheckIn);
            reservation.setFechaCheckOut(newCheckOut);
        }

        if (newNumGuests != null) {
            // Verificar capacidad
            Optional<Room> room = roomService.findRoomById(reservation.getRoomId());
            if (room.isPresent() && !room.get().puedeAlojar(newNumGuests)) {
                throw new ServiceException("La habitación no tiene capacidad para " + newNumGuests + " huéspedes");
            }
            reservation.setNumeroHuespedes(newNumGuests);
        }

        // Recalcular el monto total
        BigDecimal newTotal = roomService.calculateTotalPrice(reservation.getRoomId(),
                reservation.getFechaCheckIn(),
                reservation.getFechaCheckOut());
        reservation.setMontoTotal(newTotal);

        try {
            return reservationDAO.update(reservation);
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar la reserva", e);
        }
    }

    public boolean cancelReservation(Long reservationId) throws ServiceException {
        Optional<Reservation> existingReservation = findReservationById(reservationId);
        if (!existingReservation.isPresent()) {
            throw new ServiceException("No existe una reserva con ID: " + reservationId);
        }

        Reservation reservation = existingReservation.get();

        // Verificar que la reserva puede ser cancelada
        if (!reservation.puedeSerCancelada()) {
            throw new ServiceException("Esta reserva no puede ser cancelada (debe estar activa y ser futura)");
        }

        reservation.setEstado(EstadoReserva.CANCELADA);

        try {
            reservationDAO.update(reservation);
            return true;
        } catch (SQLException e) {
            throw new ServiceException("Error al cancelar la reserva", e);
        }
    }

    private void validateReservationInput(Long guestId, Long roomId, LocalDate checkIn,
                                          LocalDate checkOut, Integer numGuests) throws ServiceException {
        if (guestId == null || guestId <= 0) {
            throw new ServiceException("El ID del huésped debe ser un número positivo");
        }

        if (roomId == null || roomId <= 0) {
            throw new ServiceException("El ID de la habitación debe ser un número positivo");
        }

        if (checkIn == null || checkOut == null) {
            throw new ServiceException("Las fechas de check-in y check-out son obligatorias");
        }

        if (checkIn.isAfter(checkOut)) {
            throw new ServiceException("La fecha de check-in debe ser anterior a la de check-out");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new ServiceException("La fecha de check-in no puede ser en el pasado");
        }

        if (numGuests == null || numGuests <= 0 || numGuests > 10) {
            throw new ServiceException("El número de huéspedes debe estar entre 1 y 10");
        }
    }
}
