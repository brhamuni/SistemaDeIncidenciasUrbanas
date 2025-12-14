package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
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
    RepositorioUsuario repositorioUsuario;

    @Autowired
    RepositorioTipoIncidencia repositorioTipoIncidencia;

    @Autowired
    PasswordEncoder codificadorClaves;

    public DUsuario dto(Usuario usuario) {
        return new DUsuario(
                usuario.login(),
                "",
                usuario.email(),
                usuario.nombre(),
                usuario.apellidos(),
                usuario.fechaNacimiento(),
                usuario.direccion(),
                usuario.telefono()
        );
    }

    public Usuario entidad(DUsuario dUsuario) {
        return new Usuario(
                dUsuario.apellidos(),
                dUsuario.nombre(),
                dUsuario.fechaNacimiento(),
                dUsuario.direccion(),
                dUsuario.telefono(),
                dUsuario.email(),
                dUsuario.login(),
                dUsuario.claveAcceso()
        );
    }

    public Usuario entidadNueva(DUsuario dUsuario) {
        return new Usuario(
                dUsuario.apellidos(),
                dUsuario.nombre(),
                dUsuario.fechaNacimiento(),
                dUsuario.direccion(),
                dUsuario.telefono(),
                dUsuario.email(),
                dUsuario.login(),
                codificadorClaves.encode(dUsuario.claveAcceso())
        );
    }

    public DIncidencia dto(Incidencia incidencia) {
        return new DIncidencia(
                incidencia.id(),
                incidencia.fecha(),
                incidencia.descripcion(),
                incidencia.localizacion(),
                incidencia.localizacionGPS(),
                incidencia.estadoIncidencia(),
                incidencia.usuario(),
                incidencia.tipoIncidencia(),
                incidencia.foto()
        );
    }

    public Incidencia entidad(DIncidencia dIncidencia) {
        Usuario usuario = repositorioUsuario.buscarPorLogin(dIncidencia.usuario().login())
                .orElseThrow(UsuarioNoLogeado::new);

        TipoIncidencia tipo = repositorioTipoIncidencia.buscar(dIncidencia.tipoIncidencia().nombre())
                .orElseThrow(() -> new IllegalArgumentException("Tipo desconocido"));

        return new Incidencia(
                dIncidencia.id(),
                dIncidencia.descripcion(),
                usuario,
                tipo,
                dIncidencia.localizacion(),
                dIncidencia.localizacionGPS(),
                dIncidencia.fecha(),
                dIncidencia.estadoIncidencia()
        );
    }

    public Incidencia entidadNueva(DIncidencia dIncidencia) {
        Usuario usuario = repositorioUsuario.buscarPorLogin(dIncidencia.usuario().login())
                .orElseThrow(UsuarioNoLogeado::new);

        TipoIncidencia tipo = repositorioTipoIncidencia.buscar(dIncidencia.tipoIncidencia().nombre())
                .orElseThrow(() -> new IllegalArgumentException("Tipo desconocido"));

        return new Incidencia(
                dIncidencia.descripcion(),
                usuario,
                tipo,
                dIncidencia.localizacion(),
                dIncidencia.localizacionGPS(),
                dIncidencia.fecha(),
                dIncidencia.estadoIncidencia()
        );
    }

    public DTipoIncidencia dto(TipoIncidencia tipoIncidencia) {
        return new DTipoIncidencia(
                tipoIncidencia.nombre(),
                tipoIncidencia.descripcion()
        );
    }

    public TipoIncidencia entidad(DTipoIncidencia dTipoIncidencia) {
        return new TipoIncidencia(
                dTipoIncidencia.nombre(),
                dTipoIncidencia.descripcion()
        );
    }

    public TipoIncidencia entidadNueva(DTipoIncidencia dTipoIncidencia) {
        return new TipoIncidencia(
                dTipoIncidencia.nombre(),
                dTipoIncidencia.descripcion()
        );
    }

}
