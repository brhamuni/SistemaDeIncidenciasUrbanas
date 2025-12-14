package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;
import java.time.LocalDate;

public record DUsuario(
        String login,
        String claveAcceso,
        String email,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        String telefono){
}
