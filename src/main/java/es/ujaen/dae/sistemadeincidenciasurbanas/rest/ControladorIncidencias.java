package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.TipoIncidenciaEnUso;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.TipoIncidenciaNoExiste;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioNoAdmin;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.UsuarioYaExiste;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DTipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.Mapeador;
import es.ujaen.dae.sistemadeincidenciasurbanas.seguridad.ServicioCredencialesUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import jakarta.validation.ConstraintViolationException;

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

    @PutMapping("/usuarios/{login}")
    public ResponseEntity<Void> actualizarUsuario(@PathVariable String login, @RequestBody DUsuario usuarioActualizado, Principal usuarioAutenticado) {
    
        if (!login.equals(usuarioAutenticado.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        try {
            Usuario usuario = mapeador.entidad(usuarioActualizado);
            sistema.actualizarDatosUsuario(usuario);
            return ResponseEntity.ok().build();
        } catch (UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<DIncidencia>> buscarIncidencias(@RequestParam(required = false) String tipo, @RequestParam(required = false) String estado, Principal usuarioAutenticado) {
    
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        TipoIncidencia tipoIncidencia = null;
        EstadoIncidencia estadoIncidencia = null;
    
        if (tipo != null) {
            tipoIncidencia = sistema.listarTiposDeIncidencia().stream()
                .filter(t -> t.nombre().equals(tipo))
                .findFirst()
                .orElse(null);
        }
    
        if (estado != null) {
            try {
                estadoIncidencia = EstadoIncidencia.valueOf(estado);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        List<Incidencia> incidencias = sistema.buscarIncidencias(tipoIncidencia, estadoIncidencia);
        return ResponseEntity.ok(incidencias.stream().map(mapeador::dto).toList());
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<DTipoIncidencia>> listarTipos() {
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        return ResponseEntity.ok(tipos.stream().map(mapeador::dto).toList());
    }

    @DeleteMapping("/tipos/{nombre}")
    public ResponseEntity<Void> borrarTipo(@PathVariable String nombre, @AuthenticationPrincipal Usuario usuarioAutenticado) {
    
        try {
            TipoIncidencia tipo = sistema.listarTiposDeIncidencia().stream()
                .filter(t -> t.nombre().equals(nombre))
                .findFirst()
                .orElseThrow(() -> new TipoIncidenciaNoExiste("Tipo no encontrado"));
        
            sistema.borrarTipoIncidencia(tipo, usuarioAutenticado);
            return ResponseEntity.noContent().build();
        
        } catch (TipoIncidenciaNoExiste e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TipoIncidenciaEnUso e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (UsuarioNoAdmin e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}