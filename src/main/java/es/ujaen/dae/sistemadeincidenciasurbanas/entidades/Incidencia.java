package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Incidencia {

    private LocalDateTime fecha;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    @NotBlank(message = "La localización no puede estar vacía")
    private String localizacion;

    private String localizacionGPS;

    @NotNull
    private EstadoIncidencia estadoIncidencia;

    @NotNull
    private Usuario usuario;

    @NotNull
    private TipoIncidencia tipoIncidencia;

    public Incidencia() {}

    public Incidencia(LocalDateTime fecha, String descripcion, String localizacion, String localizacionGPS, Usuario usuario, TipoIncidencia tipoIncidencia) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.localizacion = localizacion;
        this.localizacionGPS = localizacionGPS;
        this.usuario = usuario;
        this.tipoIncidencia = tipoIncidencia;
        this.estadoIncidencia = EstadoIncidencia.PENDIENTE;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getLocalizacionGPS() {
        return localizacionGPS;
    }

    public void setLocalizacionGPS(String localizacionGPS) {
        this.localizacionGPS = localizacionGPS;
    }

    public EstadoIncidencia getEstadoIncidencia() {
        return estadoIncidencia;
    }

    public void setEstadoIncidencia(EstadoIncidencia estadoIncidencia) {
        this.estadoIncidencia = estadoIncidencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoIncidencia getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(TipoIncidencia tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }
}