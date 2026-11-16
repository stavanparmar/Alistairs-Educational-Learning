# Alistair’s Educational Learning

Offline-first learning for children on Android and iOS. Kotlin Multiplatform shares the Compose UI and learning logic across both platforms.

> **Project status:** This is a cross-platform starter scaffold, not a finished or release-tested app. It includes a playable memory-match game, shared learning logic and tests, local child profile persistence with profile editing, an Android launcher, and an Xcode iOS app target. More learning activities, progress persistence, production RAG, and a compatible on-device model runtime still need implementation and device testing.

## Product principles

- No account, login, email, or cloud profile.
- No ads, analytics, or tracking SDKs.
- No internet permission in the Android manifest.
- Child name and age (3–10) are stored locally.
- Learning difficulty is selected using the child’s age and locally stored progress.
- Games, maths, general knowledge, drawing, and colour learning are planned modules.
- The Games tile currently opens an interactive memory-match activity.
- New stages are generated locally from curated learning material and validated before being shown.
- The app must remain usable when an AI model is absent or cannot load.
- Original visual design, illustrations, activities, and question wording should be created for this project.

## First launch

The app asks for:
1. Child’s first name or nickname.
2. Age (3 through 10).

The child’s name and age are saved locally on the device, so the welcome screen is skipped on later launches. Use **Edit profile** on the learning home screen to update either value. No login is requested. A parent settings area should eventually allow progress reset, model import, sound, and reduced-motion preferences.

## Age-based learning bands

| Age | Suggested learning focus |
|---|---|
| 3–4 | Colours, shapes, picture matching, counting |
| 5–6 | Letters, number sense, simple addition, basic GK |
| 7–8 | Addition, subtraction, multiplication introduction, science and vocabulary |
| 9–10 | Multiplication, division, reasoning, broader GK and age-appropriate challenges |

Age bands are defaults, not rigid limits. Stage progression should also consider demonstrated mastery, repeated mistakes, and parent settings.

## Offline AI architecture

```text
Shared Compose UI (Android + iOS)
  ├── Local child profile + progress store
  ├── Learning modules (games / maths / GK / drawing / colours)
  └── Stage planner
        ├── Local curriculum / facts library
        ├── Retriever (RAG)
        ├── LangChain4j orchestration adapter
        └── On-device model adapter
              └── Model file imported/stored on device
```

### RAG and stage generation

1. Select the child’s age band, subject, completed stages, and learning needs.
2. Retrieve relevant approved learning segments from the local knowledge library.
3. Build a constrained prompt using the retrieved segments and child-safe output rules.
4. Ask the on-device model to propose a stage.
5. Validate age range, answer keys, question count, content safety, and duplicate questions.
6. Save the stage locally only after validation.
7. If the model is missing or fails, use pre-authored local stages.

**Important:** RAG does not guarantee factual correctness. The production app needs curated sources, answer-key validation, content moderation rules, and parent review controls. Do not let a model invent factual answer keys unchecked.

### LangChain4j and local model integration

This starter keeps the retrieval and model boundaries explicit. Integrate a LangChain4j `ChatModel`/AI service only with an adapter whose inference backend is local. Do not configure hosted providers or API keys.

The on-device model runtime is deliberately an interface at this stage. Choose and test a runtime/model pair against the target Android devices, RAM, storage, ABI, and licensing requirements before shipping. The model weights are **not included** in this ZIP.

A model usually must be downloaded or copied onto the device once before it can be used offline. After model setup, inference must remain on-device. The non-AI learning modules must work without the model.

## Build

### Requirements

- Android Studio (recommended) with Android SDK
- JDK 17
- Gradle 8.9 or a compatible Gradle installation
- Android device or emulator (min SDK 26)
- macOS with Xcode for building or signing the iOS app (iOS deployment target 15.0)

Open this folder in Android Studio and allow Gradle sync. Run the `app` configuration for Android. To build iOS, open `iosApp/iosApp.xcodeproj` on a Mac; its build phase builds and links the shared Kotlin framework.

Android command line:

```bash
./gradlew testDebugUnitTest :shared:testDebugUnitTest
./gradlew :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The first build requires access to download Android Gradle Plugin, AndroidX, and other build dependencies. **That is a build-time requirement, not an app runtime requirement.** The installed app is designed not to need internet.

## GitHub Actions

GitHub Actions builds Android and iOS on pushes, pull requests, or manual dispatch. The Android workflow runs shared logic tests and uploads a debug APK. The iOS workflow builds an unsigned iOS Simulator app on macOS and uploads it as an artifact; App Store distribution still requires signing and release configuration.

## Testing

Shared learning logic tests run with:

```bash
./gradlew :shared:testDebugUnitTest
```

Add tests for:
- Age validation and age-band selection.
- Profile persistence and editing on both platforms; progress reset.
- Stage progression and duplicate prevention.
- RAG retrieval relevance and empty-result handling.
- Prompt constraints and malformed model output.
- Offline fallback when the model is unavailable.
- Maths scoring, memory matching, GK answer validation.
- Drawing state and colour selection.
- UI layouts on compact, medium, and expanded widths.
- Rotation, font scaling, touch targets, and reduced motion.

The included unit tests cover the initial age-band and stage-planning logic only. They are not a substitute for the full test plan above.

## Privacy and permissions

The manifest intentionally declares no `INTERNET` permission. No login, telemetry, ad SDK, or cloud endpoint is configured in this starter.

Before release, inspect the merged manifest and all transitive dependencies to ensure no library introduces unexpected permissions or network behavior.

## Suggested project roadmap

1. Complete profile editing, persistence, parent controls, and local progress.
2. Build the five learning modules and original content library.
3. Implement local RAG indexing/retrieval.
4. Integrate LangChain4j with a verified local inference adapter.
5. Select, import, and benchmark a compatible on-device model.
6. Add stage validation, parent review, and offline fallback.
7. Complete UI, accessibility, device, and airplane-mode testing.
8. Produce signed release builds and document model licensing.

## License

No third-party learning content or model weights are bundled. Add a project license and verify licenses for every library, illustration, font, dataset, and model before distribution.
