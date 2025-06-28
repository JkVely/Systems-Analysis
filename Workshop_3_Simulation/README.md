# Sleep and Movement Simulation System

## Abstract
This project implements a simulation framework for modeling the probability of movement and sleep states in humans, based on environmental and physiological variables. The simulator is inspired by scientific literature on actigraphy, sleep scoring algorithms, and probabilistic models, and is designed for academic and medical research purposes in sleep medicine and chronobiology.

## Introduction
Sleep is a fundamental biological process, and its assessment is crucial for understanding health and disease. Traditional methods such as polysomnography (PSG) are accurate but invasive and costly. Wearable actigraphy and computational models offer a non-invasive, scalable alternative for long-term sleep monitoring. This simulator generates realistic time series of movement activity (ENMO) and wrist orientation (Anglez) as a function of environmental factors (light, sound, stress) and internal sleep-wake dynamics, following established actigraphic protocols.

## System Overview

The simulation models the following variables with physiologically plausible ranges:

- **Light (lux):** [0–1000]. Simulates realistic day/night cycles with a 24-hour circadian pattern (3 periods of 8 hours each):
  - **Night/Early Morning (0-8h):** 0-200 lux (mean ~50 lux)
  - **Day (8-16h):** 400-1000 lux (mean ~700 lux) 
  - **Evening/Night (16-24h):** 50-400 lux (mean ~150 lux)
- **Sound (dB):** [20–80]. Models environmental noise with circadian variation:
  - **Nighttime:** 25-50 dB (quiet environments)
  - **Daytime:** 40-75 dB (active environments)
- **Stress:** [0–10]. Subjective scale based on perceived stress levels, updated smoothly with Gaussian noise (σ = 0.6).
- **Physical Activity (proxy ENMO):** [0–1]. Can be set manually or derived from simulated movement probability. Normalized for model input.
- **ENMO (g):** Euclidean Norm Minus One, consequence of movement state. During wake: ENMO ~ N(0.04, 0.02), range [0.012, 0.15]. During sleep: ENMO ~ N(0.01, 0.003), range [0.004, 0.022].
- **Anglez (degrees):** Wrist orientation relative to gravity. During wake: changes ~ N(0, 15°), range [-40°, 40°]. During sleep: changes ~ N(0, 2°), range [-7°, 7°]. Always constrained to [-90°, 90°] as per accelerometer specifications.

## Simulation Logic

- **Circadian Light Cycle:** The system implements a realistic 24-hour light cycle (288 steps = 24 hours). Every 6 steps, light and sound gradually move toward circadian targets, creating natural day-night transitions with stable periods within each circadian phase.
- **Circadian Sleep Regulation with Individual Variability:** The probability of sleep is strongly influenced by circadian timing but includes significant individual and daily variation:
  - **Night/Early Morning (0-8h):** High sleep probability (0.5-0.95) with natural variation for early/late sleepers, occasional brief awakenings, and stress from dreams or disturbances
  - **Day (8-16h):** Low sleep probability (~0.12) with occasional naps (15% chance during post-lunch period), gradual fatigue accumulation, and stress spikes from daily activities
  - **Evening (16-24h):** Gradual increase in sleep probability (0.12-0.67) with high individual variability, weekend effects, and occasional evening stress that can delay sleep onset
- **Environmental Variability:** Light, sound, and stress levels include realistic fluctuations and occasional events:
  - Stress spikes during the day (8% chance, representing meetings, deadlines, conflicts)
  - Evening stress events (12% chance, representing family issues, work concerns, insomnia triggers)  
  - Brief nighttime disturbances (3% chance, representing noise, nightmares, bathroom trips)
  - Morning anxiety (8% chance, representing anticipation of daily challenges)
- **Dynamic Memory System:** Sleep probability uses variable memory windows (2-5 steps) and adaptive weighting (20-60%) to simulate individual differences in sleep stability and responsiveness to environmental changes.
- The probability of movement is calculated using a logistic regression model with enhanced noise and individual variation.
- ENMO and Anglez are generated as consequences of the simulated movement state, following established actigraphic measurement protocols.
- Sleep-wake transitions include realistic resistance to change while allowing for natural human variability in sleep timing and quality.

## Mathematical Models
### 1. Probability of Movement (Enhanced Logistic Regression)
$$P_{movement} = \sigma(\beta_0 + \beta_{light} \cdot light_{norm} + \beta_{sound} \cdot sound_{norm} + \beta_{stress} \cdot stress_{norm} + \beta_{asleep} \cdot I_{asleep})$$

Where:
- $\sigma(x) = \frac{1}{1 + e^{-x}}$ is the logistic function
- $\beta_0 = -2.0$ (baseline intercept)
- $\beta_{light} = 3.5$ (light strongly promotes arousal)
- $\beta_{sound} = 2.8$ (sound promotes arousal/movement)
- $\beta_{stress} = 1.2$ (stress increases activity)
- $\beta_{asleep} = -2.5$ (being asleep strongly inhibits movement)
- Variables are normalized: $light_{norm} = \min(1.0, \frac{lux}{500})$, $sound_{norm} = \min(1.0, \frac{dB}{60})$, $stress_{norm} = \min(1.0, \frac{stress}{10})$

### 2. Probability of Sleep (Modified Sadeh Algorithm with Circadian Modulation)
$$P_{sleep} = 0.3 \cdot \sigma(\alpha_0 + \alpha_{activity} \cdot activity + \alpha_{light} \cdot light_{norm} + \alpha_{sound} \cdot sound_{norm} + \alpha_{stress} \cdot stress_{norm}) + 0.7 \cdot C_{circadian}$$

Where:
- $\alpha_0 = 3.0$ (baseline favoring sleep)
- $\alpha_{activity} = -2.0$ (activity strongly opposes sleep)
- $\alpha_{light} = -2.5$ (light strongly opposes sleep)
- $\alpha_{sound} = -1.8$ (noise opposes sleep)
- $\alpha_{stress} = -1.5$ (stress opposes sleep)
- $C_{circadian}$ is the circadian sleep modulation factor:
  - **Night/Early Morning (0-8h):** 0.6-0.95 (high sleep drive)
  - **Day (8-16h):** 0.15 (strong wake drive)  
  - **Evening (16-24h):** 0.15-0.65 (gradual sleep drive increase)

### 3. Dynamic Sleep/Wake Threshold
$$T_{threshold} = 0.04 + 0.20 \cdot sound_{norm} + 0.18 \cdot light_{norm} + 0.12 \cdot stress_{norm}$$

Constrained to [0.02, 0.25]. When $P_{movement} < T_{threshold}$, the state is classified as sleep.

### 4. Sleep State Inertia (Memory Mechanism)
$$P_{sleep}^{(t)} = 0.6 \cdot \overline{P_{sleep}^{(t-5:t-1)}} + 0.4 \cdot P_{sleep,base}^{(t)}$$

This weighted average over the previous 5 time steps models the natural resistance to rapid sleep-wake transitions.

## Usage
1. Ensure Java 11+ and JavaFX are installed
2. Build the project: `mvn clean compile`
3. Run the simulation: `mvn exec:java -Dexec.mainClass="edu.system_analysis.view.SimulationView"`
4. Interact with the GUI to adjust environmental parameters and observe real-time simulation
5. Use zoom (mouse wheel), pan (right-click drag), and reset (double-click) on all charts
6. Export data for further analysis using standard actigraphy analysis tools

## Applications
- **Academic Research:** Sleep-wake pattern analysis, circadian rhythm studies, sleep disorder modeling
- **Algorithm Development:** Training and validation of automated sleep scoring algorithms
- **Clinical Applications:** Understanding the impact of environmental factors on sleep quality
- **Educational Purposes:** Demonstrations of actigraphic principles and sleep science concepts

## Validation and Limitations
- The model parameters are based on published actigraphy studies and sleep research literature
- ENMO and Anglez ranges follow established guidelines from large-scale actigraphy datasets (UK Biobank, NHANES)
- Limitations include simplified environmental modeling and absence of individual circadian preference (chronotype) variations
- The simulation does not model sleep stages (REM, NREM) or micro-arousals explicitly

## References
- Sadeh, A., Sharkey, K. M., & Carskadon, M. A. (1994). Activity-based sleep-wake identification: An empirical test of methodological issues. Sleep, 17(3), 201-207.
- van Hees, V. T., et al. (2013). Separating movement and gravity components in an acceleration signal and implications for the assessment of human daily physical activity. PLoS One, 8(4), e61691.
- Hildebrand, M., et al. (2014). Age group comparability of raw accelerometer output from wrist-and hip-worn monitors. Medicine and Science in Sports and Exercise, 46(9), 1816-1824.
- Cole, R. J., et al. (1992). Automatic sleep/wake identification from wrist activity. Sleep, 15(5), 461-469.
- Doherty, A., et al. (2017). Large scale population assessment of physical activity using wrist worn accelerometers: The UK Biobank Study. PLoS One, 12(2), e0169649.

