package es.ujaen.dae.sistemadeincidenciasurbanas.excepciones;

public class TipoIncidenciaNoExiste extends RuntimeException {
    public TipoIncidenciaNoExiste(String mensaje) {
        super(mensaje);
    }
}
