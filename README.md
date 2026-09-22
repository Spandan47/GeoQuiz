# GeoQuiz

A simple Android app (Kotlin) that quizzes the user on geography with true/false
questions.

## How to open

1. Open Android Studio → **Open** → select the `GeoQuiz` project folder.
2. Let Gradle sync (it will download the Android Gradle Plugin / Kotlin plugin
   the first time).
3. Run on an emulator or device (minSdk 21 / Android 5.0+).

## Project structure

```
app/src/main/java/com/example/geoquiz/
    Question.kt        - data class for a single true/false question
    MainActivity.kt     - quiz screen: question display, scoring, state saving
    CheatActivity.kt    - second screen, launched via explicit Intent

app/src/main/res/layout/
    activity_main.xml   - question, score, True/False, Next/Previous, Cheat! buttons
    activity_cheat.xml  - warning text + "Show Answer" button

app/src/main/res/values/
    strings.xml         - all question text and UI strings (easy to add more questions here)
```

## How each requirement is met

| Requirement | Where |
|---|---|
| True/false question with TRUE/FALSE buttons | `activity_main.xml`, `MainActivity.checkAnswer()` |
| Toast for correct/incorrect | `MainActivity.checkAnswer()` |
| 5–6+ questions, cycle with Next/Previous | `questionBank` list in `MainActivity`, `nextButton`/`prevButton` listeners |
| Running score shown after all answered | `correctCount`/`answeredCount`, `updateScoreDisplay()`, `showFinalScore()` |
| Second Activity via explicit Intent | `CheatActivity.newIntent()` + `cheatLauncher` (Activity Result API) in `MainActivity` |
| Data passed between Activities | `EXTRA_ANSWER_IS_TRUE` (Main → Cheat), `EXTRA_ANSWER_SHOWN` (Cheat → Main) |
| Survives rotation | `onSaveInstanceState()` / restore block in `MainActivity.onCreate()`, storing current index, score, answered state, and cheat state |

## Adding more questions

Add a string resource in `strings.xml` and a new `Question(...)` entry to the
`questionBank` list in `MainActivity.kt`. The `cheatedOnQuestion` array is sized
from `questionBank.size`, so it stays in sync automatically.

## Note on the Gradle wrapper

The wrapper's `gradle-wrapper.jar` binary isn't included (it can't be generated
in this environment). Android Studio will offer to regenerate it automatically
on first sync, or you can run `gradle wrapper` once yourself if you have Gradle
installed locally and want to build from the command line.

## Notes

- Uses View Binding (no `findViewById`) and the modern
  `registerForActivityResult` API instead of the deprecated
  `startActivityForResult`.
- A question that's been peeked at via the Cheat screen is still marked
  "answered" but is never counted toward the score, and shows a
  "Cheating is wrong." toast instead of correct/incorrect.
