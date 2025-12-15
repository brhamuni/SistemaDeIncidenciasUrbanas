package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioYaExiste;
import es.ujaen.dae.sistemadeincidenciasurbanas.repositorios.RepositorioIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DTipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.Mapeador;
import es.ujaen.dae.sistemadeincidenciasurbanas.seguridad.ServicioCredencialesUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incidencias")
public class  ControladorIncidencias {
    @Autowired
    Mapeador mapeador;

    @Autowired
    Sistema sistema;

    @Autowired
    ServicioCredencialesUsuario servicioCredencialesUsuario;
    @Autowired
    private RepositorioIncidencia repositorioIncidencia;

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public void mapeadoExcepcionConstraintViolationException() {}

    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoCliente(@RequestBody DUsuario usuario) {
        try {
            sistema.registrarUsuario(mapeador.entidadNueva(usuario));
        }
        catch(UsuarioYaExiste e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/usuarios/{login}")
    public ResponseEntity<DUsuario> obtenerCliente(@PathVariable String login) {
        try {
            Usuario usuario = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);
            return ResponseEntity.ok(mapeador.dto(usuario));
        }
        catch(UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/creadas")
    public ResponseEntity<DIncidencia> crearIncidencia(@RequestBody DIncidencia dIncidencia, Authentication authentication) {

        String login = authentication.getName();
        Usuario usuarioAutenticado = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

        Incidencia incidenciaCreada = mapeador.entidadNueva(dIncidencia);

        sistema.crearIncidencia(incidenciaCreada, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.dto(incidenciaCreada));

    }

    @DeleteMapping("/creadas/{id}")
    public ResponseEntity<Void> borrarIncidencia(@PathVariable("id") int id, Authentication authentication) {

        System.out.println("se mete");

        String login = authentication.getName();
        Usuario usuarioAutenticado = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

        sistema.borrarIncidencia(usuarioAutenticado, repositorioIncidencia.buscarPorId(id).orElseThrow(NoSuchElementException::new));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/creadas")
    public ResponseEntity<List<DIncidencia>> listarIncidenciasUsuario(Authentication authentication) {

            String login = authentication.getName();
            Usuario usuario = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);
            List<Incidencia> incidencias = sistema.listarIncidenciasDeUsuario(usuario);

            return ResponseEntity.status(HttpStatus.OK).body(incidencias.stream().map(i -> mapeador.dto(i)).toList());

    }

    @PostMapping("/tipos")
    public ResponseEntity<Void> crearTipo(@RequestBody DTipoIncidencia dTipo, Authentication authentication) {

        String login = authentication.getName();
        Usuario usuarioAutenticado = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

        TipoIncidencia nuevoTipo = mapeador.entidadNueva(dTipo);
        sistema.addTipoIncidencia(nuevoTipo,usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<DTipoIncidencia>> listarTipos() {
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        return ResponseEntity.ok(tipos.stream().map(mapeador::dto).toList());
    }

    @DeleteMapping("/tipos/{nombreTipoIncidencia}")
    public ResponseEntity<Void> borrarTipo(@PathVariable String nombreTipoIncidencia, Authentication authentication) {
        String login = authentication.getName();
        Usuario usuarioAutenticado = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

        sistema.borrarTipoIncidencia(nombreTipoIncidencia,usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}