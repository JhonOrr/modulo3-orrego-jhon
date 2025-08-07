package com.example.gestion_hotel;

import com.example.gestion_hotel.entity.EstadoReserva;
import com.example.gestion_hotel.entity.Guest;
import com.example.gestion_hotel.entity.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Tag(name = "Reservas", description = "Operaciones simuladas relacionadas con reservas")
public class ReservaControllerSimulado {

    @Operation(
            summary = "Crear una nueva Cliente",
            description = "Simula la creación de un nuevo cliente con los datos proporcionados.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cliente creado correctamente",
                            content = @Content(
                                    schema = @Schema(implementation = Guest.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Datos de reserva inválidos")
            }
    )
    public Guest crearCliente(
            @Parameter(description = "Objeto cliente con datos completos")
            Guest cliente
    ) {
        cliente.setId(1L);
        cliente.setEmail("cliente@hotel.com");
        cliente.setNombre("Jhon");
        cliente.setTelefono("2323423423");
        cliente.setFechaRegistro(LocalDateTime.now());

        return cliente;
    }



    @Operation(
            summary = "Obtener todos los clientes",
            description = "Simula la obtención de todos los clientes registrados",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de clientes obtenida correctamente",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = Guest.class))
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "Error interno al obtener los clientes")
            }
    )
    public List<Guest> obtenerClientes() {
        List<Guest> clientes = new ArrayList<>();

        Guest cliente1 = new Guest();
        cliente1.setId(1L);
        cliente1.setNombre("Jhon");
        cliente1.setEmail("jhon@hotel.com");
        cliente1.setTelefono("123456789");
        cliente1.setFechaRegistro(LocalDateTime.now());

        Guest cliente2 = new Guest();
        cliente2.setId(2L);
        cliente2.setNombre("Ana");
        cliente2.setEmail("ana@hotel.com");
        cliente2.setTelefono("987654321");
        cliente2.setFechaRegistro(LocalDateTime.now());

        clientes.add(cliente1);
        clientes.add(cliente2);

        return clientes;
    }


    @Operation(
            summary = "Obtener reservaciones activas",
            description = "Devuelve una lista de reservaciones que están en estado ACTIVA.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de reservaciones activas obtenida correctamente",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = Reservation.class))
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "Error al obtener las reservaciones")
            }
    )
    public List<Reservation> obtenerReservasActivas() {
        List<Reservation> todas = new ArrayList<>();

        Reservation r1 = new Reservation(1L, 101L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2);
        r1.setId(1L);
        r1.setEstado(EstadoReserva.ACTIVA);

        Reservation r2 = new Reservation(2L, 102L, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), 1);
        r2.setId(2L);
        r2.setEstado(EstadoReserva.CANCELADA);

        Reservation r3 = new Reservation(3L, 103L, LocalDate.now(), LocalDate.now().plusDays(2), 3);
        r3.setId(3L);
        r3.setEstado(EstadoReserva.ACTIVA);

        todas.add(r1);
        todas.add(r2);
        todas.add(r3);

        // Filtrar solo las activas
        List<Reservation> activas = new ArrayList<>();
        for (Reservation r : todas) {
            if (r.getEstado() == EstadoReserva.ACTIVA) {
                activas.add(r);
            }
        }

        return activas;
    }





}

