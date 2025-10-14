package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import java.time.LocalDate;

public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(String apellidos, String nombre, LocalDate fechaNacimiento, String direccion, String telefono, String email, String login, String claveAcceso) {
        super(apellidos, nombre, fechaNacimiento, direccion, telefono, email, login, claveAcceso);
    }
}