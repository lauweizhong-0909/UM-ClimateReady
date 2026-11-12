# UM ClimateReady --- AI-Native Engineering Plan & Development Guide (V2)

**Course:** WIA2007 Mobile Application Development (Occurence 3)  
**Academic Year:** 2026/2027 Semester 1 (Weightage: 20%)  
**Theme:** Mobile Application and SDGs --- Making Technology Purposeful for a Better Future  
**SDG Focus:** SDG 13 --- Climate Action, Target 13.3 (Climate Awareness & Education)  
**Target Community:** Universiti Malaya (UM) Students, Staff, and Campus Residents  
**Team Roster:** 6 Members (Minimum contribution: 20 hours/member; 24 planned hours/member; 144 team-hours)  
**Repository:** [lauweizhong-0909/UM-ClimateReady](https://github.com/lauweizhong-0909/UM-ClimateReady)  
**AI Ethics Policy:** Universiti Malaya Level 2 (Restricted AI Usage --- Mandatory `AI_USAGE_LOG.md`)  
**Development Baseline:** V2 AI-Native Sprint Edition (Pre-loaded JSON Asset Seeds & Role-by-Role AI Prompts)  

---

# 1. Project Overview & Campus Context

UM ClimateReady is an educational and preparedness Android application engineered specifically for the Universiti Malaya campus community. It translates abstract global climate concepts into actionable campus preparedness for tropical climate phenomena.

The application contains exactly five functional modules:
1. **Climate Awareness Assessment:** Diagnostic 9-MCQ pre/post test evaluating climate resilience with deterministic scoring (0–9) and color-coded recommendations.
2. **Climate Micro-Learning:** 12 bite-sized offline units (4 Rain, 4 Heat, 4 Haze) with real-world Malaysian context, UM campus tips, and CCNA-style instant verification checks.
3. **AI Scenario Quiz:** Dynamic 3-scenario decision challenges grounded in realistic campus geography, backed by a 60-question offline seed bank and a strict server/local LLM question-generation pipeline.
4. **Global Warming Explorer:** Interactive 6-step physics walkthrough of the greenhouse effect and structured comparisons of +1.5°C, +2°C, and +3°C warming scenarios.
5. **UM Climate Resources:** Curated offline directory of 12 verified campus facilities (BKU, JPPHB, PKU UM, UMSDC) and national statutory agencies (APIMS, MET Malaysia, NADMA).

### 1.1 Validated UM Campus Problem Context
Field research, stakeholder outreach (UMSDC Collaboration Letter), and campus surveys have identified primary climate pain points:
* **Heavy Rain & Waterlogging:** Low-lying campus access choke points (PJ Gate, KL Gate, Examination Building perimeter) frequently experience rapid stormwater runoff accumulation during afternoon convective storms (3:30 PM – 6:00 PM), disrupting shuttle bus schedules and pedestrian mobility.
* **Extreme Heat Stress:** Prolonged walking transit across unsheltered asphalt ring roads between residential colleges (KK2, KK10, KK12) and distant faculties under high heat index (>41°C feels-like) causes acute fatigue.
* **Transboundary Smoke Haze:** Southwest Monsoon wind regimes transport particulate aerosols (PM2.5), causing API spikes (>150 Unhealthy) requiring outdoor athletic cancellations and respiratory precautions.

### 1.2 Fixed Engineering Decisions
* **Platform & Language:** 100% Native Android in **Kotlin** (Minimum SDK: `API 24 - Android 7.0`, Target SDK: `API 34/37`).
* **UI Architecture:** **Android XML Layouts + ViewBinding + Material Design 3** (strictly NO Jetpack Compose to avoid mixed-paradigm defects).
* **Architecture Pattern:** **MVVM (Model - View - ViewModel)** + Repository Pattern + Room Database + Kotlin Coroutines & Flow.
* **Module 3 AI Pipeline (Option B - Secure Client Config):**
  * Android clients store API keys securely in `local.properties` (strictly ignored by `.gitignore`).
  * Injected at build time via `BuildConfig.AI_API_KEY`.
  * HTTP client enforces an **8-second timeout**.
  * Strict Kotlin JSON validator enforces schema, 4 distinct options, valid single correct answer, linked learning unit, and official citations.
  * Instant deterministic fallback to pre-verified 60-question seed bank on timeout, offline, or malformed JSON.
* **Offline Asset Seeds:** Pre-compiled JSON datasets bundled inside `app/src/main/assets/data/`.

---

# 2. Complete Asset Seed Registry (Week 6 Seed Data)

All curriculum specifications have been pre-extracted from peer-reviewed design documents into clean JSON assets:

| Asset File Path | Source Specification | Record Count | Description |
| :--- | :--- | :--- | :--- |
| `app/src/main/assets/data/m1_assessment_questions.json` | Module 1 Design Doc | 18 MCQs | 9 Pre-assessment + 9 Post-assessment questions across Rain, Heat, and Haze |
| `app/src/main/assets/data/m2_microlearning_units.json` | Module 2 Curriculum Doc | 12 Units | 12 complete units with summaries, examples, UM tips, CCNA pop quizzes, and takeaways |
| `app/src/main/assets/data/m3_scenario_questions.json` | Module 3 Question Bank | 60 Scenarios | 20 Rain, 20 Heat, 20 Haze (including 15 campus-specific items) with rationales |
| `app/src/main/assets/data/m5_climate_resources.json` | Module 5 Directory Doc | 12 Resources | 12 verified campus facilities, national agencies, hotlines, URLs, and descriptions |

### 2.1 Verified Resource Register (12 Seed Records)
- **UM Security Division (Bahagian Keselamatan UM - BKU)** (`RES_RAIN_01`) - Category: `HEAVY_RAIN`
  - Org: Universiti Malaya (BKU) | Hotline: `+603-7967 7070`
  - URL: https://security.um.edu.my
  - Description: Primary 24/7 campus emergency operations control center. Dispatches officers to blocked access gates (PJ Gate / KL Gate), manages storm traffic diversions, and assists stranded students during heavy downpours.
  - Verified: `2026-11-01` (VERIFIED)
- **UM Department of Development & Estate Maintenance (JPPHB)** (`RES_RAIN_02`) - Category: `HEAVY_RAIN`
  - Org: Universiti Malaya (JPPHB) | Hotline: `+603-7967 3254`
  - URL: https://jpphb.um.edu.my
  - Description: Handles physical estate infrastructure, drain unclogging, retention basin maintenance, flood barrier deployments, and electrical substation safety inspections across faculty basements.
  - Verified: `2026-11-01` (VERIFIED)
- **NADMA Malaysia (National Disaster Management Agency)** (`RES_RAIN_03`) - Category: `HEAVY_RAIN`
  - Org: Prime Minister's Department, Malaysia | Hotline: `+603-8870 4848`
  - URL: https://www.nadma.gov.my
  - Description: National portal providing real-time flood situation maps, active temporary evacuation center (PPS) statuses via InfoBencanaJKM, and standard civil defense flood safety guidelines.
  - Verified: `2026-11-01` (VERIFIED)
- **Universiti Malaya Student Health Clinic (PKU UM)** (`RES_HEAT_01`) - Category: `EXTREME_HEAT`
  - Org: Pusat Kesihatan Universiti (PKU UM) | Hotline: `+603-7967 6445`
  - URL: https://pku.um.edu.my
  - Description: On-campus medical triage, acute fluid resuscitation, and emergency care for students experiencing heat exhaustion, severe dehydration, or clinical hyperthermia.
  - Verified: `2026-11-01` (VERIFIED)
- **MET Malaysia Heatwave Monitoring System** (`RES_HEAT_02`) - Category: `EXTREME_HEAT`
  - Org: Malaysian Meteorological Department | Hotline: `1-300-22-1638`
  - URL: https://www.met.gov.my
  - Description: Official daily nationwide heatwave monitoring portal tracking consecutive days above 35°C (Yellow Alert: 35–37°C for 3 days; Orange Alert: 37–40°C; Red Alert: >40°C).
  - Verified: `2026-11-01` (VERIFIED)
- **Ministry of Health Malaysia (MOH) Public Heat Advisories** (`RES_HEAT_03`) - Category: `EXTREME_HEAT`
  - Org: Kementerian Kesihatan Malaysia (KKM) | Hotline: `+603-8883 3888`
  - URL: https://www.moh.gov.my
  - Description: Statutory clinical health guidelines on identifying heat exhaustion versus life-threatening heat stroke, immediate bystander first aid, and rehydration best practices.
  - Verified: `2026-11-01` (VERIFIED)
- **Department of Environment APIMS Portal** (`RES_HAZE_01`) - Category: `HAZE_AIR_QUALITY`
  - Org: Department of Environment (JAS Malaysia) | Hotline: `+603-8889 1972`
  - URL: https://apims.doe.gov.my
  - Description: Official real-time hourly Air Pollutant Index Management System (APIMS) reporting continuous PM2.5 readings for Petaling Jaya (nearest station to UM campus) and Cheras.
  - Verified: `2026-11-01` (VERIFIED)
- **ASEAN Specialised Meteorological Centre (ASMC)** (`RES_HAZE_02`) - Category: `HAZE_AIR_QUALITY`
  - Org: ASMC / ASEAN Secretariat | Hotline: `N/A`
  - URL: https://asmc.asean.org
  - Description: Regional satellite monitoring agency tracking transboundary smoke haze trajectories, satellite fire hotspot counts, and prevailing Southwest Monsoon wind directions across Southeast Asia.
  - Verified: `2026-11-01` (VERIFIED)
- **UM Occupational Safety & Health Section (OSH UM)** (`RES_HAZE_03`) - Category: `HAZE_AIR_QUALITY`
  - Org: Universiti Malaya (OSH / BKU) | Hotline: `+603-7967 7948`
  - URL: https://osh.um.edu.my
  - Description: Oversees university-wide workplace safety guidelines, indoor air quality filtration standards in auditoriums and lecture theatres, and personal protective equipment (PPE/respirator) advisories.
  - Verified: `2026-11-01` (VERIFIED)
- **Universiti Malaya Sustainability Development Centre (UMSDC)** (`RES_CLIMATE_01`) - Category: `CLIMATE_CHANGE`
  - Org: Universiti Malaya (UMSDC) | Hotline: `+603-7967 7990`
  - URL: https://sustainability.um.edu.my
  - Description: Central institutional hub driving UM's Eco-Campus master plan, coordinating campus carbon tracking, Rimba Ilmu ecological conservation, and university-wide SDG 13 initiatives.
  - Verified: `2026-11-01` (VERIFIED)
- **Ministry of Natural Resources & Environmental Sustainability (NRES)** (`RES_CLIMATE_02`) - Category: `CLIMATE_CHANGE`
  - Org: NRES Malaysia | Hotline: `+603-8000 8000`
  - URL: https://www.nres.gov.my
  - Description: Lead federal ministry overseeing Malaysia's National Climate Change Policy, greenhouse gas inventory reports, carbon commitments under the Paris Agreement, and national adaptation planning.
  - Verified: `2026-11-01` (VERIFIED)
- **United Nations SDG 13 Knowledge Platform** (`RES_CLIMATE_03`) - Category: `CLIMATE_CHANGE`
  - Org: United Nations Department of Economic and Social Affairs | Hotline: `N/A`
  - URL: https://sdgs.un.org/goals/goal13
  - Description: Global repository providing official target indicators, progress metrics, and educational documentation for Sustainable Development Goal 13: Climate Action (Target 13.3).
  - Verified: `2026-11-01` (VERIFIED)

---

# 3. Android Architecture & Package Layout

```text
com.um.climateready/
├── app/
│   ├── ClimateReadyApp.kt           # Application class & Room DB singleton
│   └── MainActivity.kt              # BottomNavigationView host & navigation router
├── core/
│   ├── database/
│   │   ├── AppDatabase.kt           # Room database definition with AssetSeeder callback
│   │   ├── dao/                     # AssessmentDao, LearningDao, QuizDao, ResourceDao
│   │   └── entity/                  # Room Entities mapped to asset JSON schemas
│   ├── network/
│   │   ├── AiQuestionClient.kt      # OkHttp / Retrofit client with 8-second timeout
│   │   ├── AiJsonValidator.kt       # Strict JSON schema validator
│   │   └── EnvironmentApiAdapter.kt # Weather / APIMS data normalization
│   └── util/
│       ├── AssetJsonReader.kt       # Helper to parse assets/data/*.json
│       ├── ScoringEngine.kt         # Pure deterministic scoring functions (0-3, 0-9)
│       └── ResultColorMapper.kt     # Red (0-1), Yellow (2), Green (3) color mapper
├── feature/
│   ├── shell/                       # Home Dashboard, Progress Cards, Status Banners
│   ├── assessment/                  # Module 1: Pre/Post quiz screens, Recommendation UI
│   ├── learning/                    # Module 2: Category lists, Unit detail, CCNA pop quiz
│   ├── quiz/                        # Module 3: Scenario cards, 3-question session, Rationale
│   ├── explorer/                    # Module 4: 6-step physics walkthrough, Scenario comparison
│   └── resources/                   # Module 5: Resource RecyclerView, Search, Category filter chips
└── data/
    └── repository/                  # AssessmentRepository, LearningRepository, QuizRepository, ResourceRepository
```

---

# 4. Six-Role Sprint Execution Roadmap (Week 6 to Week 10)

```
Week 6 (09-15 Nov): Setup & Assets Kickoff (Lau Wei Zhong)
Week 7 (16-22 Nov): Core Dev Sprint 1 (Foundations & Offline Schemas) [All 6 Active]
Week 8 (23-29 Nov): Core Dev Sprint 2 (Logic, UI & AI Pipeline) [All 6 Active]
Mid-Sem (30 Nov - 06 Dec): Sprint 3 & Stabilization (Integration & Merge to develop) [All 6 Active]
Week 9 (07-13 Dec): System Testing & NFR Verification [All 6 Active]
Week 10 (14-20 Dec): Final Video Recording & Technical Report [All 6 Active]
```

### Role Allocations & Ownership:
* **Role 1 (Leader & Shell Lead - Lau Wei Zhong):** Navigation Shell, BottomNavigation tabs, shared styles, Base Room DB setup, Home Dashboard, progress calculation, live/cached status banners, code integration.
* **Role 2 (Assessment Lead - Module 1):** 9-MCQ pre-assessment flow, ViewBinding UI, attempt persistence, deterministic scoring (0-3 cat, 0-9 total), Red/Yellow/Green mapping, post-test unlock logic (>=9 units, >=2/cat), pre/post comparison.
* **Role 3 (Micro-Learning Lead - Module 2):** Lesson card RecyclerView, category filter chips, JSON parser for 12 units, unit completion toggle, progress calculation, CCNA-style instant verification pop quiz, offline layouts.
* **Role 4 (AI Quiz Lead - Module 3):** 60-question offline seed bank Room entities, 3-question session flow, Gemini API client integration, 8-second timeout, strict JSON validator, deterministic fallbacks.
* **Role 5 (Backend/APIs & Triggers Lead):** Weather & APIMS API adapters (or simulated environmental provider), timestamps & freshness cache, BuildConfig secure key injection from `local.properties`, trigger engine, notification preferences.
* **Role 6 (Explorer & Directory Lead - Modules 4 & 5):** M4 6-step greenhouse walkthrough UI, scenario comparison matrix (+1.5°C, +2°C, +3°C), M5 12 verified resources parser & RecyclerView, combined search + category chip filter, explicit phone dialer/browser intents.

---

# 5. Dual-Mode AI Agent Engineering Playbook

This section contains copy-pasteable, highly constrained work orders designed for **Antigravity CLI (Autonomous Mode)** and **Android Studio Gemini / ChatGPT (Interactive Prompt Mode)**.

> [!IMPORTANT]
> **Universal Engineering Constraints for All AI Prompts:**
> 1. Always use **Kotlin** with **XML Layouts & ViewBinding**. NEVER use Jetpack Compose.
> 2. Minimum SDK is **API 24**. Do not use APIs that require API 26+ without backward compatibility checks.
> 3. Never hard-code API keys in source code. Inject via `BuildConfig.AI_API_KEY`.
> 4. Keep scoring logic in pure, testable Kotlin functions.
> 5. Never modify files outside your assigned module scope without explicit permission.

---

## 5.1 Master System Prompt (Set Once in AI Agent)

```text
You are the senior Android engineer building UM ClimateReady for Universiti Malaya (WIA2007).
PROJECT CONSTRAINTS:
- Language: 100% Kotlin.
- UI Framework: XML Layouts + ViewBinding + Material Design 3. Strictly NO Jetpack Compose.
- Minimum SDK: 24 (Android 7.0) | Target SDK: 34.
- Architecture: MVVM + Repository Pattern + Room Database + Coroutines & Flow.
- Offline Seed Assets: Read pre-compiled data from `app/src/main/assets/data/*.json`.
- Deterministic Math: Module 1 score is 0-3 per category, 0-9 total. Feedback bands: 0-1 Red, 2 Yellow, 3 Green. Post-test unlock: >=9 total units AND >=2 in each of 3 categories.
- Module 3 AI Rules: 8-second timeout. Strict JSON validation. Instant fallback to offline seed pool on failure. Never score quizzes with AI.
- Git & Code Integrity: Inspect repo first. Modify only assigned module files. Ensure `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` pass before reporting done.
```

---

## 5.2 Role 1 Work Order: App Shell & Room Database (Lau Wei Zhong)

### Mode A: Antigravity Autonomous Agent Execution
```powershell
# Branch workflow
git checkout develop
git checkout -b feature/role1-app-shell
```
**Antigravity Task Prompt:**
```text
Task: Implement Role 1 Foundations (App Shell, BottomNavigation, Room Database & Asset Seeding).
Scope:
1. Inspect `app/src/main/res/layout/activity_main.xml` and configure a BottomNavigationView with 5 menu tabs:
   - Home (`navigation_home`)
   - Assessment (`navigation_assessment`)
   - Learning (`navigation_learning`)
   - Quiz (`navigation_quiz`)
   - Directory (`navigation_directory`)
2. Implement `MainActivity.kt` with Navigation component or Fragment transaction router to switch fragments.
3. Configure Room Database in `core/database/AppDatabase.kt` with Room entities for:
   - `AssessmentQuestionEntity` (from `m1_assessment_questions.json`)
   - `LearningUnitEntity` (from `m2_microlearning_units.json`)
   - `ScenarioQuestionEntity` (from `m3_scenario_questions.json`)
   - `ClimateResourceEntity` (from `m5_climate_resources.json`)
4. Create an `AssetSeeder.kt` callback using `RoomDatabase.Callback` to parse the 4 JSON files in `app/src/main/assets/data/` upon first database creation.
5. Build Home Dashboard fragment in `feature/shell/HomeFragment.kt` displaying:
   - Welcome banner with UM ClimateReady branding.
   - Overall learning progress card (e.g., "0 / 12 units completed").
   - Climate Awareness baseline card ("Not assessed yet" or score badge).
   - Environmental status banner with LIVE / CACHED / UNAVAILABLE badge.
Verification:
- Run `./gradlew assembleDebug` to ensure compilation passes without errors.
- Run `./gradlew testDebugUnitTest` to verify Room schema and asset parsing.
```

### Mode B: Android Studio Copyable Prompt
```text
Role 1 Assignment: App Shell, BottomNavigationView, and Room Database Setup.
Please write the Kotlin code and XML layouts for:
1. `activity_main.xml`: CoordinatorLayout + FragmentContainerView + BottomNavigationView with 5 items (Home, Assessment, Learn, Quiz, Resources).
2. `AppDatabase.kt`: Abstract Room database with pre-population callback that reads `app/src/main/assets/data/*.json` and seeds tables on first run.
3. `HomeFragment.kt` & `fragment_home.xml`: Dashboard showing learning progress progressbar, assessment score pill, and environmental status card using MaterialCardView and ViewBinding.
Ensure all code uses Kotlin, ViewBinding, and Material Components. No Compose.
```

---

## 5.3 Role 2 Work Order: Module 1 Climate Awareness Assessment

### Mode A: Antigravity Autonomous Agent Execution
```powershell
git checkout develop
git checkout -b feature/role2-assessment-module
```
**Antigravity Task Prompt:**
```text
Task: Implement Module 1 Climate Awareness Assessment.
Scope:
1. Load 9 Pre-assessment MCQs from Room (`AssessmentQuestionEntity` where type == 'PRE') using `AssessmentRepository`.
2. Implement `AssessmentQuizActivity.kt` / Fragment:
   - One question at a time with smooth view transition.
   - MaterialRadioButton or MaterialCardView option selectors (A, B, C, D).
   - Progress indicator (e.g. "Question 3 of 9").
3. Implement `ScoringEngine.kt` (Pure Kotlin functions):
   - Category score: 0 to 3 for HEAVY_RAIN, EXTREME_HEAT, HAZE_AIR_QUALITY.
   - Total score: 0 to 9.
   - Color band mapping: 0–1 Red ("Needs Urgent Review"), 2 Yellow ("Basic Understanding"), 3 Green ("Sound Awareness").
   - Recommendation rule: Return first incomplete unit ID in the lowest-scoring category. In case of tie, prioritize HEAVY_RAIN > EXTREME_HEAT > HAZE_AIR_QUALITY.
4. Implement `AssessmentResultFragment.kt`:
   - Visual score summary with 3 category score cards and colored badges.
   - Targeted learning unit recommendation card with "Start Lesson" navigation intent.
5. Implement Post-Test Unlock Logic:
   - Query `LearningRepository`: Check if total completed units >= 9 AND count(completed RAIN) >= 2 AND count(completed HEAT) >= 2 AND count(completed HAZE) >= 2.
   - If unlocked, allow taking 9 Post-assessment questions (`type == 'POST'`) and display side-by-side Pre vs Post score gain comparison.
Verification:
- Add comprehensive JUnit unit tests in `app/src/test/java/com/um/climateready/ScoringEngineTest.kt` testing all score boundary conditions and unlock thresholds.
- Execute: `./gradlew testDebugUnitTest --tests "*ScoringEngineTest*"`
```

### Mode B: Android Studio Copyable Prompt
```text
Role 2 Assignment: Module 1 Assessment Scoring & UI.
Please generate Kotlin code for:
1. `ScoringEngine.kt`:
   - `calculateScores(answers: Map<String, String>, questions: List<AssessmentQuestion>): AssessmentResult`
   - Maps category scores (0-3) to Red/Yellow/Green.
   - Determines the recommended learning unit based on lowest category score.
   - `isPostTestUnlocked(completedUnits: List<LearningUnitProgress>): Boolean` checking >=9 total and >=2 per category.
2. `AssessmentViewModel.kt`: LiveData/StateFlow managing question index, selected option, and persistent result storage in Room.
3. Unit test class covering score boundary tests (all correct = 9, all wrong = 0, tied lowest category recommendation).
Use Kotlin and ViewBinding. Do not use Compose.
```

---

## 5.4 Role 3 Work Order: Module 2 Climate Micro-Learning Curriculum

### Mode A: Antigravity Autonomous Agent Execution
```powershell
git checkout develop
git checkout -b feature/role3-microlearning-module
```
**Antigravity Task Prompt:**
```text
Task: Implement Module 2 Climate Micro-Learning.
Scope:
1. Read the 12 learning units from Room / `m2_microlearning_units.json` in `LearningRepository`.
2. Implement `LearningCategoryFragment.kt` & `fragment_learning_category.xml`:
   - Category selection filter chips: "All", "Heavy Rain & Flooding (4)", "Extreme Heat (4)", "Haze & Air Quality (4)".
   - RecyclerView of `LearningUnit` cards displaying unit title, order, category badge, and completion checkmark.
3. Implement `LessonDetailActivity.kt` & `activity_lesson_detail.xml`:
   - Summary text (80-120 words).
   - Real-World Malaysian Example card.
   - UM Student Action Tip card.
   - CCNA-style Knowledge Check card: Question with 4 interactive options, instant color feedback (Green correct, Red incorrect), and verified explanation reveal.
   - Key Takeaway banner.
   - "Mark as Completed" / "Undo" toggle button that persists progress in Room.
4. Add offline support and test font scaling / screen rotation state preservation.
Verification:
- Run `./gradlew assembleDebug`
- Add unit test verifying that toggling unit completion updates overall completed count in `LearningRepository`.
```

### Mode B: Android Studio Copyable Prompt
```text
Role 3 Assignment: Module 2 Micro-Learning UI and CCNA Pop Quiz.
Please write:
1. `LessonAdapter.kt` and `item_lesson_card.xml`: MaterialCardView showing unit number, title, category tag, and completion indicator.
2. `LessonDetailActivity.kt`: ScrollView layout containing summary, real-world example, UM student tip, interactive CCNA knowledge check card, and completion switch.
3. Interactive logic: When student taps an option in the knowledge check, immediately highlight the correct answer in green, wrong answer in red, and display the explanation box.
Ensure Kotlin + ViewBinding.
```

---

## 5.5 Role 4 Work Order: Module 3 AI Scenario Quiz & JSON Validation Pipeline

### Mode A: Antigravity Autonomous Agent Execution
```powershell
git checkout develop
git checkout -b feature/role4-ai-quiz-pipeline
```
**Antigravity Task Prompt:**
```text
Task: Implement Module 3 AI Scenario Quiz & Validation Pipeline.
Scope:
1. Offline Seed Integration:
   - Query Room DB for 60 approved scenario questions (`m3_scenario_questions.json`).
   - Implement 3-question random session selection logic (1 per category or targeted to user's weakest category).
2. AI Generation Client (`AiQuestionClient.kt`):
   - Use OkHttp / Retrofit to call Google Gemini API (`gemini-1.5-flash` or REST endpoint).
   - Inject API key from `BuildConfig.AI_API_KEY`.
   - Configure strict **8-second socket timeout**.
   - Send System Prompt: Only generate ONE multiple-choice scenario in strict JSON format.
3. Strict JSON Schema Validation (`AiJsonValidator.kt`):
   - Validate `status == "ok"`.
   - Validate `category` in `["HEAVY_RAIN", "EXTREME_HEAT", "HAZE_AIR_QUALITY"]`.
   - Validate `scenario.length >= 20`.
   - Validate exactly 4 options with distinct IDs `["A", "B", "C", "D"]`.
   - Validate `correctOptionId` exists in options.
   - Validate `relatedLearningUnitId` matches valid units (e.g. `RAIN_01` to `RAIN_04`).
   - If invalid or exception thrown, REJECT immediately and return safe pre-verified fallback question from offline seed pool.
4. Quiz Flow UI (`ScenarioQuizActivity.kt`):
   - Display scenario narrative card (50-80 words).
   - 4 option buttons.
   - Deterministic scoring (never AI scored).
   - Post-answer explanation showing rationale and "Read Related Lesson" link to Module 2.
Verification:
- Write JUnit tests in `AiJsonValidatorTest.kt`:
  - Test valid JSON passes.
  - Test malformed JSON, missing field, 3 options, invalid correctOptionId, and timeout fallback.
- Run: `./gradlew testDebugUnitTest --tests "*AiJsonValidatorTest*"`
```

### Mode B: Android Studio Copyable Prompt
```text
Role 4 Assignment: Module 3 Scenario Quiz, Gemini Client & JSON Validator.
Please generate Kotlin code for:
1. `AiJsonValidator.kt`: Pure Kotlin validator that checks JSON structure, ensuring 4 options (A, B, C, D), correctOptionId, valid relatedLearningUnitId, and non-empty rationale.
2. `AiQuestionClient.kt`: OkHttp client with 8-second timeout, calling Gemini REST API with BuildConfig.AI_API_KEY. On any HTTP error, malformed JSON, or timeout, catch exception and return a random unattempted question from Room DB.
3. `ScenarioQuizViewModel.kt`: Manages 3-question session state, selected answer, and explanation display.
Kotlin + ViewBinding only.
```

---

## 5.6 Role 5 Work Order: Environmental Data Adapters & Triggers

### Mode A: Antigravity Autonomous Agent Execution
```powershell
git checkout develop
git checkout -b feature/role5-environmental-api
```
**Antigravity Task Prompt:**
```text
Task: Implement Role 5 Environmental Adapters & Status Engine.
Scope:
1. Implement `EnvironmentRepository.kt`:
   - Provide environmental status data class: `EnvironmentalStatus(category, reading, unit, statusBadge, locationLabel, timestamp)`.
   - Support states: `LIVE`, `CACHED`, `UNAVAILABLE`.
   - Data sources: Open-Meteo / MET Malaysia weather API & APIMS Air Quality (or robust mock adapter with live timestamp for testing).
2. Never describe regional Petaling Jaya measurements as localized UM campus measurements without labeling source.
3. Trigger Evaluation:
   - Rain alert: Rainfall > 15 mm/hr or MET Orange Warning -> Suggest Heavy Rain quiz.
   - Heat alert: Heat Index > 38°C -> Suggest Extreme Heat quiz.
   - Haze alert: API > 100 -> Suggest Haze & Air Quality quiz.
4. Notification Preferences & Rate Limiting:
   - Check user toggle in SharedPreferences.
   - Enforce maximum 1 notification per category per 24 hours.
Verification:
- Write unit tests verifying that when API returns 500 or network is disconnected, `EnvironmentRepository` returns `UNAVAILABLE` without crashing the app.
- Run: `./gradlew testDebugUnitTest --tests "*EnvironmentRepositoryTest*"`
```

### Mode B: Android Studio Copyable Prompt
```text
Role 5 Assignment: Environmental Data Adapter & Status Model.
Please create:
1. `EnvironmentalStatus.kt`: Data model containing status (LIVE, CACHED, UNAVAILABLE), temperature/rain/API value, station name ("Petaling Jaya Station"), and observed timestamp.
2. `EnvironmentRepository.kt`: Fetches weather/air quality data with caching and error handling. If offline or error, gracefully returns cached snapshot with CACHED badge, or UNAVAILABLE badge.
3. UI Helper to bind environmental status banner into Home Dashboard.
Kotlin + ViewBinding.
```

---

## 5.7 Role 6 Work Order: Modules 4 & 5 (Explorer & Resources Directory)

### Mode A: Antigravity Autonomous Agent Execution
```powershell
git checkout develop
git checkout -b feature/role6-explorer-resources
```
**Antigravity Task Prompt:**
```text
Task: Implement Module 4 (Global Warming Explorer) and Module 5 (UM Climate Resources).
Scope:
1. Module 4: Global Warming Explorer (`ExplorerFragment.kt` & `fragment_explorer.xml`):
   - 6-step greenhouse effect horizontal / vertical stepper:
     1. Solar radiation enters Earth system.
     2. Earth's surface absorbs energy.
     3. Surface emits infrared radiation.
     4. Greenhouse gases absorb and re-emit infrared radiation.
     5. Energy balance changes with higher GHG concentrations.
     6. Long-term energy imbalance drives rising global average temperature.
   - Add label to every step: "Simplified educational illustration".
   - Scenario comparison matrix: Interactive cards comparing +1.5°C, +2°C, and +3°C scenarios regarding Extreme Heat, Sea-Level Rise, Heavy Downpours, and Ecosystem Impact (sourced from IPCC AR6).
2. Module 5: UM Climate Resources (`ResourceDirectoryFragment.kt` & `fragment_resource_directory.xml`):
   - Load 12 verified resources from `m5_climate_resources.json` into RecyclerView.
   - Search bar: Real-time search filter matching name, description, and source organization.
   - Category filter chips: "All", "Heavy Rain", "Extreme Heat", "Haze & AQI", "Climate Change".
   - Search and category filters work conjunctively (AND logic).
   - "Call Hotline" button triggers `Intent.ACTION_DIAL` (or copies to clipboard if not a phone device).
   - "Visit Portal" button triggers `Intent.ACTION_VIEW` opening browser safely in external intent.
   - Empty state view when no search matches found.
Verification:
- Verify search query filter with unit test in `ResourceFilterTest.kt`.
- Run: `./gradlew testDebugUnitTest --tests "*ResourceFilterTest*"`
```

### Mode B: Android Studio Copyable Prompt
```text
Role 6 Assignment: Module 4 Explorer & Module 5 Resources Directory.
Please write:
1. `ResourceAdapter.kt` and `item_resource_card.xml`: MaterialCardView displaying resource name, verified badge ("Verified Campus Contact" / "Verified National Agency"), hotline, and action buttons ("Call", "Visit Website").
2. `ResourceDirectoryFragment.kt`: Search bar + Filter chips + RecyclerView with combined filtering and empty state handling.
3. `ExplorerFragment.kt`: 6-step greenhouse illustration cards and +1.5°C / +2°C / +3°C scenario comparison tabs.
Kotlin + ViewBinding.
```

---

# 6. Git Branching, Pull Requests & Grading Evidence

### 6.1 Team Git Rules (Strictly Graded by Lecturer)
* **Never commit directly to `main` or `develop`.**
* Each member works on their own feature branch: `feature/role[X]-[feature-name]`.
* Before opening a Pull Request into `develop`:
  ```bash
  # 1. Pull latest develop changes
  git checkout develop
  git pull origin develop
  git checkout feature/your-branch
  git merge develop

  # 2. Verify compilation & unit tests pass
  ./gradlew assembleDebug
  ./gradlew testDebugUnitTest

  # 3. Push and create PR
  git push -u origin feature/your-branch
  ```
* Every commit message must be descriptive and follow conventional commits:
  - `feat(module1): implement 9-mcq assessment question flow`
  - `feat(module2): add ccna-style instant pop quiz check`
  - `feat(module3): integrate gemini client with 8s timeout and json validation`
  - `feat(module5): add conjunctive search and category filter for resources`
  - `test(scoring): add unit tests for score calculation and band mapping`

### 6.2 Recording in `AI_USAGE_LOG.md`
After completing any feature using AI assistance:
1. Open [AI_USAGE_LOG.md](file:///C:/Users/Lau%20Wei%20Zhong/Documents/1.%20UM/1.%20Y2S1/WIA2007%20MoDev/UM-ClimateReady/AI_USAGE_LOG.md).
2. Append your log entry detailing the AI prompt used, the generated code, and how you manually debugged, reviewed, and tested it on device.
3. Commit `AI_USAGE_LOG.md` alongside your code.
