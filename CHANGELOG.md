<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# EmacsJ Changelog

## [Unreleased]

Note: previous versions of this plugin modified the base IntelliJ keymap, adding the plugin's bindings to all keymaps. This was incorrect
and has been removed in this release. If this causes unforeseen consequences, please file an issue on GitHub.

### Added

- New keymap "EmacsJ (macOS)" with Command key used instead of Option key for most key bindings.
- New command "Copy From Above Command". Bound to `Control-c a`.

### Fixed

- Add missing keybinding for Isearch: Toggle Case Sensitive Searching in EmacsJ keymap (`alt-c`).

## [1.5.1] - 2025-10-31

### Added

- New command *Transpose Lines* exchanges the current line with a line above it. Which line can be modified with *Universal argument*.
- Case-sensitive search can now be toggled on and off during Isearch with `alt-c`.
- *Delete All Whitespace at Point* now deletes whitespace to the left of caret after *Universal argument*.
- The Paste commands now respect numeric *Universal argument*, and will paste the kill ring element chosen with the argument, counted from
  the end. Paste after non-numeric *Universal argument* invokes *Paste: Leave Caret at Point* as before.

## [1.5.0] - 2025-09-30

### Added

- Two new XRef commands (contributor: Kyle Waldner)
    - XRef Go Forward: Go forward to the point where a previous XRef Go Back was invoked. Bound to `Control Alt COMMA`.
    - XRef Push Mark: Push the current position onto the XRef stack. (this action has no default key binding)
- New command "Cancel Repeating Action": stop the repeating action that is currently running (after Universal argument). Bound to `Control G`.
- The four zap commands now remove (or remove up to) the number of found occurrences specified by *Universal Argument*.

### Fixed

- Commands "Pop Mark" and "XRef Go Backward" (try to) set the scrolling position to the same as that of the original position.

## [1.4.3] - 2025-08-31

### Added

- Four new actions available during search/replace:
    - `,`: perform the replacement but do not move to the next match.
    - `e`: edit the replacement text.
    - `u`: undo the last replacement without exiting search/replace.
    - `^`: visit the position of the last replacement without exiting search/replace.

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

- Recenter works better at editor ends.

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

[Unreleased]: https://github.com/strindberg/emacsj/compare/v1.5.1...HEAD
[1.5.1]: https://github.com/strindberg/emacsj/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/strindberg/emacsj/compare/v1.4.3...v1.5.0
[1.4.3]: https://github.com/strindberg/emacsj/compare/v1.4.2...v1.4.3
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
