<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# EmacsJ Changelog

## [Unreleased]

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
