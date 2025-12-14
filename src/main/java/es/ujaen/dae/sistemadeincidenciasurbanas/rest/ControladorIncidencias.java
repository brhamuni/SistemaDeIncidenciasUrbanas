package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioYaExiste;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DTipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.Mapeador;
import es.ujaen.dae.sistemadeincidenciasurbanas.seguridad.ServicioCredencialesUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import jakarta.validation.ConstraintViolationException;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incidencias")
public class ControladorIncidencias {
    @Autowired
    Mapeador mapeador;

    @Autowired
    Sistema sistema;

    @Autowired
    ServicioCredencialesUsuario servicioCredencialesUsuario;

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


    @PostMapping
    public ResponseEntity<DIncidencia> crearIncidencia(@RequestBody DIncidencia incidencia, Principal usuarioAutenticado) {
        try {
            Usuario usuario = sistema.buscarUsuario(usuarioAutenticado.getName()).orElseThrow(UsuarioNoEncontrado::new);
            Incidencia incidenciaCreada = mapeador.entidadNueva(incidencia);

            sistema.crearIncidencia(incidenciaCreada, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.dto(incidenciaCreada));
        }
        catch(UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DIncidencia>> listarIncidenciasUsuario(Principal usuarioAutenticado) {
        try {
            Usuario usuario = sistema.buscarUsuario(usuarioAutenticado.getName()).orElseThrow(UsuarioNoEncontrado::new);
            List<Incidencia> incidencias = sistema.listarIncidenciasDeUsuario(usuario);

            return ResponseEntity.ok(incidencias.stream().map(i -> mapeador.dto(i)).toList());
        }
        catch(UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/tipos")
    public ResponseEntity<Void> crearTipo(@RequestBody DTipoIncidencia dTipo,@AuthenticationPrincipal Usuario usuarioAutenticado) {
        System.out.println(">>> ENDPOINT crearTipo LLAMADO");
        System.out.println(">>> Usuario autenticado: " + usuarioAutenticado);

        TipoIncidencia nuevoTipo = mapeador.entidadNueva(dTipo);
        sistema.addTipoIncidencia(nuevoTipo,usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}