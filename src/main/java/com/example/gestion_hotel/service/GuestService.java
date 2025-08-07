package com.example.gestion_hotel.service;

import com.example.gestion_hotel.dao.GuestDao;
import com.example.gestion_hotel.entity.Guest;
import com.example.gestion_hotel.exception.ServiceException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GuestService {
    private final GuestDao guestDAO;

    public GuestService() {
        this.guestDAO = new GuestDao();
    }

    public Guest createGuest(String nombre, String email, String telefono) throws ServiceException {
        // Validaciones de negocio
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ServiceException("El nombre del huésped es obligatorio");
        }

        if (nombre.trim().length() < 2) {
            throw new ServiceException("El nombre debe tener al menos 2 caracteres");
        }

        Guest guest = new Guest(nombre.trim(), email, telefono);

        if (!guest.validateEmail()) {
            throw new ServiceException("El formato del email no es válido");
        }

        if (!guest.validatePhone()) {
            throw new ServiceException("El formato del teléfono no es válido (9-15 dígitos)");
        }

        try {
            // Verificar unicidad del email
            Optional<Guest> existingGuest = guestDAO.findByEmail(email);
            if (existingGuest.isPresent()) {
                throw new ServiceException("Ya existe un huésped registrado con este email");
            }

            return guestDAO.create(guest);

        } catch (SQLException e) {
            throw new ServiceException("Error al crear el huésped en la base de datos", e);
        }
    }

    public Optional<Guest> findGuestById(Long id) throws ServiceException {
        if (id == null || id <= 0) {
            throw new ServiceException("El ID del huésped debe ser un número positivo");
        }

        try {
            return guestDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el huésped", e);
        }
    }

    public Optional<Guest> findGuestByEmail(String email) throws ServiceException {
        if (email == null || email.trim().isEmpty()) {
            throw new ServiceException("El email no puede estar vacío");
        }

        try {
            return guestDAO.findByEmail(email);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el huésped por email", e);
        }
    }

    public List<Guest> getAllGuests() throws ServiceException {
        try {
            return guestDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener la lista de huéspedes", e);
        }
    }

    public Guest updateGuest(Guest guest) throws ServiceException {
        if (guest == null || guest.getId() == null) {
            throw new ServiceException("El huésped y su ID son obligatorios para actualizar");
        }

        // Verificar que el huésped existe
        Optional<Guest> existingGuest = findGuestById(guest.getId());
        if (!existingGuest.isPresent()) {
            throw new ServiceException("No existe un huésped con ID: " + guest.getId());
        }

        // Validaciones
        if (!guest.validateEmail()) {
            throw new ServiceException("El formato del email no es válido");
        }

        if (!guest.validatePhone()) {
            throw new ServiceException("El formato del teléfono no es válido");
        }

        try {
            // Verificar unicidad del email (excluyendo el huésped actual)
            Optional<Guest> guestWithSameEmail = guestDAO.findByEmail(guest.getEmail());
            if (guestWithSameEmail.isPresent() &&
                    !guestWithSameEmail.get().getId().equals(guest.getId())) {
                throw new ServiceException("Ya existe otro huésped con este email");
            }

            return guestDAO.update(guest);

        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el huésped", e);
        }
    }

    public boolean deleteGuest(Long id) throws ServiceException {
        if (id == null || id <= 0) {
            throw new ServiceException("El ID del huésped debe ser un número positivo");
        }

        // Verificar que el huésped existe
        Optional<Guest> existingGuest = findGuestById(id);
        if (!existingGuest.isPresent()) {
            throw new ServiceException("No existe un huésped con ID: " + id);
        }

        try {
            return guestDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el huésped", e);
        }
    }
}
