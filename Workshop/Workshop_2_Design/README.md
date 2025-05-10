# Workshop 2: System Design for Detecting Sleep States Using Accelerometers

## Overview

This workshop builds upon the systemic analysis from Workshop 1, focusing on the design of a robust system for detecting sleep states using wrist-worn accelerometer data. The project is based on the [Child Mind Institute - Detect Sleep States](https://www.kaggle.com/competitions/child-mind-institute-detect-sleep-states) Kaggle competition, which aims to develop a model that can accurately classify sleep states based on accelerometer data and predict sleep states.

## Development Process

- **Initial Review:** We started by reviewing the findings from Workshop 1, identifying key constraints, data characteristics, data omitted on previous analysis and chaos-theory factors that impact system design.
- **Requirements Definition:** Functional and non-functional requirements were derived from the competition context and systemic analysis. These include performance, reliability, scalability, interpretability, privacy, and adaptability.
- **Prioritization and Documentation:** Requirements were prioritized (essential, desirable, optional) and made measurable for validation.
- **Stakeholder Needs:** The system was designed to support researchers, clinicians, and families, ensuring ethical handling of sensitive health data and providing interpretable results.
- **High-Level Architecture:** The architecture was defined to illustrate the data flow from extraction and preprocessing to model training, prediction, and feedback.

## Architecture Diagrams

The high-level system architecture is illustrated in the following diagram:

<div style="text-align: center; background-color: white; padding: 10px;">
    <img src="system.drawio%281%29.png" alt="High-Level Architecture Diagram" style="background-color: white; max-width: 100%; height: auto;">
</div>

## Final Report

The complete analysis, requirements, and architecture are documented in the final PDF:

- [Workshop 2 Report (PDF)](workshop2.pdf)

---

This workshop demonstrates a structured approach to system design, emphasizing the importance of understanding the context, defining clear requirements, and creating a robust architecture. The system is designed to be adaptable, scalable, and interpretable, ensuring it meets the needs of various stakeholders while addressing ethical considerations in handling sensitive health data.