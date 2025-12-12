package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

import java.time.LocalDate;

public record RegistroDTO (
        String nombre,
        String apellidos,
        String email,
        String clave,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento
){
}
