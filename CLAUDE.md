# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

EmacsJ is an IntelliJ IDEA plugin that adds Emacs-style editing commands (incremental search, kill ring, mark ring, universal argument, word/rectangle/zap commands, etc.) to IntelliJ-based IDEs. It is built with the IntelliJ Platform Gradle Plugin v2.

## Commands

```bash
./gradlew buildPlugin          # Build the distributable plugin zip
./gradlew check                # Tests + ktlint + detekt + kover verify (what CI runs)
./gradlew test                 # Run tests only
./gradlew detektMain           # detekt with type resolution, main sources
./gradlew detektTest           # detekt with type resolution, test sources
./gradlew formatKotlin         # Apply ktlint formatting
./gradlew runIde               # Sandbox IDE on the default platform (platformVersion in gradle.properties)
./gradlew runIde53             # Sandbox against IDEA 2025.3.6.1
./gradlew runIde61             # Sandbox against IDEA 2026.1.4
./gradlew runIde62             # Sandbox against IDEA 2026.2.0.1
```

Run a single test class or method:
```bash
./gradlew test --tests "com.github.strindberg.emacsj.search.ISearchTest"
./gradlew test --tests "com.github.strindberg.emacsj.search.ISearchTest.test Simple search works"
```

Linting and static analysis use **ktlint** and **detekt** (config: `detekt2.yml`). Both run as part of `check`. Code coverage uses **kover**.

**`detektMain` and `detektTest` must be green after a change.** `check` only runs the plain `detekt` task; these two additionally analyze with type resolution and report things it does not — unused imports left behind by a refactoring, for instance. Run them explicitly, since a green `check` does not imply they pass.

The default sandbox runs with `-Dide.plugins.snapshot.on.unload.fail=true`: when the plugin fails to unload dynamically, the IDE writes a heap snapshot naming whatever still holds the plugin classloader.

`instrumentCode` / `instrumentTestCode` occasionally fail with `1 >= 1` after classes are added or removed. It is stale incremental state, cleared by rerunning that task with `--rerun`.

A change to a `main` signature that the tests call can leave `compileTestKotlin` restoring a **poisoned build-cache entry**: the tests still hold the old call site and every fixture test dies with `NoSuchMethodError`, and `clean` does not help because the stale output is in the cache, not the build directory. `./gradlew compileTestKotlin --rerun` recompiles and overwrites the entry. It bites hardest on `internal` members, whose JVM names carry a `$com_github_strindberg_emacsj_emacsj` module suffix, so a stale call site fails at run time rather than at compile time.

## Code style

Beyond what ktlint and detekt enforce:

- **Private methods come after all non-private ones.** A class reads as its public surface first, with the helpers it happens to be built from below. This applies to test classes too: private helpers such as `pressEscape` or `setText` belong at the bottom, after every test method.
- **Comments and KDoc are written in American English** — "behavior", "canceled", "localized", "center". This matches the IntelliJ Platform API the code sits on (`isCanceled`, `EditorColorsScheme`).

## Architecture

### Two-layer structure

Every feature follows the same split:

- `src/main/kotlin/.../actions/<feature>/` — thin `EditorAction` subclasses. Each constructs a handler and passes it to `EditorAction`. No logic lives here.
- `src/main/kotlin/.../emacsj/<feature>/` — the handler implementing `EditorActionHandler` (or a plain class). All logic lives here.

### Action history

`EmacsJService` is an application-level `@Service` holding global state:

- **Action history** (`lastActionIds`) — the two most recent things the user did, used by handlers that behave differently when repeated (recenter/reposition cycling, append-next-kill, paste history).
- **Universal argument** — the numeric prefix argument, default 1.
- **Repeat flag**, and a **single-action registry** of action ids that ignore the universal argument.

Two listeners feed the history, and the split matters:

- `EmacsJActionListener` (`AnActionListener`) records **action ids** in `afterActionPerformed`, gated on `result.isPerformed`. All matching is by id, never by display text — action texts are localized, so text matching silently stops working in a translated IDE.
- `EmacsJCommandListener` (`CommandListener`) records command **names**, but only for commands *not* raised by an action: typing, mouse edits, commands started in code. Editor actions raise a command as well, so `isPerformingAction()` stops those being recorded twice. A command name can never equal an action id, so these entries simply read as "something unrelated happened" — which is exactly what has to break a repeat chain.

Neither listener sees everything on its own. Only `EditorAction` wraps execution in a command, so plain `AnAction`s (`NextSplitter`, most of `ide.actions`) produce no command; and typing while an interactive command is active is consumed by the raw typed-action handler, so it produces neither an action nor a command. Delegates that depend on "nothing happened since" must therefore invalidate their own state too — see `pastedText` in `ISearchDelegate`.

### Interactive-mode delegates

Multi-keystroke features use a **delegate**: a stateful object held in a `companion object` field (`ISearchHandler.delegate` and friends). While it is non-null, `EmacsJActionsPromoter` moves that feature's actions to the front of the action list so they receive keystrokes ahead of built-in ones, and the raw typed-action handler in `EmacsJTypedActionService` routes typed characters into the delegate instead of the document.

- `ISearchDelegate` — one incremental search session: caret positions, match highlights, direction, search type, breadcrumb history for backspace, and the clipboard-history walk behind isearch paste.
- `ReplaceDelegate` — a query-replace session.
- `UniversalArgumentDelegate` — accumulates digits, then repeats the following command in batches so a long repeat stays interruptible.
- `ZapDelegate` — waits for the target character.
- `GotoLineDelegate` — reads a `line[:column]`.

All of them implement `UIDelegate : Disposable` and call `EditorUtil.disposeWithEditor(editor, this)` in `init`. That parenting is what guarantees teardown when the editor or project closes mid-session; `hide()` is only the user-initiated route into the same `dispose()`.

`CommonUI` is the shared popup: a title, the text (a `JLabel` while read-only, a `LanguageTextField` while editable) and a match counter. Its `showText(found, notFound)` renders the not-yet-matching tail of a failing search in red, so the read-only text is held in a backing field rather than read back off the label.

### Mark ring

`MarkHandler` keeps a per-file stack (`places: Map<String, LimitedStack<PlaceInfo>>`, keyed by a virtual-file signature) of saved positions, separate from IntelliJ's own navigation history. The isearch handler always pushes a mark before starting a search. `XRefHandler` keeps an equivalent undo/redo stack for declaration navigation, fed from `EmacsJActionListener` when a `GotoDeclaration*` action runs.

### Settings

User-configurable preferences (lax isearch whitespace, selection-based isearch start, custom whitespace regexp) live in `EmacsJSettings` / `EmacsJState`, persisted via `PersistentStateComponent`.

### Services

Every service is declared by `@Service` on the class, never in `plugin.xml`, and reached through an `instance` accessor on its companion (project-level `MarkPlaces` and `XRefPlaces` are reached with `service<T>()` at the call site instead). Keep it that way: `@Service` keys the service on the class, while a `plugin.xml` `<applicationService serviceInterface=... serviceImplementation=.../>` keys it on the interface, and a class carrying both is registered **twice** — two instances, each with its own state, handed out depending on which type the caller asks for. `EmacsJService` had exactly that split, which is why it is now one class rather than an interface and an impl.

### Dynamic plugin unloading

The plugin is expected to unload without an IDE restart, which constrains anything outliving a keystroke:

- **No raw threads, and no executors either.** `kotlin.concurrent.thread { }` creates a `Thread` whose lambda pins the plugin classloader for as long as anything holds that `Thread` object — and the platform does hold them. All asynchronous work is coroutines; there is no longer any `AppExecutorUtil` or `ScheduledFuture` in the plugin.
- **Coroutine scopes come from the platform**, so that unloading cancels them; a scope the plugin constructs itself pins the classloader instead. `CommonHighlighter` is a `@Service` with an injected `CoroutineScope`. Anything without a service of its own — `CommonUI.flashText`, `CopyRegionHandler`'s highlight — uses `EmacsJScope`.
- **Hand work back with `withContext(Dispatchers.EDT)`, not `invokeLater`.** With `invokeLater` the coroutine completes as soon as it has *queued* the work, so its `Job` stops meaning "this result is still wanted": cancelling an already completed job is a no-op and superseded work paints anyway. Keeping the EDT half inside the coroutine is what makes `job.cancel()` sufficient, and it is why neither `CommonHighlighter` nor `flashText` needs a generation counter or a cancellation token.
- **Anything registered with the platform needs a parent disposable**: `IdeEventQueue` dispatchers, caret listeners, and so on. Delegates pass `this`.
- **Swing listeners on long-lived components must be removed from the object they were added to.** `PopupBoundsListener` captures `editor.component` once for exactly this reason: re-deriving it at removal time can yield a different component, or null. It is shared by `CommonUI` and `KillRingUI`, and `detach()` is what the popups call on teardown.
- `EmacsJActionListener`'s `init` touches `EmacsJTypedActionService` solely to instantiate it. That service is a lazy `@Service` whose constructor installs the raw typed-action handler, so without the touch, typing during isearch/zap/universal-argument goes into the document. It looks like dead code; it is not.

## Testing

Tests are **JUnit 6 (Jupiter)**, configured through the JVM test suite DSL: `testing.suites.named<JvmTestSuite>("test") { useJUnitJupiter(libs.versions.jupiter) }` is the whole setup — it pulls in `junit-jupiter` and the platform launcher and puts the test task on the JUnit Platform, so there is no BOM, no explicit launcher and no `useJUnitPlatform()`. JUnit 6 aligned platform versions with Jupiter's, so `junit-platform-*` resolves to 6.x rather than 1.x. Every test method needs `@Test`. Names are backticked prose and no longer carry the `test ` prefix that JUnit 3 discovery required. Assertions come from `org.junit.jupiter.api.Assertions` — note that Jupiter puts the message **last**, the reverse of JUnit 3.

Fixture tests extend **`EmacsJTestCase`**, which no longer inherits from `BasePlatformTestCase`. It builds `myFixture` the way `BasePlatformTestCase` does — `IdeaTestFixtureFactory` → `createLightFixtureBuilder` → `createCodeInsightFixture` — so the test bodies are unchanged from the JUnit 3 era. Three things in it are load-bearing:

- `@RunInEdt(writeIntent = true)`, which subclasses inherit (the annotation is `@Inherited`). Without `writeIntent` every test that edits a document fails with "Write-unsafe context!".
- `@BeforeEach setUpFixture` / `@AfterEach tearDownFixture`. A subclass's own `@AfterEach` runs *before* the base's, which is what the old `finally { super.tearDown() }` gave.
- The teardown hides every delegate, clears the repeat flag and pushes two empty entries onto the action history — application-scoped state that would otherwise leak into the next test class. It also offers `pressKey(ui, keyCode)` for driving a delegate's popup.

`junit:junit` is `testRuntimeOnly`, and no test refers to it: the platform test framework registers `com.intellij.tests.JUnit5TestSessionListener` as a `LauncherSessionListener` via `ServiceLoader`, and that class touches `junit.framework.TestCase`, so without JUnit 4 on the runtime classpath the executor dies at startup with a `ServiceConfigurationError`. Keeping it off the compile classpath is what stops a JUnit 4 import creeping back in. The vintage engine is gone. Four pure-logic classes (`WordUtilsTest`, `UndoRedoStackTest`, `EmacsJLexerTest`, `EllipsizeTest`) have no fixture and extend nothing; `WordUtilsTest` and `EllipsizeTest` use `@ParameterizedTest` with `@MethodSource`, whose factory methods **must be public** — Kotlin mangles `internal` names and Jupiter's reflection lookup then fails.

The typical pattern:

```kotlin
myFixture.configureByText("file.txt", "<caret>foo bar")
myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
myFixture.type("bar")
myFixture.checkResult("foo <caret>bar")
```

`<caret>` marks the caret position and `<selection>...</selection>` a selection; several `<caret>` markers give multiple carets. Internal state read by tests is marked `@VisibleForTesting`.

**Add new tests last** among the test methods of an existing class — appended after the final test, and still above the private helpers. Do not insert them next to thematically related tests; the reading order of a test class is the order it grew in.

### Test seams

Four pieces of production state exist so tests can be deterministic, rather than to switch behavior off:

- `CommonHighlighter.delay` — the debounce before a search highlights. `ISearchTest` and `ReplaceTest` set it to 0 in `@BeforeEach` and restore it, otherwise the suite pays the delay on every keystroke; they wait on `CommonHighlighter.isIdle`.
- `CopyRegionHandler.clock` — the key-repeat throttle reads it, so `AppendKillTest` can move time explicitly and test the throttle instead of disabling it.
- `MarkPlaces.clear()` and `UndoRedoStack.clear()` — the mark ring and the xref history are project-scoped, but the light fixture hands the same project to every test in a class, so both carry over. `EmacsJTestCase` empties them in teardown. Without this, a test that assumes an empty history passes or fails depending on where it lands in Jupiter's method order.

Some things cannot be asserted headlessly. Popups are suppressed in unit-test mode, so anything depending on a popup being genuinely on screen (its position, whether it follows a window resize) needs `runIde`. Clipboard history is application-scoped and is polluted by other test classes, so a test that walks it must pin it first.
