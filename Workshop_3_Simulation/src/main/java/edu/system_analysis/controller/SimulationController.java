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
    // Anglez: -90 a 90 grados (posición de la muñeca)
    private double luzBase = 5; // lux - empezar en noche profunda
    private double sonidoBase = 25; // dB - muy silencioso al inicio
    private double estresBase = 1; // escala 0-10 - muy bajo estrés nocturno

    // Últimos valores simulados
    private double luzActual = 5;
    private double sonidoActual = 25;
    private double estresActual = 1;
    private double enmoActual = 0.008; // Empezar con muy poco movimiento (sueño)
    private double anglezActual = 15; // Posición típica de sueño

    // Historial de probabilidad de estar dormido
    private final List<Double> probDormidoHistory = new ArrayList<>();

    // private boolean estabaDormido = true; // (no usado)
    private double lastUmbral = 0.35; // El último umbral calculado

    private double actividadManual = -1; // Si <0, se simula automáticamente
    private int pasoActual = 0;

    public void reset() {
        probHistory.clear();
        stateHistory.clear();
        dormidoHistory.clear();
        probDormidoHistory.clear();
        // Reiniciar en estado nocturno realista
        pasoActual = 0;
        actividadManual = -1;
        // Valores iniciales nocturnos
        luzBase = 5;
        sonidoBase = 25;
        estresBase = 1;
        luzActual = 5;
        sonidoActual = 25;
        estresActual = 1;
        enmoActual = 0.008;
        anglezActual = 15;
    }

    /**
     * Calcula el umbral de probabilidad de movimiento para clasificar sueño/vigilia.
     * El umbral es bajo en condiciones de baja luz, bajo sonido y bajo estrés (ambiente de sueño),
     * y aumenta progresivamente con mayor luz, sonido y estrés (ambiente de vigilia).
     * El rango típico es 0.01 (muy fácil dormir) a 0.20 (muy difícil dormir).
     *
     * @param luz    Luz ambiental (lux, normalizado 0-1)
     * @param sonido Sonido ambiental (dB, normalizado 0-1)
     * @param estres Estrés percibido (escala 0-10, normalizado 0-1)
     * @return Umbral fisiológicamente plausible para transición sueño/vigilia
     */
    private double calcularUmbralMovimiento(double luz, double sonido, double estres) {
        // Umbral base bajo, sube con estrés, luz y sonido
        double base = 0.04;
        // Luz y sonido tienen mayor peso en la transición
        double umbral = base + 0.20 * sonido + 0.18 * luz + 0.12 * estres;
        // Limitar a rango fisiológico
        return Math.max(0.02, Math.min(umbral, 0.25));
    }

    public double getLastUmbral() {
        return lastUmbral;
    }

    public void setBases(double luz, double sonido, double estres, double enmo, double anglez) {
        this.luzBase = luz;
        this.sonidoBase = sonido;
        this.estresBase = estres;
        // ENMO y Anglez base ya no se usan directamente
    }
    //TODO: REVisa 
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
    // private double normAnglez(double ang) { return Math.abs(Math.cos(Math.toRadians(ang))); } // (no usado)

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
        
        // Ciclo circadiano simulado: cada 96 pasos (8 horas) la luz tiende hacia el extremo opuesto
        // Para simular: noche (0-200 lux) -> amanecer -> día (400-1000 lux) -> atardecer -> noche
        double cicloCircadiano = pasoActual % (96 * 3); // Ciclo completo de 24 horas (3 períodos de 8h)
        double targetLuz, targetSonido;
        
        if (cicloCircadiano < 96) {
            // Período 1: Noche/Madrugada (0-8h) - Luz baja estable, sonido muy bajo, estrés mínimo
            double progresoNoche = cicloCircadiano / 96.0;
            // Transición suave al inicio y final del período, estable en el medio
            if (progresoNoche < 0.3) {
                // Primeras 2.4h: transición suave desde el período anterior
                targetLuz = 3 + 7 * (1 - progresoNoche / 0.3); // 10 -> 3 lux (noche profunda)
                targetSonido = 22 + 3 * (1 - progresoNoche / 0.3); // 25 -> 22 dB (muy silencioso)
                // Estrés nocturno muy bajo, con posibles picos ocasionales por sueños/pesadillas
                double nightStressChange = nextGaussian(-0.05, 0.08, -0.3, 0.1);
                estresBase = Math.max(0.2, estresBase + nightStressChange);
                
                // Occasional sleep disturbances (nightmares, noise, etc.)
                if (Math.random() < 0.03) { // 3% chance of brief disturbance
                    estresBase = Math.min(4.0, estresBase + nextGaussian(0.8, 0.3, 0.3, 1.5));
                }
            } else if (progresoNoche > 0.7) {
                // Últimas 2.4h: preparación para el amanecer
                double factorAmanecer = (progresoNoche - 0.7) / 0.3; // 0 -> 1
                targetLuz = 3 + 20 * factorAmanecer; // 3 -> 23 lux (pre-amanecer)
                targetSonido = 22 + 8 * factorAmanecer; // 22 -> 30 dB
                // Estrés puede empezar a subir ligeramente al acercarse el despertar
                double morningStressChange = nextGaussian(0.2, 0.15, -0.1, 0.5);
                estresBase = Math.min(3.0, estresBase + morningStressChange);
                
                // Some people are naturally more anxious about waking up
                if (Math.random() < 0.08) { // 8% chance of morning anxiety
                    estresBase = Math.min(4.5, estresBase + nextGaussian(0.6, 0.2, 0.2, 1.2));
                }
            } else {
                // Medio del período: estable y muy bajo (sueño profundo)
                targetLuz = 3; // Muy bajo, noche profunda
                targetSonido = 22; // Extremadamente silencioso
                // Mantener estrés muy bajo durante sueño profundo con micro-variaciones
                double deepSleepVariation = nextGaussian(0, 0.05, -0.15, 0.15);
                estresBase = Math.max(0.1, estresBase + deepSleepVariation);
            }
        } else if (cicloCircadiano < 192) {
            // Período 2: Día (8-16h) - Luz alta estable, sonido moderado-alto, estrés variable
            double progresoDia = (cicloCircadiano - 96) / 96.0;
            if (progresoDia < 0.2) {
                // Primeras 1.6h: amanecer
                targetLuz = 23 + 577 * progresoDia / 0.2; // 23 -> 600 lux (amanecer rápido)
                targetSonido = 30 + 25 * progresoDia / 0.2; // 30 -> 55 dB
                // Estrés aumenta gradualmente al despertar
                estresBase = Math.min(4.0, estresBase + 2.0 * progresoDia / 0.2);
            } else if (progresoDia > 0.8) {
                // Últimas 1.6h: preparación para el atardecer
                double factorAtardecer = (progresoDia - 0.8) / 0.2; // 0 -> 1
                targetLuz = 700 - 100 * factorAtardecer; // 700 -> 600 lux
                targetSonido = 55 - 5 * factorAtardecer; // 55 -> 50 dB
                // Estrés puede bajar ligeramente al final del día
                estresBase = Math.max(2.0, estresBase - 0.5 * factorAtardecer);
            } else {
                // Medio del día: estable y alto (actividad plena)
                targetLuz = 700; // Alto, típico de día
                targetSonido = 55; // Activo pero no excesivo
                // Estrés diurno variable (trabajo, actividades, eventos)
                double stressVariation = nextGaussian(0, 0.4, -0.8, 0.8);
                estresBase = Math.min(6.0, Math.max(1.5, estresBase + stressVariation));
                
                // Occasional stress spikes (meetings, deadlines, etc.)
                if (Math.random() < 0.08) { // 8% chance of stress spike
                    estresBase = Math.min(8.0, estresBase + nextGaussian(1.5, 0.5, 0.5, 3.0));
                }
            }
        } else {
            // Período 3: Tarde/Noche (16-24h) - Luz decreciente, sonido decreciente, estrés bajando
            double progresoTarde = (cicloCircadiano - 192) / 96.0;
            if (progresoTarde < 0.4) {
                // Primeras 3.2h: atardecer gradual
                targetLuz = 600 - 400 * progresoTarde / 0.4; // 600 -> 200 lux
                targetSonido = 50 - 15 * progresoTarde / 0.4; // 50 -> 35 dB
                // Estrés comienza a bajar (relajación vespertina) con variabilidad
                double eveningStressChange = nextGaussian(-0.3, 0.2, -0.8, 0.2);
                estresBase = Math.max(0.8, estresBase + eveningStressChange);
            } else if (progresoTarde > 0.7) {
                // Últimas 2.4h: preparación para la noche
                double factorNoche = (progresoTarde - 0.7) / 0.3; // 0 -> 1
                targetLuz = 100 - 70 * factorNoche; // 100 -> 30 lux (pre-noche)
                targetSonido = 35 - 10 * factorNoche; // 35 -> 25 dB
                // Estrés baja significativamente preparándose para dormir
                double nightStressChange = nextGaussian(-0.8, 0.3, -1.5, -0.2);
                estresBase = Math.max(0.3, estresBase + nightStressChange);
                
                // Some evenings are more stressful (insomnia triggers)
                if (Math.random() < 0.12) { // 12% chance of evening stress
                    estresBase = Math.min(5.0, estresBase + nextGaussian(1.2, 0.4, 0.5, 2.5));
                }
            } else {
                // Medio de la tarde: estable y moderado
                targetLuz = 200; // Moderado, típico de tarde
                targetSonido = 40; // Tranquilo
                // Estrés estable y bajando lentamente con variabilidad natural
                double afternoonVariation = nextGaussian(-0.1, 0.15, -0.4, 0.3);
                estresBase = Math.max(0.8, estresBase + afternoonVariation);
            }
        }
        
        // Cambios muy graduales y controlados hacia el target circadiano
        if (pasoActual % 6 == 0) { // Cada 6 pasos para transiciones más suaves
            // Movimiento EXTREMADAMENTE gradual hacia el target (solo 3% por cambio)
            double luzDelta = (targetLuz - luzBase) * 0.03;
            double sonidoDelta = (targetSonido - sonidoBase) * 0.03;
            
            // Ruido muy reducido y proporcional al nivel actual
            double luzNoise = Math.max(2, luzBase * 0.02); // 2% del nivel actual, mínimo 2 lux
            double sonidoNoise = Math.max(0.5, sonidoBase * 0.02); // 2% del nivel actual, mínimo 0.5 dB
            
            luzDelta += nextGaussian(0, luzNoise, -luzNoise * 2, luzNoise * 2);
            sonidoDelta += nextGaussian(0, sonidoNoise, -sonidoNoise * 2, sonidoNoise * 2);
            
            luzBase = Math.max(0, Math.min(1000, luzBase + luzDelta));
            sonidoBase = Math.max(20, Math.min(80, sonidoBase + sonidoDelta));
        }
        
        // Variabilidad mínima en cada paso (aún más reducida)
        double luzStd = Math.max(1, luzBase * 0.01); // 1% del nivel actual, mínimo 1 lux
        double sonidoStd = Math.max(0.3, sonidoBase * 0.01); // 1% del nivel actual, mínimo 0.3 dB
        
        luzActual = nextGaussian(luzBase, luzStd, Math.max(0, luzBase - luzStd * 3), Math.min(1000, luzBase + luzStd * 3));
        sonidoActual = nextGaussian(sonidoBase, sonidoStd, Math.max(20, sonidoBase - sonidoStd * 3), Math.min(80, sonidoBase + sonidoStd * 3));
        estresActual = nextGaussian(estresBase, 0.2, 0, 10); // Reducir variabilidad del estrés

        double luzN = normLuz(luzActual);
        double sonidoN = normSonido(sonidoActual);
        double estresN = normEstres(estresActual);

        boolean estabaDormidoBool = getUltimaProbDormido() > 0.5;
        double probMov = model.calcularProbabilidadMovimiento(luzN, sonidoN, estresN, estabaDormidoBool);

        // Probabilidad de movimiento con mayor variabilidad y ruido gaussiano
        double noiseMov = nextGaussian(0, 0.08, -0.20, 0.20);
        probMov = Math.max(0.0, Math.min(1.0, probMov + noiseMov));
        probHistory.add(probMov);

        boolean movioMano = Math.random() < probMov;
        stateHistory.add(movioMano);

        // ENMO y Anglez fisiológicamente realistas con límites estocásticos
        double enmoMin, enmoMax, angMin, angMax;
        if (movioMano) {
            // Vigilia: ENMO más alto, Anglez cambia más
            enmoMin = nextGaussian(0.018, 0.003, 0.012, 0.025); // min ENMO vigilia
            enmoMax = nextGaussian(0.10, 0.03, 0.06, 0.15);     // max ENMO vigilia
            angMin = nextGaussian(-25, 8, -40, -10);            // min delta Ang vigilia
            angMax = nextGaussian(25, 8, 10, 40);               // max delta Ang vigilia
            enmoActual = nextGaussian(0.04, 0.02, enmoMin, enmoMax);
            // Refuerzo de límites fisiológicos para ENMO
            enmoActual = Math.max(0.004, Math.min(0.15, enmoActual));
            double deltaAng = nextGaussian(0, 15, angMin, angMax);
            anglezActual += deltaAng;
            // Refuerzo de límites para Anglez
            anglezActual = Math.max(-90, Math.min(90, anglezActual));
        } else {
            // Sueño: ENMO bajo, Anglez varía poco
            enmoMin = nextGaussian(0.006, 0.001, 0.004, 0.009); // min ENMO sueño
            enmoMax = nextGaussian(0.017, 0.002, 0.012, 0.022); // max ENMO sueño
            angMin = nextGaussian(-3, 1, -7, -1);               // min delta Ang sueño
            angMax = nextGaussian(3, 1, 1, 7);                  // max delta Ang sueño
            enmoActual = nextGaussian(0.01, 0.003, enmoMin, enmoMax);
            // Refuerzo de límites fisiológicos para ENMO
            enmoActual = Math.max(0.004, Math.min(0.022, enmoActual));
            double deltaAng = nextGaussian(0, 2, angMin, angMax);
            anglezActual += deltaAng;
            // Refuerzo de límites para Anglez
            anglezActual = Math.max(-90, Math.min(90, anglezActual));
        }
        // Limitar anglez entre -90 y 90
        anglezActual = Math.max(-90, Math.min(90, anglezActual));

        // Usar actividad manual si está fijada, si no usar ENMO normalizado
        double actividadActual = getActividadActual();
        
        // Probabilidad de sueño más inversamente correlacionada con movimiento
        // Si hay mucho movimiento, es muy difícil estar dormido
        double factorMovimiento = 1.0 - (probMov * 1.2); // Inversamente proporcional, amplificado
        factorMovimiento = Math.max(0.1, Math.min(1.0, factorMovimiento));
        
        double probDormidoBase = model.calcularProbabilidadDormido(actividadActual, luzN, sonidoN, estresN);
        probDormidoBase *= factorMovimiento; // Aplicar factor de movimiento
        
        // Ajuste circadiano para probabilidad de sueño - MUY IMPORTANTE
        int periodoActual = getPeriodoCircadiano();
        double ajusteCircadiano;
        double variabilidadCircadiana = nextGaussian(0, 0.15, -0.3, 0.3); // Variabilidad individual
        
        if (periodoActual == 0) {
            // Noche/Madrugada: FUERTE tendencia al sueño con variabilidad natural
            double progreso = getProgresoPeriodo();
            if (progreso < 0.3) {
                // Inicio de noche: transición al sueño (variable según la persona/día)
                double baseTransicion = 0.5 + 0.35 * (progreso / 0.3); // 0.5 -> 0.85
                ajusteCircadiano = baseTransicion + variabilidadCircadiana;
                // Some people go to bed early, others late
                ajusteCircadiano += nextGaussian(0, 0.12, -0.25, 0.25);
            } else if (progreso > 0.7) {
                // Final de noche: preparación para despertar (algunos despiertan antes)
                double baseDespertar = 0.85 - 0.4 * ((progreso - 0.7) / 0.3); // 0.85 -> 0.45
                ajusteCircadiano = baseDespertar + variabilidadCircadiana;
                // Early birds vs night owls
                ajusteCircadiano += nextGaussian(0, 0.18, -0.35, 0.25);
            } else {
                // Mitad de la noche: sueño profundo con micro-despertares ocasionales
                double baseProfundo = 0.9;
                ajusteCircadiano = baseProfundo + variabilidadCircadiana;
                // Occasional brief awakenings (bathroom, dreams, noise)
                if (Math.random() < 0.05) { // 5% chance of brief awakening
                    ajusteCircadiano -= nextGaussian(0.3, 0.1, 0.1, 0.5);
                }
            }
        } else if (periodoActual == 1) {
            // Día: FUERTE tendencia a la vigilia con siestas ocasionales
            double baseDia = 0.12;
            ajusteCircadiano = baseDia + variabilidadCircadiana;
            
            // Siestas ocasionales (especialmente después del almuerzo)
            double progresoDia = getProgresoPeriodo();
            if (progresoDia > 0.4 && progresoDia < 0.7) { // Período post-almuerzo
                if (Math.random() < 0.15) { // 15% chance of nap
                    ajusteCircadiano += nextGaussian(0.4, 0.15, 0.2, 0.6);
                }
            }
            
            // Fatigue accumulation during the day
            ajusteCircadiano += progresoDia * 0.1; // Gradual increase in sleepiness
        } else {
            // Tarde/Noche: Transición gradual hacia el sueño con gran variabilidad
            double progreso = getProgresoPeriodo();
            double baseTarde = 0.12 + 0.55 * progreso; // 0.12 -> 0.67
            ajusteCircadiano = baseTarde + variabilidadCircadiana;
            
            // Evening activities affect sleep propensity
            if (progreso > 0.5) { // Later evening
                // Some days people are more tired, others more alert
                ajusteCircadiano += nextGaussian(0, 0.2, -0.3, 0.4);
                
                // Weekend vs weekday patterns
                if (pasoActual % (96 * 7) > (96 * 5)) { // Weekend-like pattern
                    ajusteCircadiano -= nextGaussian(0.1, 0.05, 0, 0.2); // Stay up later
                }
            }
        }
        
        // Constrain to reasonable bounds but allow more flexibility
        ajusteCircadiano = Math.max(0.05, Math.min(0.98, ajusteCircadiano));
        
        // Aplicar el ajuste circadiano con más peso a la variabilidad natural
        probDormidoBase = probDormidoBase * 0.25 + ajusteCircadiano * 0.75;
        
        // Suavizar la probabilidad de sueño con memoria (inercia) reducida para más reactividad
        double memoria = nextGaussian(0.4, 0.1, 0.2, 0.6); // Variable memory (40% ± 10%)
        double promedioPrevio = 0.0;
        int N = nextGaussian(3, 1, 2, 5) > 3.5 ? 4 : 3; // Variable memory window
        int count = 0;
        for (int i = probDormidoHistory.size() - 1; i >= 0 && count < N; i--, count++) {
            promedioPrevio += probDormidoHistory.get(i);
        }
        if (count > 0) promedioPrevio /= count;
        else promedioPrevio = probDormidoBase;
        
        double probDormido = memoria * promedioPrevio + (1.0 - memoria) * probDormidoBase;
        
        // Hacer el rango de probabilidad de sueño más dinámico y variable
        double minProbDormido = nextGaussian(0.01, 0.02, 0.0, 0.08);
        double maxProbDormido = nextGaussian(0.97, 0.02, 0.85, 1.0);
        probDormido = Math.max(minProbDormido, Math.min(maxProbDormido, probDormido));
        
        // Add final random variation to make it more human-like
        double finalVariation = nextGaussian(0, 0.08, -0.15, 0.15);
        probDormido = Math.max(0.0, Math.min(1.0, probDormido + finalVariation));
        
        probDormidoHistory.add(probDormido);
        
        lastUmbral = calcularUmbralMovimiento(luzN, sonidoN, estresN);
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

    /**
     * Obtiene el período circadiano actual (0=Noche, 1=Día, 2=Tarde)
     */
    public int getPeriodoCircadiano() {
        double ciclo = pasoActual % (96 * 3);
        if (ciclo < 96) return 0; // Noche/Madrugada
        else if (ciclo < 192) return 1; // Día
        else return 2; // Tarde/Noche
    }

    /**
     * Obtiene el nombre del período circadiano actual
     */
    public String getNombrePeriodoCircadiano() {
        switch (getPeriodoCircadiano()) {
            case 0: return "Noche/Madrugada";
            case 1: return "Día";
            case 2: return "Tarde/Noche";
            default: return "Desconocido";
        }
    }

    /**
     * Obtiene el progreso dentro del período actual (0.0 - 1.0)
     */
    public double getProgresoPeriodo() {
        double ciclo = pasoActual % (96 * 3);
        int periodo = getPeriodoCircadiano();
        double inicioPeriodo = periodo * 96;
        return (ciclo - inicioPeriodo) / 96.0;
    }
}
