package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.AccionNoAutorizada;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.IncidenciaNoExiste;
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

    /**
     * Ejercicio Voluntario 1
     * Endpoint para subir una foto a una incidencia existente
     */
    @PostMapping(value = "/creadas/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> subirFoto(@PathVariable int id, @RequestPart("foto") MultipartFile foto, Authentication authentication) {

            //Validación de que la foto es correcta.
            if (foto == null || foto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            //Validar que es una imagen correcta.
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            try {
                //Obtener usuario autenticado
                String login = authentication.getName();
                Usuario usuario = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

                //Guardar la foto asociada a la incidencia
                sistema.agregarFotoAIncidencia(id, foto.getBytes(), usuario);
                return ResponseEntity.status(HttpStatus.CREATED).build();

            } catch (UsuarioNoEncontrado e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } catch (IncidenciaNoExiste e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } catch (AccionNoAutorizada e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

    /**
     * Ejercicio Voluntario 1
     * Endpoint para descargar la foto de una incidencia
     * Nota: Solo descarga fotos con la extensión PNG
     */
    @GetMapping(value = "/creadas/{id}/foto", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> descargarFoto(@PathVariable int id, Authentication authentication) {
        
        try {
            //Obtener usuario autenticado
            String login = authentication.getName();
            Usuario usuario = sistema.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

            byte[] foto = sistema.obtenerFotoDeIncidencia(id,usuario);
            if (foto == null || foto.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_PNG).body(foto);

        } catch (UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IncidenciaNoExiste e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccionNoAutorizada e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}