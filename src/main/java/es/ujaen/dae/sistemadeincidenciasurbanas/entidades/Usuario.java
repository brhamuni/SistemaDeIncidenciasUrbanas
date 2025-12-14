package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Usuario {

    @Id
    @NotBlank(message = "El login no puede estar vacío")
    @Column(nullable = false)
    private String login;

    @NotBlank(message = "La clave de acceso no puede estar vacía")
    @Column(nullable = false)
    private String claveAcceso;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Column(nullable = false)
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank
    private String direccion;

    @Pattern(regexp = "^(\\+34|0034|34)?[6789]\\d{8}$", message = "No es un número de teléfono válido")
    private String telefono;

    @Version
    private long version;

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

    //Metodos get
    public String apellidos() {
        return apellidos;
    }
    public String nombre() {
        return nombre;
    }
    public LocalDate fechaNacimiento() {
        return fechaNacimiento;
    }
    public String direccion() {
        return direccion;
    }
    public String telefono() {
        return telefono;
    }
    public String email() {
        return email;
    }
    public String login() {
        return login;
    }
    public String claveAcceso() {
        return claveAcceso;
    }

    //Metodos set
    public void apellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public void nombre(String nombre) {
        this.nombre = nombre;
    }
    public void fechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    public void direccion(String direccion) {
        this.direccion = direccion;
    }
    public void telefono(String telefono) {
        this.telefono = telefono;
    }
    public void email(String email) {
        this.email = email;
    }
    public void login(String login) {
        this.login = login;
    }
    public void claveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return java.util.Objects.equals(login(), usuario.login());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(login());
    }
}