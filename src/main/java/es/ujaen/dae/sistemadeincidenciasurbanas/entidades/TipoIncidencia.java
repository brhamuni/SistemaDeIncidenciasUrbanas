package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import jakarta.validation.constraints.NotBlank;

public class TipoIncidencia {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    public TipoIncidencia() {
    }

    public TipoIncidencia(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String nombre() {
        return nombre;
    }

    public void nombre(String nombre) {
        this.nombre = nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public void descripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}