# Sleep State Detection: Systematic Analysis and Computational Approach

This repository contains the final research project for the "Analysis and Design of Systems" course, presenting a systematic analysis and computational approach for sleep state prediction using accelerometry data. The project is contextualized within the framework of the Kaggle competition "Child Mind Institute - Detect Sleep States" and employs rigorous analytical methodologies to address the challenges of non-invasive sleep monitoring.

## 📂 Repository Structure

```
Project/
└── Paper/
    └── sleep_detection_system_paper.tex   # Main IEEE-formatted research paper in LaTeX
    └── sleep_detection_system_paper.pdf   # Compiled PDF of the research paper
    └── figures/                           # Visual representations and diagrams
```

## 🔍 Research Methodology

The research methodology employed in this project followed a systematic literature review approach:

1. **Initial Query Formulation**: The investigation began with broad search terms including "sleep state detection", "accelerometry sleep monitoring", and "psg sleep state" on Google Scholar.

2. **Iterative Refinement**: Search queries were progressively refined to include more specific terms such as "wrist-worn accelerometry validation", "machine learning sleep classification", and "polysomnography comparison studies".

3. **Citation Network Analysis**: Key papers were identified and their citation networks explored to discover seminal works and recent advancements in the field.

4. **Inclusion Criteria**: Selected literature met specific criteria including peer-reviewed status, methodological rigor, and relevance to accelerometry-based sleep detection.

5. **Theoretical Framework Development**: Based on the literature review, a comprehensive theoretical framework was constructed to guide the analysis and design of the sleep detection system.

## 📚 Paper Content and Structure

The paper presents a comprehensive academic analysis of sleep state detection using accelerometry data, structured according to the following sections as they appear in the manuscript:

1. **Introduction**: Establishes the theoretical foundation of sleep physiology, the significance of sleep monitoring, and the research gap addressed by accelerometry-based approaches.

2. **Theoretical Framework: Computational Models for Sleep State Prediction**: Examines various approaches for sleep state prediction, including threshold-based algorithms and deep learning implementations.

3. **Methods and Materials: Systematic Analysis Using Accelerometry Data**: Details data sources, variables analyzed, comparison between polysomnography and accelerometry, system description, and feature extraction methodologies.

4. **Implementation Approaches and Results from the Literature**: Reviews established model implementations, evaluation metrics and validation approaches, and challenges and limitations identified in previous research.

5. **Discussion**: Analyzes the potential and limitations of accelerometry-based sleep monitoring, with a subsection on limitations and future work.

6. **Systematic Approach to Sleep State Analysis**: Presents a hierarchical framework that integrates clinical sleep research methodologies with modern computational techniques, with a subsection on sleep transitions and population-specific considerations.

7. **Conclusion**: Summarizes the key findings and implications of the research.

## 🔬 Research Insights

The research identified several key insights:

- Accelerometry provides a viable, non-invasive alternative to traditional polysomnography for large-scale sleep monitoring and a non-invasive alternative for sleep state detection
- Machine learning approaches, particularly random forests and LSTM networks, demonstrate promising accuracy in sleep/wake classification
- The sensitivity-specificity tradeoff remains a significant challenge, with specificity (wake detection) consistently lower than sensitivity (sleep detection)
- Transition periods between sleep states present particular classification challenges for accelerometry-based methods
- Individual differences in movement patterns significantly impact algorithm performance, suggesting the need for personalized approaches

## 🔗 Academic Resources

- [Kaggle Competition](https://www.kaggle.com/competitions/child-mind-institute-detect-sleep-states): Original data source and problem definition
- [Workshop 1: Systemic Analysis](/Workshop/Workshop1/): Preliminary analytical framework
- [Workshop 2: System Design](/Workshop/Workshop_2_Design/): System architecture and methodological considerations
- [AASM Guidelines](https://aasm.org/clinical-resources/scoring-manual/): American Academy of Sleep Medicine scoring manual
