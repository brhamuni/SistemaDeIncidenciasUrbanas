package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class Incidencia {

    private int id;

    private LocalDateTime fecha;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    @NotBlank(message = "La localización no puede estar vacía")
    private String localizacion;

    private LocalizacionGPS localizacionGPS;

     private EstadoIncidencia estadoIncidencia;

    private Usuario usuario;

    @NotNull
    private TipoIncidencia tipoIncidencia;

    public Incidencia() {}

    public Incidencia(int id, LocalDateTime fecha, String descripcion, String localizacion, LocalizacionGPS localizacionGPS, Usuario usuario, TipoIncidencia tipoIncidencia) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.localizacion = localizacion;
        this.localizacionGPS = localizacionGPS;
        this.usuario = usuario;
        this.tipoIncidencia = tipoIncidencia;
        this.estadoIncidencia = EstadoIncidencia.PENDIENTE;
    }

    public LocalDateTime fecha() {
        return fecha;
    }

    public void fecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String descripcion() {
        return descripcion;
    }

    public void descripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String localizacion() {
        return localizacion;
    }

    public void localizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public LocalizacionGPS localizacionGPS() {
        return localizacionGPS;
    }

    public void localizacionGPS(LocalizacionGPS localizacionGPS) {
        this.localizacionGPS = localizacionGPS;
    }

    public EstadoIncidencia estadoIncidencia() {
        return estadoIncidencia;
    }

    public void estadoIncidencia(EstadoIncidencia estadoIncidencia) {
        this.estadoIncidencia = estadoIncidencia;
    }

    public Usuario usuario() {
        return usuario;
    }

    public void usuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoIncidencia tipoIncidencia() {
        return tipoIncidencia;
    }

    public void tipoIncidencia(TipoIncidencia tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public int id() {return id;}

    public void id(int id) {this.id = id;}
}