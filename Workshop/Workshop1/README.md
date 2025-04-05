# Workshop 1

- [Workshop 1](#workshop-1)
  - [Overview](#overview)
  - [Methodology](#methodology)
    - [Second best solutions to the kaggle competitions diagram](#second-best-solutions-to-the-kaggle-competitions-diagram)
  - [Analysis steps](#analysis-steps)
  - [Results](#results)

## Overview

This workshop is a systemic analysis of the prized competition of Kaggle [Child Mind Institute - Detect Sleep States](https://www.kaggle.com/competitions/child-mind-institute-detect-sleep-states), which consists of a system that detects
sleep states using accelerometers. The system is designed to analyze the data collected from accelerometers and
classify different sleep states based on the patterns observed in the data. The analysis includes a detailed examination
of the system's components, their interactions, and the emergent behaviors that emerges from these interactions.
By understanding the system's dynamics, we aim to identify potential areas for improvement and optimization,
ultimately enhancing the accuracy and reliability of sleep state detection.

## Methodology

To analyze this Kaggle competition, we approached the problem by studying the competition overview, requirements, goals, and provided code examples, focusing on understanding the input data and expected outputs.

Since the competition had already concluded at the time of our analysis, we examined the winning solutions to gain better insight into the problem. We paid particular attention to the second-best solution, which offered a significant systems approach to solving the challenge.

### Second best solutions to the kaggle competitions diagram

![Second-best-solution](/Workshop/Workshop1/second-solution-diagram.png)

## Analysis steps

Approaching the problem following these top-down abstraction steps we create a robust model for detecting sleep states from accelerometer data. This involves classifying periods of sleep and wakefulness by analyzing time-series data from wearable sensors. The model should generalize well across individuals and account for variations in sleep patterns to provide insights into sleep and potential sleep disorders. Ultimately, the goal is to improve awareness and guidance surrounding the importance of sleep.

- System elements : How the systems is compound by enough elements with its specific task in order to get the main goal of the system
- Elements interactions: How elements are related in order to reach process that aim the main goal
- Emergent behaviours : How the differents process works together to generate the behaviour of the system

## Results

[System approach to Detect sleep states problem report](/Workshop/Workshop1/Workshop1.pdf)
