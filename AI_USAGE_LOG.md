# Universiti Malaya (UM) - Coursework AI Usage Log
**Course:** WIA2007 Mobile Application Development  
**Academic Year / Semester:** 2026/2027 Semester 1  
**Project:** UM ClimateReady (SDG 13 - Climate Action, Target 13.3)  
**Group:** Occurence 3  
**Policy Compliance:** Universiti Malaya Level 2 (Restricted AI Usage - Specific Tasks Only)

---

## 1. Compliance Statement & Ethics Declaration
This project strictly follows Universiti Malaya's Generative AI Policy (Level 2). Generative AI tools (e.g., Antigravity, Google Gemini, Android Studio AI Assistant) are utilized solely as intelligent engineering accelerators for scaffolding, boilerplate generation, code structure optimization, test case drafting, and documentation formatting. 

All core algorithmic design, deterministic scoring mathematics, state machine unlock logic, campus field data validation, and empirical system testing have been independently verified, debugged, and executed by team members. No AI-generated code is integrated without static code analysis, peer review, and device execution testing.

---

## 2. Team Member Summary

| Role | Responsibility | Assigned Member | Matric No. |
| :--- | :--- | :--- | :--- |
| **Role 1 (Leader)** | App Shell, Navigation Architecture, Base Room DB & Integration | Lau Wei Zhong | 25006511 |
| **Role 2** | Module 1: Climate Awareness Assessment | [Team Member 2] | [Matric No.] |
| **Role 3** | Module 2: Climate Micro-Learning Curriculum | [Team Member 3] | [Matric No.] |
| **Role 4** | Module 3: AI Scenario Quiz & JSON Validation Pipeline | [Team Member 4] | [Matric No.] |
| **Role 5** | Client API Adapters, Trigger Logic & Secure Key Config | [Team Member 5] | [Matric No.] |
| **Role 6** | Modules 4 & 5: Global Warming Explorer & UM Climate Directory | [Team Member 6] | [Matric No.] |

---

## 3. Log Records

### Log Entry #001: Week 6 Project Kickoff & Assets Scaffolding
* **Date:** 2026-11-12
* **Member / Role:** Lau Wei Zhong (Role 1: Leader & Shell Lead)
* **AI Tool Used:** Antigravity (Powered by Gemini)
* **Task Objective:** Initialize Android Studio project skeleton, configure clean `.gitignore`, set up Git branching policy (`main` & `develop`), and extract peer-reviewed curriculum specifications into structured JSON asset seeds.
* **Specific Prompts Inputted:**
  ```text
  Guide me on setting up GitHub for WIA2007 MADev course, initialize the project repository, ensure proper Android .gitignore to exclude local.properties and build caches, and parse peer-reviewed PDF question banks into valid Android assets.
  ```
* **AI Output Received:**
  - Standard Git setup and remote tracking instructions.
  - Recommended Kotlin + XML Views + minSdk 24 configuration.
  - Python data parser to extract 18 M1 questions, 12 M2 units, 60 M3 questions, and 12 M5 verified resources into clean JSON format.
* **Human Verification, Testing & Modification:**
  - Inspected `app/build.gradle.kts` to verify `minSdk = 24` and `targetSdk = 37`.
  - Verified `.gitignore` to ensure `local.properties` (with machine-specific SDK paths) is strictly untracked.
  - Executed parsing script and validated JSON schemas against course technical design specifications.
  - Committed and pushed initial skeleton to GitHub remote repository.

---

*(Team members: please append your sprint log entries below following this standard template after each completed feature)*

### Log Entry Template (Copy for new entries):
```markdown
### Log Entry #[Number]: [Sprint / Module Title]
* **Date:** YYYY-MM-DD
* **Member / Role:** [Your Name] (Role [X]: [Module])
* **AI Tool Used:** [Antigravity / Android Studio AI / Gemini]
* **Task Objective:** [Brief 1-sentence goal]
* **Specific Prompts Inputted:**
  ```text
  [Paste exact prompt used here]
  ```
* **AI Output Received:**
  [Brief summary of code snippets / boilerplate generated]
* **Human Verification, Testing & Modification:**
  [Explain how you reviewed, modified, and tested the code on emulator / unit tests to ensure it met course specifications]
```
