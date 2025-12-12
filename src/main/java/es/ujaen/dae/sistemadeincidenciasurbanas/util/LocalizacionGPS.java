package es.ujaen.dae.sistemadeincidenciasurbanas.util;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class LocalizacionGPS {
    @Column(name = "gps_x")
    private double x;

    @Column(name = "gps_y")
    private double y;

    public LocalizacionGPS() {}

    public LocalizacionGPS(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public void x(int x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void y(int y) {
        this.y = y;
    }
}
