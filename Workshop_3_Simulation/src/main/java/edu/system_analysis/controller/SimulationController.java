package edu.system_analysis.controller;

import java.util.ArrayList;
import java.util.List;

import edu.system_analysis.simulation.MovementProbabilityModel;

public class SimulationController {
    private final MovementProbabilityModel model = new MovementProbabilityModel();
    private final List<Double> probHistory = new ArrayList<>();
    private final List<Boolean> stateHistory = new ArrayList<>();
    private final List<Double> dormidoHistory = new ArrayList<>();

    // Valores base realistas
    // Luz: 0-1000 lux (noche: <10, día: 100-1000)
    // Sonido: 20-80 dB (noche: 20-40, día: 40-80)
    // Estrés: 0-10 (escala subjetiva)
    // ENMO: 0-0.2 g (típico en actigrafía, 0.01-0.05 g en sueño)
    // Anglez: 0-180 grados (posición de la muñeca)
    private double luzBase = 10; // lux
    private double sonidoBase = 30; // dB
    private double estresBase = 2; // escala 0-10
    private double enmoBase = 0.015; // g
    private double anglezBase = 90; // grados

    // Últimos valores simulados
    private double luzActual = 10;
    private double sonidoActual = 30;
    private double estresActual = 2;
    private double enmoActual = 0.015;
    private double anglezActual = 90;

    // Historial de probabilidad de estar dormido
    private final List<Double> probDormidoHistory = new ArrayList<>();

    private boolean estabaDormido = true;
    private double lastUmbral = 0.35; // El último umbral calculado

    private double actividadManual = -1; // Si <0, se simula automáticamente
    private int pasoActual = 0;

    public void reset() {
        probHistory.clear();
        stateHistory.clear();
        dormidoHistory.clear();
        probDormidoHistory.clear();
        estabaDormido = true;
        pasoActual = 0;
        actividadManual = -1;
    }

    // El umbral ahora es dinámico y depende de los estímulos
    private double calcularUmbral(double luz, double sonido, double estres, double enmo, double anglez) {
        double base = 0.35;
        double umbral = base + 0.10 * estres + 0.08 * sonido + 0.05 * luz - 0.20 * enmo - 0.10 * anglez;
        return Math.max(0.10, Math.min(umbral, 0.7));
    }

    public double getLastUmbral() {
        return lastUmbral;
    }

    public void setBases(double luz, double sonido, double estres, double enmo, double anglez) {
        this.luzBase = luz;
        this.sonidoBase = sonido;
        this.estresBase = estres;
        this.enmoBase = enmo;
        this.anglezBase = anglez;
    }

    private double nextGaussian(double base, double std, double min, double max) {
        double val = base + std * java.util.concurrent.ThreadLocalRandom.current().nextGaussian();
        return Math.max(min, Math.min(max, val));
    }

    public double getLuzActual() { return luzActual; }
    public double getSonidoActual() { return sonidoActual; }
    public double getEstresActual() { return estresActual; }
    public double getEnmoActual() { return enmoActual; }
    public double getAnglezActual() { return anglezActual; }

    // Normalización para el modelo (0-1)
    private double normLuz(double lux) { return Math.min(1.0, lux / 500.0); }
    private double normSonido(double db) { return Math.min(1.0, db / 60.0); }
    private double normEstres(double e) { return Math.min(1.0, e / 10.0); }
    private double normEnmo(double enmo) { return Math.min(1.0, enmo / 0.1); }
    private double normAnglez(double ang) { return Math.abs(Math.cos(Math.toRadians(ang))); } // 0=vertical, 1=horizontal

    public void setActividadManual(double actividad) {
        this.actividadManual = actividad;
    }
    public double getActividadActual() {
        if (actividadManual >= 0) return actividadManual;
        // Si no está fijada manualmente, usar ENMO normalizado
        return normEnmo(enmoActual);
    }

    public double simularPaso() {
        pasoActual++;
        // Cambios abruptos cada 12 pasos
        if (pasoActual % 12 == 0) {
            luzBase = nextGaussian(luzBase + (Math.random() > 0.5 ? 100 : -100), 50, 0, 1000);
            sonidoBase = nextGaussian(sonidoBase + (Math.random() > 0.5 ? 10 : -10), 10, 20, 80);
        }
        luzActual = nextGaussian(luzBase, 10, 0, 1000);
        sonidoActual = nextGaussian(sonidoBase, 5, 20, 80);
        estresActual = nextGaussian(estresBase, 1, 0, 10);

        double luzN = normLuz(luzActual);
        double sonidoN = normSonido(sonidoActual);
        double estresN = normEstres(estresActual);

        boolean estabaDormidoBool = getUltimaProbDormido() > 0.5;
        double probMov = model.calcularProbabilidadMovimiento(luzN, sonidoN, estresN, estabaDormidoBool);
        probHistory.add(probMov);

        boolean movioMano = Math.random() < probMov;
        stateHistory.add(movioMano);

        // ENMO y Anglez fisiológicamente realistas
        if (movioMano) {
            // Movimiento voluntario: ENMO alto, Anglez cambia más
            enmoActual = nextGaussian(0.13, 0.04, 0.05, 0.25); // valores más realistas
            double deltaAng = nextGaussian(25, 10, 10, 60);
            anglezActual = Math.max(0, Math.min(180, anglezActual + (Math.random() > 0.5 ? deltaAng : -deltaAng)));
        } else {
            // Micro-movimiento: ENMO bajo, Anglez varía poco
            enmoActual = nextGaussian(0.025, 0.01, 0.01, 0.05); // valores más realistas
            double deltaAng = nextGaussian(2, 1, 0, 5);
            anglezActual = Math.max(0, Math.min(180, anglezActual + (Math.random() > 0.5 ? deltaAng : -deltaAng)));
        }

        // Usar actividad manual si está fijada, si no usar ENMO normalizado
        double actividadActual = getActividadActual();
        double probDormido = model.calcularProbabilidadDormido(actividadActual, luzN, sonidoN, estresN);
        probDormidoHistory.add(probDormido);
        lastUmbral = calcularUmbral(luzN, sonidoN, estresN, 0, 0);
        return probMov;
    }

    public double getUltimaProbDormido() {
        if (probDormidoHistory.isEmpty()) return 1.0;
        return probDormidoHistory.get(probDormidoHistory.size() - 1);
    }

    public List<Double> getProbDormidoHistory() {
        return probDormidoHistory;
    }

    public List<Double> getProbHistory() {
        return probHistory;
    }

    public List<Boolean> getStateHistory() {
        return stateHistory;
    }

    public List<Double> getDormidoHistory() {
        return dormidoHistory;
    }
}
