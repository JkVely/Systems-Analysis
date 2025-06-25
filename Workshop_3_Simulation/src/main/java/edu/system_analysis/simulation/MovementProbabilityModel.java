package edu.system_analysis.simulation;

/**
 * Modelo probabilístico para estimar la probabilidad de movimiento y de estar dormido
 * en función de estímulos externos (luz, sonido, estrés) y estado previo.
 * <p>
 * Este modelo NO utiliza ENMO ni Anglez como parámetros de entrada, ya que estos
 * son consecuencias del movimiento y la postura, no causas. ENMO y Anglez pueden
 * ser simulados posteriormente a partir de la probabilidad de movimiento y el tipo
 * de estímulo recibido, pero no forman parte del cálculo de probabilidad.
 * <p>
 * El cálculo de probabilidad se basa en modelos estadísticos validados en la literatura,
 * como regresión logística y modelos de transición de Markov para sueño/vigilia.
 * <p>
 * Referencias:
 * - Quadens O, Dequae PA. Eye movements in sleep: towards a model. Rev Electroencephalogr Neurophysiol Clin. 1986.
 * - Vivaldi EA, Ocampo-Garcés A, Villegas R. Short-term homeostasis of REM sleep. Sleep. 2005.
 * - Merica H, Fortune RD. The neuronal transition probability (NTP) model for sleep structure. PLoS One. 2011.
 *
 * Limitaciones y sesgos:
 * - No se consideran parámetros fisiológicos directos (EEG, EMG, etc.), solo proxies ambientales y conductuales.
 * - El modelo asume independencia entre estímulos, lo cual es una simplificación.
 * - No se modelan transiciones ultradianas ni microdespertares explícitamente.
 *
 * Justificación médica/académica:
 * - La probabilidad de transición entre sueño y vigilia puede modelarse como un proceso estocástico de primer orden
 *   (semi-Markov), donde la probabilidad depende de la historia reciente y de estímulos externos (ver referencias).
 * - La regresión logística es ampliamente utilizada para modelar la probabilidad de eventos binarios en fisiología.
 */
public class MovementProbabilityModel {
    // Coeficientes basados en literatura científica validada (ver README)
    // Modelo para probabilidad de actividad/movimiento basado en estímulos externos
    private double beta0 = -1.5; // intercepto base
    private double betaLuz = -0.6; // luz reduce prob. movimiento (facilitación del sueño)
    private double betaSonido = 0.4; // sonido aumenta prob. movimiento (arousal)
    private double betaEstres = 0.8; // estrés aumenta activación/movimiento
    private double betaDormido = -1.8; // estar dormido reduce drasticamente prob. movimiento

    // Algoritmo modificado Sadeh para clasificación sueño/vigilia
    // PS = offset + w1*meanActivity + w2*lightLevel + w3*noiseLevel + w4*stressLevel
    // PS >= 0 == sleep, PS < 0 == wake
    private double sadehOffset = 2.1; // intercepto positivo para favorecer sueño
    private double sadehActivity = -1.2; // alta actividad reduce prob. sueño
    private double sadehLight = -0.8; // luz reduce prob. sueño
    private double sadehNoise = -0.5; // ruido reduce prob. sueño
    private double sadehStress = -0.9; // estrés reduce prob. sueño

    /**
     * Calcula la probabilidad de que ocurra un movimiento voluntario o involuntario
     * en función de los estímulos externos y el estado previo de sueño/vigilia.
     * <p>
     * ENMO y Anglez NO son parámetros de entrada ni se calculan aquí.
     *
     * @param luz         Intensidad de luz ambiental (arbitrario, normalizado 0-1)
     * @param sonido      Nivel de sonido ambiental (arbitrario, normalizado 0-1)
     * @param estres      Nivel de estrés percibido (arbitrario, normalizado 0-1)
     * @param estabaDormido true si el sujeto estaba dormido en el instante previo
     * @return Probabilidad [0,1] de que ocurra movimiento
     */
    public double calcularProbabilidadMovimiento(double luz, double sonido, double estres, boolean estabaDormido) {
        double x = beta0
                + betaLuz * luz
                + betaSonido * sonido
                + betaEstres * estres
                + betaDormido * (estabaDormido ? 1 : 0);
        return sigmoid(x);
    }

    /**
     * Calcula la probabilidad de estar dormido en función de la actividad actual
     * (derivada de movimiento) y estímulos externos.
     * <p>
     * Este método NO utiliza ENMO ni Anglez como entrada.
     *
     * @param actividadActual Nivel de actividad física reciente (proxy de movimiento)
     * @param luz             Intensidad de luz ambiental (arbitrario, normalizado 0-1)
     * @param sonido          Nivel de sonido ambiental (arbitrario, normalizado 0-1)
     * @param estres          Nivel de estrés percibido (arbitrario, normalizado 0-1)
     * @return Probabilidad [0,1] de estar dormido
     */
    public double calcularProbabilidadDormido(double actividadActual, double luz, double sonido, double estres) {
        double ps = sadehOffset
                + sadehActivity * actividadActual
                + sadehLight * luz
                + sadehNoise * sonido
                + sadehStress * estres;
        // PS >= 0 indica sueño, PS < 0 indica vigilia
        // Convertir a probabilidad [0,1]
        return sigmoid(ps);
    }

    /**
     * Función logística estándar para convertir un valor en probabilidad [0,1].
     *
     * @param x Valor de entrada
     * @return Probabilidad [0,1]
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * Permite ajustar los coeficientes del modelo de movimiento si se desea calibrar
     * con datos experimentales o literatura.
     *
     * @param beta0        Intercepto base
     * @param betaLuz      Coeficiente para luz
     * @param betaSonido   Coeficiente para sonido
     * @param betaEstres   Coeficiente para estrés
     * @param betaDormido  Coeficiente para estar dormido
     * @deprecated Los parámetros ENMO y Anglez no se usan ni se almacenan, solo se mantienen para compatibilidad.
     */
    @Deprecated
    public void setCoeficientesMovimiento(double beta0, double betaLuz, double betaSonido, double betaEstres, double betaEnmo, double betaAnglez, double betaDormido) {
        this.beta0 = beta0;
        this.betaLuz = betaLuz;
        this.betaSonido = betaSonido;
        this.betaEstres = betaEstres;
        this.betaDormido = betaDormido;
        // betaEnmo y betaAnglez ignorados intencionalmente
    }

    /**
     * Permite ajustar los coeficientes del modelo de sueño si se desea calibrar
     * con datos experimentales o literatura.
     * Solo se pueden ajustar los coeficientes del modelo Sadeh modificado.
     *
     * @param sadehOffset   Intercepto base
     * @param sadehActivity Coeficiente para actividad
     * @param sadehLight    Coeficiente para luz
     * @param sadehNoise    Coeficiente para ruido
     * @param sadehStress   Coeficiente para estrés
     */
    public void setCoeficientesDormido(double sadehOffset, double sadehActivity, double sadehLight, double sadehNoise, double sadehStress) {
        this.sadehOffset = sadehOffset;
        this.sadehActivity = sadehActivity;
        this.sadehLight = sadehLight;
        this.sadehNoise = sadehNoise;
        this.sadehStress = sadehStress;
    }
}