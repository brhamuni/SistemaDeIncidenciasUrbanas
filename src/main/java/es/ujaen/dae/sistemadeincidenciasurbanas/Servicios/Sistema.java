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

    public List<Usuario> usuarios() { return usuarios; }
    public List<Incidencia> incidencias() { return incidencias; }
    public List<TipoIncidencia> tiposDeIncidencia() { return tiposDeIncidencia;}
    public Usuario usuarioActual() { return usuarioActual; }

    public void usuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
    public void incidencias(List<Incidencia> incidencias) { this.incidencias = incidencias; }
    public void tiposDeIncidencia(List<TipoIncidencia> tiposDeIncidencia) { this.tiposDeIncidencia = tiposDeIncidencia; }
    public void usuarioActual(Usuario usuarioActual) { this.usuarioActual = usuarioActual; }
}