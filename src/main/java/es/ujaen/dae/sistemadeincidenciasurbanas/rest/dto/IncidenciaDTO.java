package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;

import java.time.LocalDateTime;

public record IncidenciaDTO(
        int id,
        String descripcion,
        String localizacion,
        double latitud,
        double longitud,
        EstadoIncidencia estado,
        LocalDateTime fecha,
        String tipo,
        String nombreUsuario
) {}