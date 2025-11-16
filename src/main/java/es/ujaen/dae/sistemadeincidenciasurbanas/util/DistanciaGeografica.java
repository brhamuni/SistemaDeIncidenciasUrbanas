package es.ujaen.dae.sistemadeincidenciasurbanas.util;

public class DistanciaGeografica {
    private static final double EARTH_RADIUS = 6371000;

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine.
     *
     * @param lat1 Latitud del primer punto en grados
     * @param lon1 Longitud del primer punto en grados
     * @param lat2 Latitud del segundo punto en grados
     * @param lon2 Longitud del segundo punto en grados
     * @return Distancia en metros
     */
    public static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}