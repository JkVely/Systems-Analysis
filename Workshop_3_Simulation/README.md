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

### Fórmulas de probabilidad detalladas

#### 1. Probabilidad de movimiento (modelo de regresión logística)

La probabilidad de que ocurra un movimiento voluntario o involuntario se calcula mediante:

$$
P_{move} = \sigma(\beta_0 + \beta_{luz} \cdot luz + \beta_{sonido} \cdot sonido + \beta_{estrés} \cdot estrés + \beta_{dormido} \cdot I_{dormido})
$$

**Donde:**
- **σ (sigma)** = Función sigmoide/logística: $\sigma(x) = \frac{1}{1 + e^{-x}}$, que convierte cualquier valor real en una probabilidad entre 0 y 1
- **β (beta)** = Coeficientes de regresión logística (pesos) que determinan la influencia de cada variable:
  - **β₀ = -1.5** (intercepto base): probabilidad basal de movimiento cuando todas las variables son 0
  - **β_luz = -0.6**: la luz reduce la probabilidad de movimiento (facilita el sueño)
  - **β_sonido = 0.4**: el sonido aumenta la probabilidad de movimiento (arousal/activación)
  - **β_estrés = 0.8**: el estrés aumenta significativamente la probabilidad de movimiento
  - **β_dormido = -1.8**: estar dormido reduce drásticamente la probabilidad de movimiento
- **I_dormido** = Variable indicadora (0 si despierto, 1 si dormido en el paso anterior)
- **luz, sonido, estrés** = Variables normalizadas entre 0 y 1

**¿Cómo se obtiene la probabilidad?**
1. Se multiplica cada variable por su coeficiente β correspondiente
2. Se suman todos los productos más el intercepto β₀
3. Se aplica la función sigmoide σ para obtener una probabilidad entre 0 y 1
4. Se compara esta probabilidad con un número aleatorio uniforme [0,1] para decidir si ocurre movimiento

#### 2. Probabilidad de estar dormido (modelo Sadeh modificado)

La probabilidad de estar dormido se calcula mediante:

$$
P_{sleep} = \sigma(\alpha_0 + \alpha_{actividad} \cdot actividad + \alpha_{luz} \cdot luz + \alpha_{sonido} \cdot sonido + \alpha_{estrés} \cdot estrés)
$$

**Donde:**
- **σ (sigma)** = Misma función sigmoide que antes: $\sigma(x) = \frac{1}{1 + e^{-x}}$
- **α (alfa)** = Coeficientes del algoritmo Sadeh modificado:
  - **α₀ = 2.1** (intercepto positivo): favorece el estado de sueño como estado basal
  - **α_actividad = -1.2**: alta actividad reduce la probabilidad de estar dormido
  - **α_luz = -0.8**: la luz reduce la probabilidad de estar dormido
  - **α_sonido = -0.5**: el ruido reduce la probabilidad de estar dormido
  - **α_estrés = -0.9**: el estrés reduce la probabilidad de estar dormido
- **actividad** = Valor de ENMO normalizado [0,1] o valor manual del slider de actividad física

**¿Cómo se obtiene la probabilidad?**
1. Se calcula el "Sadeh Score" (PS): PS = α₀ + Σ(αᵢ × variableᵢ)
2. Se aplica la función sigmoide σ(PS) para convertir el score en probabilidad [0,1]
3. En el algoritmo Sadeh original: PS ≥ 0 indica sueño, PS < 0 indica vigilia
4. Aquí se usa la versión probabilística σ(PS) para obtener valores continuos

#### 3. Justificación de los coeficientes

Los valores de β y α están basados en:
- **Literatura científica validada** sobre actigrafía y modelos de sueño (ver bibliografía)
- **Calibración empírica** para reproducir patrones fisiológicos realistas
- **Competencia Kaggle Child Mind Institute** - Detect Sleep States
- **Principios fisiológicos**: luz y ruido como inhibidores del sueño, estrés como activador

#### 4. Explicación intuitiva de las funciones matemáticas

**¿Qué significa σ (sigma)?**
- Es una función matemática que "aplasta" cualquier número (positivo o negativo) a un rango entre 0 y 1
- Imagínala como un "filtro" que convierte puntuaciones en probabilidades
- Si el número es muy negativo (-∞): σ → 0 (probabilidad muy baja)
- Si el número es cero: σ → 0.5 (probabilidad neutral, 50%)
- Si el número es muy positivo (+∞): σ → 1 (probabilidad muy alta)

**¿Qué significan β (beta) y α (alfa)?**
- Son "pesos" o "multiplicadores" que determinan qué tan importante es cada variable
- Un β/α **negativo** significa que la variable **reduce** la probabilidad
- Un β/α **positivo** significa que la variable **aumenta** la probabilidad
- Un valor más **grande** (en valor absoluto) significa mayor influencia

**Ejemplo práctico:**
- β_luz = -0.6: Cuando hay mucha luz, la probabilidad de movimiento baja (porque facilita el sueño)
- β_sonido = 0.4: Cuando hay mucho sonido, la probabilidad de movimiento sube (porque activa/despierta)
- β_estrés = 0.8: El estrés tiene un efecto grande y positivo en el movimiento
- α_actividad = -1.2: Mucha actividad física reduce fuertemente la probabilidad de estar dormido

**¿Cómo funciona en conjunto?**
1. Se multiplica cada variable por su peso (β o α)
2. Se suman todos los resultados + un valor base (intercepto)
3. Se aplica σ para obtener una probabilidad entre 0% y 100%
4. Se lanza una "moneda virtual" con esa probabilidad para decidir qué ocurre

#### 5. Ejemplos numéricos de cálculo

**Ejemplo 1: Ambiente silencioso y oscuro (noche)**
```
luz = 0.1, sonido = 0.2, estrés = 0.3, dormido_anterior = 1

Cálculo P_movimiento:
suma = -1.5 + (-0.6×0.1) + (0.4×0.2) + (0.8×0.3) + (-1.8×1)
suma = -1.5 - 0.06 + 0.08 + 0.24 - 1.8 = -3.04
P_movimiento = σ(-3.04) = 1/(1+e^3.04) ≈ 0.046 = 4.6%
→ Muy baja probabilidad de movimiento (persona dormida)
```

**Ejemplo 2: Ambiente ruidoso y luminoso (día)**
```
luz = 0.8, sonido = 0.7, estrés = 0.5, dormido_anterior = 0

Cálculo P_movimiento:
suma = -1.5 + (-0.6×0.8) + (0.4×0.7) + (0.8×0.5) + (-1.8×0)
suma = -1.5 - 0.48 + 0.28 + 0.4 + 0 = -1.3
P_movimiento = σ(-1.3) = 1/(1+e^1.3) ≈ 0.214 = 21.4%
→ Probabilidad moderada de movimiento (persona despierta pero tranquila)
```

**Ejemplo 3: Alta actividad y estrés**
```
actividad = 0.6, luz = 0.5, sonido = 0.4, estrés = 0.8

Cálculo P_sueño:
suma = 2.1 + (-1.2×0.6) + (-0.8×0.5) + (-0.5×0.4) + (-0.9×0.8)
suma = 2.1 - 0.72 - 0.4 - 0.2 - 0.72 = 0.06
P_sueño = σ(0.06) = 1/(1+e^-0.06) ≈ 0.515 = 51.5%
→ Probabilidad neutral de estar dormido (zona de transición)
```

Esta simulación permite explorar cómo diferentes combinaciones de variables ambientales y fisiológicas afectan las probabilidades de movimiento y sueño en tiempo real.

- Si ocurre movimiento (según $P_{move}$), se simulan ENMO y Anglez con las distribuciones mencionadas.

### Proceso completo de obtención de datos de probabilidad

Para clarificar completamente cómo se obtienen las probabilidades, aquí está el proceso paso a paso:

**1. Entradas del sistema:**
- Variables ambientales: luz [0-1000 lux] → normalizada a [0-1]
- Sonido [20-80 dB] → normalizada a [0-1]  
- Estrés [0-10] → normalizada a [0-1]
- Estado previo de sueño [0 o 1]
- Actividad física manual [0-1] (si está habilitada)

**2. Cálculo de probabilidad de movimiento:**
```
suma_movimiento = -1.5 + (-0.6 × luz) + (0.4 × sonido) + (0.8 × estrés) + (-1.8 × dormido_anterior)
P_movimiento = 1 / (1 + e^(-suma_movimiento))
```

**3. Decisión estocástica de movimiento:**
```
número_aleatorio = random.uniform(0, 1)
ocurre_movimiento = (número_aleatorio < P_movimiento)
```

**4. Generación de ENMO y Anglez (consecuencias del movimiento):**
```
si ocurre_movimiento:
    ENMO = normal(media=0.13, desviación=0.04) truncada a [0.05, 0.25] g
    Anglez = uniforme(10, 60) grados
sino:
    ENMO = normal(media=0.025, desviación=0.01) truncada a [0.01, 0.05] g  
    Anglez = uniforme(0, 5) grados
```

**5. Cálculo de probabilidad de sueño:**
```
actividad = ENMO_normalizado (0-1) o valor_manual_slider
suma_sueño = 2.1 + (-1.2 × actividad) + (-0.8 × luz) + (-0.5 × sonido) + (-0.9 × estrés)
P_sueño = 1 / (1 + e^(-suma_sueño))
```

**6. Decisión estocástica de sueño:**
```
número_aleatorio2 = random.uniform(0, 1)
está_dormido = (número_aleatorio2 < P_sueño)
```

**7. Visualización:**
- Se grafican las probabilidades calculadas (líneas continuas)
- Se grafican los valores de ENMO y Anglez generados (como consecuencias)
- Se muestra el umbral dinámico como línea punteada

Este proceso se repite cada paso de simulación (cada 30 segundos simulados), donde las variables ambientales cambian ligeramente por ruido gaussiano, y cada 12 pasos pueden ocurrir cambios abruptos en luz y sonido.

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

