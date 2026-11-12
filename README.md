# UM ClimateReady 🌧️☀️🌿

**Educational Android Application for Climate Preparedness and Awareness**  
Aligned with **United Nations Sustainable Development Goal 13 (Climate Action - Target 13.3)**  

* **Course:** WIA2007 Mobile Application Development (Occurence 3)
* **Institution:** Universiti Malaya (UM), Faculty of Computer Science & Information Technology (FCSIT)
* **Team:** 6 Members | **Platform:** Native Android (Kotlin) | **Min SDK:** API 24 (Android 7.0)

---

## 📌 Quick Links & Core Project Documents

* 📖 **[AI-Native Development Plan & Prompt Guide (docs/DEVELOPMENT_PLAN_AI_GUIDE.md)](docs/DEVELOPMENT_PLAN_AI_GUIDE.md)**  
  *Contains complete specifications for all 5 modules, architecture rules, and dual-mode AI prompts (Antigravity & Android Studio) for all 6 team roles.*
* 📝 **[Coursework AI Usage Log (AI_USAGE_LOG.md)](AI_USAGE_LOG.md)**  
  *Mandatory transparent audit trail adhering to Universiti Malaya Level 2 (Restricted) AI Policy.*
* 📦 **[Offline JSON Asset Seeds (app/src/main/assets/data/)](app/src/main/assets/data/)**  
  *Pre-loaded peer-reviewed question banks (M1: 18 MCQs, M2: 12 Units, M3: 60 Scenarios, M5: 12 Verified Resources).*

---

## 📱 Five Core Modules

1. **Module 1 --- Climate Awareness Assessment:** 9-MCQ diagnostic pre/post test with deterministic scoring (0–9) and color-coded recommendations.
2. **Module 2 --- Climate Micro-Learning:** 12 offline units (Heavy Rain, Extreme Heat, Haze) with real-world Malaysian context and CCNA-style instant verification.
3. **Module 3 --- AI Scenario Quiz:** Dynamic 3-scenario decision challenges with strict 8s timeout, JSON schema validation, and instant fallback to 60-question seed pool.
4. **Module 4 --- Global Warming Explorer:** 6-step greenhouse effect walkthrough and IPCC-sourced +1.5°C, +2°C, +3°C scenario comparison matrix.
5. **Module 5 --- UM Climate Resources:** Verified directory of 12 campus emergency contacts (BKU, JPPHB, PKU UM, UMSDC) and national portals (APIMS, MET Malaysia).

---

## 🛠️ Architecture & Tech Stack

* **Language:** Kotlin
* **UI Framework:** Android XML Layouts + ViewBinding + Material Design 3
* **Architecture Pattern:** MVVM (Model - View - ViewModel) + Repository Pattern
* **Local Persistence:** Room Database + Pre-packaged JSON Asset Seeding
* **AI Security:** Client-side BuildConfig injection (`local.properties`), strict JSON parsing, no secrets in Git/APK.

---

## 👥 Team Roles & Ownership

| Role | Domain / Module | Assigned Member | Matric No. |
| :--- | :--- | :--- | :--- |
| **Role 1 (Leader)** | App Shell, Navigation Architecture, Base Room DB & Integration | Lau Wei Zhong | 25006511 |
| **Role 2** | Module 1: Climate Awareness Assessment | [Team Member 2] | [Matric No.] |
| **Role 3** | Module 2: Climate Micro-Learning Curriculum | [Team Member 3] | [Matric No.] |
| **Role 4** | Module 3: AI Scenario Quiz & JSON Validation Pipeline | [Team Member 4] | [Matric No.] |
| **Role 5** | Client API Adapters, Trigger Logic & Secure Key Config | [Team Member 5] | [Matric No.] |
| **Role 6** | Modules 4 & 5: Global Warming Explorer & UM Climate Directory | [Team Member 6] | [Matric No.] |

---

## 🚀 Branching & Workflow

* `main`: Production-ready release branch (demo & submission).
* `develop`: Integration branch for all feature pull requests.
* `feature/role[X]-[name]`: Individual feature branches for each role.
