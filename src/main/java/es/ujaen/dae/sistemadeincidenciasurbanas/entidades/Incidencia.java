package es.ujaen.dae.sistemadeincidenciasurbanas.entidades;

import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotBlank(message = "La localización no puede estar vacía")
    @Column(nullable = false)
    private String localizacion;

    @Embedded
    private LocalizacionGPS localizacionGPS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoIncidencia estadoIncidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_incidencia_id")
    private TipoIncidencia tipoIncidencia;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto")
    private byte[] foto_incidencia;

    @Version
    private long version;

    public Incidencia() {}

    public Incidencia(int id, LocalDateTime fecha, String descripcion, String localizacion,
                      LocalizacionGPS localizacionGPS, Usuario usuario, TipoIncidencia tipoIncidencia, byte[] foto) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.localizacion = localizacion;
        this.localizacionGPS = localizacionGPS;
        this.usuario = usuario;
        this.tipoIncidencia = tipoIncidencia;
        this.estadoIncidencia = EstadoIncidencia.PENDIENTE;
        this.foto_incidencia = foto;
    }

    // Métodos get
    public int id() { return id; }
    public LocalDateTime fecha() { return fecha; }
    public String descripcion() { return descripcion; }
    public String localizacion() { return localizacion; }
    public LocalizacionGPS localizacionGPS() { return localizacionGPS; }
    public EstadoIncidencia estadoIncidencia() { return estadoIncidencia; }
    public Usuario usuario() { return usuario; }
    public TipoIncidencia tipoIncidencia() { return tipoIncidencia; }
    public byte[] foto() { return foto_incidencia; }     // ← FOTO

    // Métodos set
    public void id(int id) { this.id = id; }
    public void fecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void descripcion(String descripcion) { this.descripcion = descripcion; }
    public void localizacion(String localizacion) { this.localizacion = localizacion; }
    public void localizacionGPS(LocalizacionGPS localizacionGPS) { this.localizacionGPS = localizacionGPS; }
    public void estadoIncidencia(EstadoIncidencia estadoIncidencia) { this.estadoIncidencia = estadoIncidencia; }
    public void usuario(Usuario usuario) { this.usuario = usuario; }
    public void tipoIncidencia(TipoIncidencia tipoIncidencia) { this.tipoIncidencia = tipoIncidencia; }
    public void foto(byte[] foto) { this.foto_incidencia = foto; }   // ← SET FOTO

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Incidencia)) return false;
        Incidencia that = (Incidencia) o;
        return id() != 0 && id() == that.id();
    }

    @Override
    public int hashCode() {
        return id() == 0 ? super.hashCode() : java.util.Objects.hash(id());
    }
}