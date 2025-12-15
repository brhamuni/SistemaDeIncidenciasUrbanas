package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;

import java.time.LocalDateTime;

public record DIncidencia(
        int id,
        LocalDateTime fecha,
        String descripcion,
        String localizacion,
        LocalizacionGPS localizacionGPS,
        EstadoIncidencia estadoIncidencia,
        String login,
        String nombreTipoIncidencia,
        byte[] foto) {
}