<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# EmacsJ Changelog

## [Unreleased]

## [1.4.2] - 2025-07-31

### Added

- New command "Isearch: Edit Search Term".

## [1.4.1] - 2025-05-31

### Added

- New commands "Isearch: Go to First Search Match" and "Isearch: Go to Last Search Match"
- Improve descriptions of keyboard shortcuts in Keymap settings.

### Fixed

- The zap commands no longer remove anything if the supplied character never matches.
- The zap commands now respect "Append next kill".
- "XRef Back" now separates history between projects.

## [1.4.0] - 2025-04-30

Note: This release introduces *Universal Argument*, which affects the two commands *Paste: Leave Caret at Point* and *Pop Mark*. The two
commands are no longer bound to any key since they are obsolete when used with *Universal Argument*, but they are still available as
independent commands if you for some reason prefer not to use *Universal Argument*.

### Added

- New command "Universal Argument".
- Digit-argument variants of "Universal Argument".
- New command "Append Next Kill".
- New commands "Kill Ring Copy" and "Kill Ring Cut".
- New command "Kill Whole Line".
- New commands "Beginning of buffer" and "End of buffer".

### Fixed

- Kill line at end of file no longer throws exception.
- Pop mark now works for all editor types, not only text editors.

## [1.3.6] - 2025-04-01

### Added

- Isearch lax search (replacing spaces in search text with a specified regular expression) implemented.
- Lax search can be toggled on and off during Isearch (mode persists between searches).
- Lax search regexp and default mode can be configured under Settings.

### Fixed

- Paste no longer disables multiple carets.
- Initialization error in Isearch fixed.

## [1.3.5] - 2025-03-31

### Added

- Isearch lax search (replacing spaces in search text with a specified regular expression) implemented.
- Lax search can be toggled on and off during Isearch (mode persists between searches).
- Lax search regexp and default mode can be configured under Settings.

### Fixed

- Paste no longer disables multiple carets.

## [1.3.4] - 2025-02-28

### Added

- Isearch can be finished with caret at start of final match.
- Isearch can be finished with the final match selected.

### Fixed

- Reversing search direction with empty search string no longer triggers retrieval of previous search.
- Updating plugin will less often require a restart.
- Multi-caret search can now be reversed after failed match.

## [1.3.3] - 2025-01-27

### Fixed

- Bump compatibility with future IntelliJ versions.
- Refactor location history not to use internal IntelliJ classes.
- Isearch interface now resizes/moves with IntelliJ window.
- Inactivate two Terminal-plugin key bindings clashing with this plugin.
- Migrate to new version of Gradle plugin.

## [1.3.2] - 2024-07-11

### Added

- Colors used by Isearch and Search/replace can be configured.

## [1.3.1] - 2024-02-05

### Added

- All paste commands push a mark to the opposite side of the pasted region.

### Fixed

- Run highlighting of secondary matches for "ISearch" and "Search/Replace" in separate thread.
- Fix Isearch bug where reverse search started at origin when a letter was added after previous searches.
- EmacsJ keymap: remove keybindings from keymap "$default" clashing with plugin prefix "Ctrl-U".
- Better handling of "Recenter Caret" and "Reposition Caret" near start/end of editor.
- Remove timeout for double-tapping "Set/Push Mark for Selection": tapping twice in same position pushes mark without starting selection.

## [1.3.0] - 2024-01-16

### Added

- New command: Go back in XRef history (Thank you, Eugene Bulavin).

## [1.2.2] - 2024-01-11

### Fixed

- After Isearch failed, backspace removed all failing letters, not just one per key press.

## [1.2.1] - 2024-01-09

### Fixed

- Update documentation.

## [1.2.0] - 2024-01-08

### Added

- New EmacsJ keymap.
- New commands: zap-to-character (four variants).

### Fixed

- Recenter works better att editor ends.

## [1.1.0] - 2023-12-15

### Added

- New command: comment-dwim (do what I mean).
- Recenter can be used during Isearch.
- Replace arguments: '\1' and '\&' can be escaped.
- Isearch interface font size is calculated from current editor setting.

### Fixed

- Correct cursor position after transpose word backward.
- Scroll to cursor after commands that insert a lot of text.
- NPE in MarkHandler when activated in FileDialog.

## [1.0.2] - 2023-11-28

### Added

- Improve plugin description.

## [1.0.1] - 2023-11-28

### Added

- First public release.

## [0.9.3] - 2023-11-25

### Added

- Re-implement functionality for duplicate region.
- Update key bindings.

## [0.9.2] - 2023-11-23

### Added

- Initial release.

[Unreleased]: https://github.com/strindberg/emacsj/compare/v1.4.2...HEAD
[1.4.2]: https://github.com/strindberg/emacsj/compare/v1.4.1...v1.4.2
[1.4.1]: https://github.com/strindberg/emacsj/compare/v1.4.0...v1.4.1
[1.4.0]: https://github.com/strindberg/emacsj/compare/v1.3.6...v1.4.0
[1.3.6]: https://github.com/strindberg/emacsj/compare/v1.3.5...v1.3.6
[1.3.5]: https://github.com/strindberg/emacsj/compare/v1.3.4...v1.3.5
[1.3.4]: https://github.com/strindberg/emacsj/compare/v1.3.3...v1.3.4
[1.3.3]: https://github.com/strindberg/emacsj/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/strindberg/emacsj/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/strindberg/emacsj/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/strindberg/emacsj/compare/v1.2.2...v1.3.0
[1.2.2]: https://github.com/strindberg/emacsj/compare/v1.2.1...v1.2.2
[1.2.1]: https://github.com/strindberg/emacsj/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/strindberg/emacsj/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/strindberg/emacsj/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/strindberg/emacsj/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/strindberg/emacsj/compare/v0.9.3...v1.0.1
[0.9.3]: https://github.com/strindberg/emacsj/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/strindberg/emacsj/commits/v0.9.2
