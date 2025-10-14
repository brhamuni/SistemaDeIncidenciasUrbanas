package es.ujaen.dae.sistemadeincidenciasurbanas.Servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;

import java.util.List;

public class Sistema {

    private List<Usuario> usuarios;
    private List<Incidencia> incidencias;
    private List<TipoIncidencia> tiposDeIncidencia;
    private Usuario usuarioActual;

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Incidencia> getIncidencias() { return incidencias; }
    public List<TipoIncidencia> getTiposDeIncidencia() { return tiposDeIncidencia;}
    public Usuario getUsuarioActual() { return usuarioActual; }

    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
    public void setIncidencias(List<Incidencia> incidencias) { this.incidencias = incidencias; }
    public void setTiposDeIncidencia(List<TipoIncidencia> tiposDeIncidencia) { this.tiposDeIncidencia = tiposDeIncidencia; }
    public void setUsuarioActual(Usuario usuarioActual) { this.usuarioActual = usuarioActual; }
}