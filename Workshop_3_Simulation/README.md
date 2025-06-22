# Simulación de Probabilidad de Movimiento y Sueño

## Descripción general

Este simulador reproduce la dinámica de sueño y vigilia usando variables ambientales y fisiológicas inspiradas en la literatura científica sobre actigrafía y modelos probabilísticos de transición de sueño. El sistema NO usa ENMO ni Anglez como entradas, sino que los genera como consecuencia del movimiento, siguiendo la lógica de los estudios más recientes y la competencia de Kaggle Child Mind Institute - Detect Sleep States.

## Variables simuladas y su factor de caos

- **Luz (lux):** [0–1000]. Simula ciclos día/noche y cambios ambientales. El valor varía en cada paso con ruido gaussiano (caos leve) y cada 12 pasos puede cambiar abruptamente (caos fuerte, simula cambios de ambiente).
- **Sonido (dB):** [20–80]. Simula ambiente ruidoso o silencioso. Varía con ruido gaussiano y también puede cambiar abruptamente cada 12 pasos (caos fuerte).
- **Estrés:** [0–10]. Escala subjetiva, varía suavemente con ruido gaussiano (caos leve, simula estrés fisiológico o psicológico).
- **Actividad física (proxy ENMO):** [0–1]. Puede ser fijada manualmente o simulada. Si se simula, depende de la probabilidad de movimiento y se calcula como ENMO normalizado.
- **ENMO (g):** Consecuencia del movimiento. Si hay movimiento voluntario, ENMO se genera con una distribución normal $N(0.13, 0.04)$ truncada a [0.05, 0.25]. Si no, $N(0.025, 0.01)$ truncada a [0.01, 0.05]. El caos es moderado, ya que depende de la estocasticidad del movimiento.
- **Anglez (°):** Consecuencia del movimiento. Si hay movimiento voluntario, cambia aleatoriamente entre 10–60°; si no, solo 0–5°. El caos es bajo, pero puede acumularse con el tiempo.

## Organización y cálculo de las variaciones

- Cada variable ambiental (luz, sonido, estrés) se actualiza en cada paso con una variación gaussiana (ruido blanco). Cada 12 pasos, luz y sonido pueden cambiar abruptamente para simular eventos reales (apagar/encender luz, abrir ventana, etc.).
- La probabilidad de movimiento se calcula usando una regresión logística sobre los estímulos y el estado previo de sueño:

$$
P_{move} = \sigma(\beta_0 + \beta_{luz} \cdot luz + \beta_{sonido} \cdot sonido + \beta_{estrés} \cdot estrés + \beta_{dormido} \cdot I_{dormido})
$$

- Si ocurre movimiento (según $P_{move}$), se simulan ENMO y Anglez con las distribuciones mencionadas.
- La probabilidad de estar dormido se calcula con un modelo tipo Sadeh modificado:

$$
P_{sleep} = \sigma(\alpha_0 + \alpha_{actividad} \cdot actividad + \alpha_{luz} \cdot luz + \alpha_{sonido} \cdot sonido + \alpha_{estrés} \cdot estrés)
$$

Donde **actividad** es el valor de ENMO normalizado o el valor manual del slider.

## Cálculos estocásticos y caos

- **Ruido gaussiano:** Todas las variables ambientales y fisiológicas varían con ruido gaussiano, lo que introduce caos y variabilidad fisiológica realista.
- **Eventos abruptos:** Cada 12 pasos, luz y sonido pueden cambiar bruscamente, simulando eventos impredecibles (caos fuerte).
- **Movimiento:** La decisión de movimiento es estocástica, usando la probabilidad calculada y un número aleatorio uniforme.
- **ENMO y Anglez:** Se generan con distribuciones normales truncadas, lo que introduce variabilidad fisiológica realista y caos moderado.

## Ejemplo de ciclo de simulación

1. Se actualizan luz, sonido y estrés con ruido gaussiano.
2. Cada 12 pasos, luz y sonido pueden cambiar abruptamente.
3. Se calcula la probabilidad de movimiento con la fórmula logística.
4. Se decide estocásticamente si hay movimiento.
5. Si hay movimiento, se generan ENMO y Anglez con mayor variabilidad; si no, con menor variabilidad.
6. Se calcula la probabilidad de estar dormido usando la actividad (ENMO normalizado o manual).
7. Se actualizan las gráficas y la interfaz.

## Justificación científica

- El modelo sigue la lógica de algoritmos validados en la literatura de actigrafía y sueño, como Sadeh, Cole-Kripke y modelos de transición probabilística (ver referencias).
- La regresión logística es el estándar para modelar la probabilidad de eventos binarios en fisiología y sueño.
- ENMO y Anglez son métricas derivadas del movimiento, nunca entradas del modelo probabilístico (ver [van Hees 2015], [Sadeh 2011]).
- El caos y la variabilidad fisiológica se modelan explícitamente con ruido gaussiano y eventos abruptos, siguiendo recomendaciones de la literatura y la competencia de Kaggle.

## Limitaciones y sesgos

- No se consideran parámetros fisiológicos directos (EEG, EMG, etc.), solo proxies ambientales y conductuales.
- El modelo asume independencia entre estímulos, lo cual es una simplificación.
- No se modelan transiciones ultradianas ni microdespertares explícitamente.
- El umbral para distinguir entre movimiento voluntario/involuntario es dinámico y puede variar según el contexto simulado.

## Bibliografía académica

- Quadens O, Dequae PA. "Eye movements in sleep: towards a model." Rev Electroencephalogr Neurophysiol Clin. 1986. https://pubmed.ncbi.nlm.nih.gov/3764033/
- Vivaldi EA, Ocampo-Garcés A, Villegas R. "Short-term homeostasis of REM sleep." Sleep. 2005. https://pubmed.ncbi.nlm.nih.gov/16218076/
- Merica H, Fortune RD. "The neuronal transition probability (NTP) model for sleep structure." PLoS One. 2011. https://pubmed.ncbi.nlm.nih.gov/21886801/
- Ancoli-Israel S, et al. "The role of actigraphy in the study of sleep and circadian rhythms." Sleep. 2003. https://pubmed.ncbi.nlm.nih.gov/14655915/
- Basner M, et al. "Auditory and non-auditory effects of noise on health." Lancet. 2014. https://pubmed.ncbi.nlm.nih.gov/25194255/
- Meerlo P, et al. "Sleep restriction alters the hypothalamic-pituitary-adrenal response to stress." J Neuroendocrinol. 2002. https://pubmed.ncbi.nlm.nih.gov/12472889/
- van Hees VT, et al. "A novel, open access method to assess sleep duration using a wrist-worn accelerometer." PLoS One. 2015. https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0142533
- Sadeh A. "The role and validity of actigraphy in sleep medicine: an update." Sleep Med Rev. 2011. https://pubmed.ncbi.nlm.nih.gov/21334746/
- Neishabouri A, et al. "Quantification of acceleration as activity counts in ActiGraph wearable." Nature Sci Rep. 2022. https://www.nature.com/articles/s41598-022-16003-x
- https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8691611/

