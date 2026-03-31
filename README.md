# Workout Log (MVP)

Offline-first Android app for fast workout set logging.

## Stack

- Kotlin
- Android API 26+
- Jetpack Compose (Material 3)
- Room + migrations
- Coroutines + Flow
- MVVM + Repository
- Hilt DI
- Navigation Compose
- MPAndroidChart (via `AndroidView`)
- Rest timer: `CountDownTimer` + `AlarmManager` + notifications

## Features

- Active workout start/resume/finish
- Exercises in workout
- Sets (weight/reps/RPE/note)
- Fast set entry with auto-fill
- Swipe actions for set rows (duplicate/delete)
- Long press on set row (duplicate)
- Rest timer with presets + custom value
- History list + workout details
- Templates (create from workout, start from template)
- Stats by exercise (e1RM/tonnage, range filters, PR/last/trend)

## Project structure

- `app/src/main/java/com/workoutlog/data` - Room + repositories
- `app/src/main/java/com/workoutlog/domain` - models, repository contracts, use cases
- `app/src/main/java/com/workoutlog/ui` - screens, state, viewmodels
- `app/src/main/java/com/workoutlog/navigation` - nav graph + bottom tabs
- `app/src/main/java/com/workoutlog/timer` - timer state, alarm scheduler, notifications

## Database migration strategy

- DB version: 3
- `1 -> 2`: add `rpe`, `note` to `set_entry`
- `2 -> 3`: add `defaultRestSec` to `template_exercise`
- Instrumented migration tests included in `app/src/androidTest/.../WorkoutLogMigrationTest.kt`

## Build

Use Gradle wrapper:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
```

If wrapper is missing in your environment, generate it once (`gradle wrapper`) in this project.
