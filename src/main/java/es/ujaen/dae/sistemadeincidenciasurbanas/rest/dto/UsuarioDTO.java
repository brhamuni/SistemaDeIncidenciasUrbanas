package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

public record UsuarioDTO (
        String nombre,
        String apellidos,
        String email,
        String telefono,
        String direccion
){
}
