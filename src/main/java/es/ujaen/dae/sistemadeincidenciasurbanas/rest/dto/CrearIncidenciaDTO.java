package es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto;

public record CrearIncidenciaDTO (
        String descripcion,
        String localizacion,
        double latitud,
        double longitud,
        String tipo
){
}
