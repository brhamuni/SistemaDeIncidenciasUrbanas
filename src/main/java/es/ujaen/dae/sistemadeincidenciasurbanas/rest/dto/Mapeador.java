package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.seguridad.ServicioSeguridad;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioNoLogeado;
import es.ujaen.dae.sistemadeincidenciasurbanas.repositorios.RepositorioTipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.repositorios.RepositorioUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class Mapeador {
    @Autowired
    RepositorioTipoIncidencia repositorioTipoIncidencia;

    @Autowired
    RepositorioUsuario repositorioUsuario;

    @Autowired
    PasswordEncoder passwordEncoder;

    // ----------
    // Usuarios
    // ----------

    /**
     * Entidad a DTO
     * @param usuario
     * @return
     */
    public UsuarioDTO dto(Usuario usuario) {
        return new UsuarioDTO(
                usuario.login(),
                usuario.nombre(),
                usuario.apellidos(),
                usuario.email(),
                usuario.telefono(),
                usuario.direccion(),
                "" // Clave vacía por seguridad
        );
    }

    public Usuario entidad(UsuarioDTO usuarioDTO) {
        return new Usuario(
                usuarioDTO.nombre(),
                usuarioDTO.apellidos(),
                null,
                usuarioDTO.direccion(),
                usuarioDTO.telefono(),
                usuarioDTO.email(),
                usuarioDTO.login(),
                usuarioDTO.clave()
        );
    }

    public Usuario entidadNueva(UsuarioDTO usuarioDTO) {
        return new Usuario(
                usuarioDTO.nombre(),
                usuarioDTO.apellidos(),
                null,
                usuarioDTO.direccion(),
                usuarioDTO.telefono(),
                usuarioDTO.email(),
                usuarioDTO.login(),
                passwordEncoder.encode(usuarioDTO.clave())
        );
    }

    // ----------
    // Incidencias
    // ----------

    public IncidenciaDTO dto(Incidencia incidencia) {
        return new IncidenciaDTO(
                incidencia.id(),
                incidencia.descripcion(),
                incidencia.localizacion(),
                incidencia.localizacionGPS().x(),
                incidencia.localizacionGPS().y(),
                incidencia.estadoIncidencia(),
                incidencia.fecha(),
                incidencia.tipoIncidencia().nombre(),
                incidencia.usuario().login(),
                incidencia.usuario().nombre() + " " + incidencia.usuario().apellidos()
        );
    }

    public Incidencia entidad(IncidenciaDTO incidenciaDTO) {
        Usuario usuario = repositorioUsuario.buscarPorLogin(incidenciaDTO.usuario())
                .orElseThrow(UsuarioNoLogeado::new);

        TipoIncidencia tipo = repositorioTipoIncidencia.buscar(incidenciaDTO.tipo())
                .orElseThrow(() -> new IllegalArgumentException("Tipo desconocido"));

        return new Incidencia(
                incidenciaDTO.id(),
                incidenciaDTO.descripcion(),
                usuario,
                tipo,
                incidenciaDTO.localizacion(),
                new LocalizacionGPS(incidenciaDTO.latitud(), incidenciaDTO.longitud()),
                incidenciaDTO.fecha(),
                incidenciaDTO.estado()
        );
    }

    public Incidencia entidadNueva(IncidenciaDTO incidenciaDTO) {
        Usuario usuario = repositorioUsuario.buscarPorLogin(incidenciaDTO.usuario())
                .orElseThrow(UsuarioNoLogeado::new);

        TipoIncidencia tipo = repositorioTipoIncidencia.buscar(incidenciaDTO.tipo())
                .orElseThrow(() -> new IllegalArgumentException("Tipo desconocido"));

        return new Incidencia(
                incidenciaDTO.descripcion(),
                usuario,
                tipo,
                incidenciaDTO.localizacion(),
                new LocalizacionGPS(incidenciaDTO.latitud(), incidenciaDTO.longitud()),
                LocalDateTime.now(),
                EstadoIncidencia.PENDIENTE
        );
    }

    // ----------
    // Tipos de incidencia
    // ----------

    public TipoIncidenciaDTO dto(TipoIncidencia tipo) {
        return new TipoIncidenciaDTO(tipo.nombre(), tipo.descripcion());
    }

    public TipoIncidencia entidadNueva(TipoIncidenciaDTO tipoIncidenciaDTO) {
        return new TipoIncidencia(
                tipoIncidenciaDTO.nombre(),
                tipoIncidenciaDTO.descripcion()
        );
    }

    public TipoIncidencia entidad(TipoIncidenciaDTO tipoIncidenciaDTO) {
        return new TipoIncidencia(
                tipoIncidenciaDTO.nombre(),
                tipoIncidenciaDTO.descripcion()
        );
    }
}
