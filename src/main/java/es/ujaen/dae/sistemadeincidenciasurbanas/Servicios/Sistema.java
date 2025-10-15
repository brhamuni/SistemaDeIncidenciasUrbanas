package es.ujaen.dae.sistemadeincidenciasurbanas.Servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Sistema {

    private List<Usuario> usuarios;
    private List<Incidencia> incidencias;
    private List<TipoIncidencia> tiposDeIncidencia;
    private Usuario usuarioActual;
    private static int nIncidencia = 1;

    public Sistema() {
        this.usuarios = new ArrayList<>();
        this.incidencias = new ArrayList<>();
        this.tiposDeIncidencia = new ArrayList<>();
        this.usuarioActual = null;
    }

    // Getters y Setters
    public List<Usuario> usuarios() {
        return usuarios;
    }

    public List<Incidencia> incidencias() {
        return incidencias;
    }

    public List<TipoIncidencia> tiposDeIncidencia() {
        return tiposDeIncidencia;
    }

    public Usuario usuarioActual() {
        return usuarioActual;
    }

    public void usuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public void incidencias(List<Incidencia> incidencias) {
        this.incidencias = incidencias;
    }

    public void tiposDeIncidencia(List<TipoIncidencia> tiposDeIncidencia) {
        this.tiposDeIncidencia = tiposDeIncidencia;
    }

    public void usuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void registrarUsuario(Usuario nuevoUsuario) {
        Optional<Usuario> existente = usuarios.stream().filter(u -> u.login().equalsIgnoreCase(nuevoUsuario.login())).findFirst();
        if (existente.isPresent()) {
            throw new UsuarioYaExiste();
        }
        usuarios.add(nuevoUsuario);
    }

    public boolean iniciarSesion(String login, String clave) {
        for (Usuario u : usuarios) {
            if (u.login().equals(login) && u.claveAcceso().equals(clave)) {
                usuarioActual = u;
                return true;
            }
        }
        return false;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public void actualizarDatosUsuario(Usuario usuario, String nombre, String apellidos, String email, String telefono, String direccion) {
        usuario.nombre(nombre);
        usuario.apellidos(apellidos);
        usuario.email(email);
        usuario.telefono(telefono);
        usuario.direccion(direccion);
    }

    public void crearIncidencia(LocalDateTime fecha, String tipo, String descripcion, String localizacion, String localizacionGPS, Usuario usuario, TipoIncidencia tipoIncidencia) {
        Incidencia nuevaIncidencia = new Incidencia(nIncidencia++, fecha, descripcion, localizacion, localizacionGPS, usuario, tipoIncidencia);

        if (incidencias.contains(nuevaIncidencia)) {
            throw new IncidenciaYaExiste();
        }

        incidencias.add(nuevaIncidencia);
    }
}