package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import jakarta.validation.constraints.*;
import java.time.LocalDate;


public class Usuario {

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    private String apellidos;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    private String direccion;

    @Pattern(regexp = "^(\\+34|0034|34)?[6789]\\d{8}$", message = "No es un número de teléfono válido")
    private String telefono;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El login no puede estar vacío")
    private String login;

    @NotBlank(message = "La clave de acceso no puede estar vacía")
    private String claveAcceso;


    public Usuario() {
    }

    public Usuario(String apellidos, String nombre, LocalDate fechaNacimiento, String direccion, String telefono, String email, String login, String claveAcceso) {
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.login = login;
        this.claveAcceso = claveAcceso;
    }

    public String apellidos() {
        return apellidos;
    }

    public void apellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String nombre() {
        return nombre;
    }

    public void nombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate fechaNacimiento() {
        return fechaNacimiento;
    }

    public void fechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String direccion() {
        return direccion;
    }

    public void direccion(String direccion) {
        this.direccion = direccion;
    }

    public String telefono() {
        return telefono;
    }

    public void telefono(String telefono) {
        this.telefono = telefono;
    }

    public String email() {
        return email;
    }

    public void email(String email) {
        this.email = email;
    }

    public String login() {
        return login;
    }

    public void login(String login) {
        this.login = login;
    }

    public String claveAcceso() {
        return claveAcceso;
    }

    public void claveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return login.equals(usuario.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

}