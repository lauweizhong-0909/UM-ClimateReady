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
| **Role 2** | Module 1: Climate Awareness Assessment | Yip Zheng Xyun | 25006747 |
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

### Log Entry #002: Week 7 Core Dev Sprint 1 --- App Shell & Foundation
* **Date:** 2026-11-16
* **Member / Role:** Lau Wei Zhong (Role 1: Leader & Shell Lead)
* **AI Tool Used:** Antigravity (Powered by Gemini)
* **Task Objective:** Scaffold Android app shell, persistent BottomNavigationView with 5 module tabs, Home Dashboard with progress and status banners, 5 skeleton fragments, AssetDataManager data loader, and automated seed validation unit tests.
* **Specific Prompts Inputted:**
  ```text
  start my task role 1, week 7
  Implement Role 1 Foundations: BottomNavigationView with 5 tabs, Home Dashboard, 5 module fragments, AssetDataManager for JSON asset seeds, ViewBinding, and JUnit test suite.
  ```
* **AI Output Received:**
  - ViewBinding & BuildConfig configuration in `app/build.gradle.kts`.
  - Color palette (UM navy/gold & sustainability green) in `colors.xml`.
  - 5 vector navigation drawables and `bottom_nav_menu.xml`.
  - `MainActivity.kt` with fragment transaction routing.
  - `HomeFragment.kt` & `fragment_home.xml` with dynamic progress bars and assessment score badge.
  - 4 skeleton feature fragments (`AssessmentFragment`, `LearningFragment`, `QuizFragment`, `ResourcesFragment`).
  - `AssetDataManager.kt` loading 4 pre-compiled JSON seeds from assets.
  - `AssetSeedDataTest.kt` verifying all 18 M1, 12 M2, 60 M3, and 12 M5 records.
* **Human Verification, Testing & Modification:**
  - Executed `./gradlew testDebugUnitTest`: verified that all 4 asset schema tests passed with 100% assertions satisfied.
  - Executed `./gradlew assembleDebug`: confirmed zero layout compilation errors and clean APK packaging.
  - Checked Git branch separation (`feature/role1-app-shell`), keeping `develop` and `main` protected.

---
### Log Entry #003: Week 7 Core Dev Sprint 1 — Module 5 UM Climate Resources
* **Date:** 2026-11-16
* **Member / Role:** Hon Chi Fung(Role 6: Explorer & Directory Lead)
* **AI Tool Used:** Muse
* **Task Objective:** Implement Module 5 directory: display 12 verified resources as cards, with real-time search, category chip filtering (AND logic), empty state, and Call / Visit Portal intents.
* **Specific Prompts Inputted:**
  ```text
  Guide me step-by-step to build Module 5 in Android Studio: item_resource_card layout, ResourceAdapter, connecting ResourcesFragment to AssetDataManager, ResourceFilter search/category logic, chips and empty state in fragment_resources.xml, and a ResourceFilter unit test.
  ```
* **AI Output Received:**
  - item_resource_card.xml: MaterialCardView with name, verified badge, description, organisation, hotline, Call / Visit buttons.
  - ResourceAdapter.kt: RecyclerView adapter binding ClimateResource data, ACTION_DIAL for hotlines (with N/A handling) and ACTION_VIEW for portals.
  - ResourceFilter.kt: Pure filter function matching query against name/description/organisation AND category.
  - Updated ResourcesFragment.kt and fragment_resources.xml: search TextWatcher, category ChipGroup, empty-state view.
  - ResourceFilterTest.kt: 5 JUnit tests for the filter logic.
* **Human Verification, Testing & Modification:**
  - Ran the app on Medium Phone API 37 emulator: verified all 12 cards load, Call opens the dialer and Visit opens the browser.
  - Manually tested search + chips: All = 12 cards, Heavy Rain = 3 cards, "clinic" + Heavy Rain = empty state, "clinic" + Extreme Heat = PKU UM and MOH (matched "clinical").
  - Executed testDebugUnitTest --tests "*ResourceFilterTest*": BUILD SUCCESSFUL, all tests passed.
  - Also verified Module 4 Explorer on the emulator: 6-step greenhouse walkthrough and +1.5°C/+2°C/+3°C scenario chips display correctly.

---

### Log Entry #004: Week 7–8 Core Dev Sprint — Module 1 Climate Awareness Assessment
* **Date:** 2026-11-22
* **Member / Role:** Yip Zheng Xyun (Role 2: Assessment Lead — Module 1)
* **AI Tool Used:** Antigravity (Powered by Gemini)
* **Task Objective:** Implement Module 1 Climate Awareness Assessment including pure deterministic scoring (`ScoringEngine`), Red/Yellow/Green color band mapping (`ResultColorMapper`), persisted Pre/Post attempts (`AssessmentRepository`), 4 ViewBinding UI screens (Hub, 9-MCQ Quiz, Result + Recommendation + Review, Pre vs Post Comparison), anonymous result sharing, and exhaustive 512-combination JUnit verification.
* **Specific Prompts Inputted:**
  ```text
  I am the assessment lead, built the assessments [referencing WIA2007 MADev Occ3 Google Drive folder]
  ```
* **AI Output Received:**
  - `ResultColorMapper.kt` & `ScoringEngine.kt`: Deterministic 0–3 category & 0–9 total scoring, Red (0–1) / Yellow (2) / Green (3) bands with text labels, weakest-category recommendation with `HEAVY_RAIN > EXTREME_HEAT > HAZE_AIR_QUALITY` tie-breaking, Post-Test unlock validation (`>=9` total units AND `>=2` per category), neutral Pre vs Post comparison, anonymous share text builder, and PDF artifact text sanitizer.
  - `AssessmentRepository.kt` & `AssessmentViewModel.kt`: Persists full Pre/Post attempts via `SharedPreferences` + `Gson`, syncs Pre-Assessment summary with `AssetDataManager` for Home Dashboard, and preserves quiz state across configuration changes.
  - 4 XML + ViewBinding screens (`AssessmentFragment`, `AssessmentQuizFragment`, `AssessmentResultFragment`, `AssessmentComparisonFragment`) hosted by `AssessmentActivity`, plus targeted navigation intent to Micro-Learning in `MainActivity`.
  - `ScoringEngineTest.kt`: Exhaustive 512-combination ($2^9$) scoring & color-band test, boundary tests, tie-breaking recommendation tests, Post-Test unlock threshold tests, neutral comparison & zero-PII sharing tests, and seed text sanitization tests.
* **Human Verification, Testing & Modification:**
  - Verified against Module 1 Assessment Question Bank PDF, Complete Development Plan V1 (Section 4), and AI Engineering Guide V2 (Section 5.3).
  - Executed `./gradlew testDebugUnitTest --tests "*ScoringEngineTest*"`, `./gradlew testDebugUnitTest`, and `./gradlew assembleDebug` to confirm all unit tests pass and debug APK compiles cleanly.

### Log Entry #005: Week 7 Core Dev Sprint 1 — Module 2 Climate Micro-Learning
* **Date:** 2026-11-22
* **Member / Role:** Tan Sze Yung (Role 3: Micro-Learning Lead — Module 2)
* **AI Tool Used:** Antigravity (Claude Sonnet 4.6)
* **Task Objective:** Implement Module 2 Micro-Learning full UI: `LearningRepository`, `LessonAdapter` with category filter chips, progress header LinearProgressIndicator, `LessonDetailBottomSheet` with CCNA interactive knowledge check (instant green/red colour feedback), Mark as Completed toggle with SharedPreferences persistence, and `MicroLearningTest.kt` JUnit suite (13 tests).
* **Specific Prompts Inputted:**
  ```text
  /plan I am the Micro-Learning (M2) Lead for UM-ClimateReady.
  First, read @docs/DEVELOPMENT_PLAN_AI_GUIDE.md, @README.md, and @AI_USAGE_LOG.md to check
  the exact Week 7 deliverables, architecture rules, and AI logging requirements for Module 2
  (Micro-Learning). Next, inspect: @app/src/main/assets/data/m2_microlearning_units.json,
  @app/src/main/java/com/um/climateready/core/database/AssetDataManager.kt,
  @app/src/main/java/com/um/climateready/core/model/Models.kt,
  @app/src/main/java/com/um/climateready/feature/resources/ (reference),
  @app/src/main/java/com/um/climateready/feature/learning/LearningFragment.kt,
  @app/src/main/res/layout/fragment_learning.xml.
  Create an implementation plan for Week 7 Micro-Learning feature [5 tasks listed].
  ```
  *(After plan was approved, implementation was triggered immediately.)*
* **AI Output Received:**
  - `data/repository/LearningRepository.kt`: MVVM repository wrapper with `getFilteredUnits()`, `toggleCompletion()`, `getCompletedCountByCategory()`, and `isPostTestUnlocked()`.
  - `feature/learning/LessonAdapter.kt`: RecyclerView adapter following `ResourceAdapter` pattern; binds unit number, category badge, title, snippet, and ✓ completion icon.
  - `res/layout/item_lesson_card.xml`: MaterialCardView with ConstraintLayout for unit number badge, category chip, title, summary snippet, and right-side completion tick.
  - `feature/learning/LessonDetailBottomSheet.kt`: BottomSheetDialogFragment displaying full summary, real-world example, UM student tip, 4-button CCNA knowledge check with instant green/red colour feedback, key takeaway banner, and Mark as Completed toggle.
  - `res/layout/bottom_sheet_lesson_detail.xml`: NestedScrollView full-lesson layout with all content sections.
  - Updated `feature/learning/LearningFragment.kt`: Full implementation replacing stub — LearningRepository wired, chip filter sets `selectedCategory`, `applyFilter()` feeds adapter, `updateProgressHeader()` drives LinearProgressIndicator; `openLessonDetail()` shows bottom sheet with dismiss callback.
  - Updated `res/layout/fragment_learning.xml`: Added progress header card (LinearProgressIndicator + "X/12 completed" label) above chip group.
  - `drawable/ic_check_circle.xml` and `drawable/bg_unit_number.xml`: Vector drawables used by lesson cards.
  - `test/.../MicroLearningTest.kt`: 13 pure JUnit tests (7 JSON parsing, 6 progress tracking logic).
* **Human Verification, Testing & Modification:**
  - Reviewed all generated Kotlin files for correctness against the JSON schema, `AssetDataManager` API, and `ResourceAdapter`/`ResourcesFragment` reference patterns.
  - Verified `LessonDetailBottomSheet` dismiss callback correctly refreshes `LearningFragment` adapter and progress bar.
  - Verified CCNA option buttons are disabled after first selection (no re-answering) and correct answer is always highlighted green even if wrong option is tapped.
  - Executed: `./gradlew testDebugUnitTest --tests "*MicroLearningTest*"` — verify all 13 tests pass.
  - Executed: `./gradlew assembleDebug` — verify zero compilation errors and clean APK.
  - Manually tested on emulator: chip filter, card list, bottom sheet scroll, CCNA colour feedback, Mark Complete toggle, progress bar update.

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

