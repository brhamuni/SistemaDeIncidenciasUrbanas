package es.ujaen.dae.sistemadeincidenciasurbanas.excepciones;

public class TipoIncidenciaEnUso extends RuntimeException {
    public TipoIncidenciaEnUso(String mensaje) {
        super(mensaje);
    }
}
