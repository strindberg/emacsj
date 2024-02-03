# EmacsJ

A collection of useful, Emacs-inspired commands for text editing and search.

This plugin is a collection of commands adding Emacs-inspired functionality to IntelliJ-based IDEs. It is mainly intended as an
extension to IntelliJ's Emacs keymap. While the Emacs keymap in IntelliJ is already quite good, it still lacks some functionality that an
Emacs user will miss. Moreover, many commands work slightly differently in IntelliJ and Emacs. This plugin's aim is to
offer features that bridge that gap, so that switching between Emacs and IntelliJ becomes easy. Many of these commands could also be of
interest to non-Emacs users.

There are two main ways of using EmacsJ: the plugin installs a number of commands which you can either pick-and-choose from, or
activate all at once by choosing the *EmacsJ* keymap. If you want the former, study the available commands under Preferences -> Keymap ->
Plugins -> EmacsJ and activate the ones you find interesting. Alternatively, you can choose the keymap *EmacsJ* under Preferences ->
Keymap. This keymap is an extension of the standard *Emacs* keymap, and this way all the EmacsJ commands are activated. You can then
de-activate the ones you don't want to use.

The main features are:

- Incremental search modelled on Emacs' Isearch, with text and regexp search.
- Query-replace as in Emacs with text or regexps, with a very light-weight interface.
- Word commands: transpose, upper-case, lower-case, capitalize, move, delete.
- Rectangle commands: copy, open, clear, paste.
- Whitespace commands: delete space around point, delete empty lines.
- Easy access to clipboard history à la Emacs (kill ring).
- A mark history with ability to pop mark (mark ring), and exchange point and mark.
- Duplicate and comment regions and lines.
- Recenter and relocate caret.
- Zap to character.
- Go back in XRef history.

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "emacsj"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/strindberg/emacsj/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Getting Started

1. Install the plugin from the JetBrains Marketplace or through GitHub.
2. Either:
    1. Open Preferences -> Keymap and choose the commands you wish to use under Plugins -> EmacsJ. The plugin suggests key bindings, but
       these can of course be changed.
    2. Alternatively, choose the keymap *EmacsJ* in Preferences -> Keymap. This activates all the plugin commands, and can then be
       de-activated or
       changed as needed.
3. If a command doesn't seem to do anything, double check that no other command is using the same key binding.

## Features

### Isearch

Incremental search. Each letter typed is added to the search, and the nearest match is highlighted, as well as all other visible matches.
The order of the current match amongst all matches is displayed together with the total number of matches. Keep adding letters to narrow
the search.

While searching, pressing the search keybinding again jumps to the next match, if available. Once the matches are exhausted at the end of
the file, the search bar indicates that no more matches can be found. Pressing the search key again at this stage restarts the search at the
beginning of the file (or the end if reverse search is active). The direction of the search can be changed at any time with the
corresponding key.

There are four ways to start a search:

- Isearch Forward Text (`ctrl-s`). The search text is interpreted as literal text, and the search direction is forward in the editor.
- Isearch Backward Text (`ctrl-r`). The search text is interpreted as literal text, and the search direction is backward in the editor.
- Isearch Forward Regexp (`ctrl-shift-s`). The search text is interpreted as a regular expression, and the search direction is forward in
  the editor.
- Isearch Backward Regexp (`ctrl-shift-r`). The search text is interpreted as a regular expression, and the search direction is backward in
  the editor.

Directly after a search has been initiated, pressing one of the search keys again will bring up the previous text used for search. This is
the quickest way to resume a search. History is kept separate between text search and regexp search, so only previous searches by the same
type are offered. The search history does not separate forward and backward searches.

Except for the keys below, any command key will abort search and execute the command. This makes Isearch very useful as a navigation command
as well: just search for the word where you want to end up, and keep on navigating with no extra key press required.

While searching, the following commands are available:

- `ESCAPE` or `ctrl-g`: abort the search and return the caret to the point where the search started.
- `ENTER`: abort the search and leave the caret where it currently is.
- `BACKSPACE`: if several matches with the current search string have been visited, go back in the history of matches. If not, remove
  the last character from the search string.
- `ctrl-w`: add the word at point in the editor to the search string.
- `ctrl-alt-e`: add the rest of the current editor line to the search string.
- `ctrl-alt-y`: add the character at point to the search string.
- `ctrl-shift-ENTER`: add new line character to the search string.
- `alt-p`: browse backward in the list of previous searches (of the current type).
- `alt-n`: browse forward in the list of previous searches (of the current type).
- `ctrl-l`: recenter. Scroll to put the current match at the center of the screen without interrupting the search. This works both with the
  plugin's *Recenter Caret* and IntelliJ's *Scroll to Center*.

Text from the clipboard can be pasted while searching: the contents of the clipboard will be added to the current search string.

Note that the keybindings above are only active while using Isearch, i.e. they do not clash with other commands having the same key
binding outside Isearch. The keybindings `ENTER`, `ESCAPE` or `ctrl-g`, and `BACKSPACE` are non-configurable.

Isearch text uses smart case, such that the search will be performed without case sensitivity if the whole search string consists of lower
case characters, but switch to case-sensitive search if the search string contains one or more capital letters. If you need to search for a
lower case string, and not match upper case letters, use regexp search which is always case-sensitive.

Isearch works with multiple carets.

### Search/replace

Search/replace asks for a text or regexp to search for, and then the replacement to be used. When replacement starts, each match found
triggers a request for confirmation.

There are two variants:

- Search and Replace Text(`alt-s`). Both arguments are interpreted as literal text.
- Search and Replace Regexp(`shift-alt-s`). Both arguments are interpreted as regular expressions.

Once the replacement text has been given, search is performed from the location of the caret, and at each match one can choose whether
to perform the change or not. The search stops at the end of the file.

When Search/replace is invoked, the last Search/replace command (if any) is suggested in the search bar. Pressing ENTER accepts the
suggestion and initiates the search.

If a selection is active when Search/replace is started (i.e. a region is selected), the replacement is only performed within the selected
region.

When using regexp search, back references to captured groups (parts of the search string delimited by parenthesis) in the matched string can
be used in the replace string either in java style (`%1, %2, ...`) or with backslash (`\1, \2, ...`). The whole match can be referenced
by `$0` or `\&`. To replace with a literal string which could be interpreted as a back reference, use double backlash, such as `\\1`
or `\\&`.

When specifying the search text or the replace text, the following keys are active:

- `ctrl-shift-ENTER`: add new line character to the search text or replace text.
- `alt-p`: browse backward in the list of previous Search/replace (of the current type).
- `alt-n`: browse forward in the list of previous Search/replace (of the current type).

While replacing, the following keys are active:

- `.`: perform the replacement on this match and then stop.
- `y` `SPACE`: perform the replacement and move to the next match.
- `n`: do not perform the replacement and move to the next match.
- `!`: perform the replacement on this and all the following matches.

Note that the keybindings above are only active while using Search/replace, i.e. they do not clash with other commands having the same key
binding outside Search/replace. The keybindings `y`, `n`, `!`, and `.` are non-configurable.

Search/replace text uses smart case, such that the search will be performed without case sensitivity if the whole search string and the
whole replacement string consist of lower case characters, but switch to case-sensitive search if the search string or replace string
contains one or more capital letters. If you need to search and replace two lower case strings, and not match upper case letters, use regexp
Search/replace which is always case-sensitive.

### Word Movement

The plugin's word movement commands are similar to the default IntelliJ word movements, but with a slight difference in how word boundaries
and non-word characters are handled. In standard IntelliJ, *Kill to Word End* will delete characters up to the next non-word character,
whereas the corresponding command *Delete Next Word* in this plugin will delete past non-word characters and to the end of the following
proper word. The same goes for word movements such as *Next Word* and *Previous Word*.

If the IDE's setting "use camel case" is true, these commands take that into account.

The commands are:

- Move Caret to Next Word (`alt-f`). Move the caret to the end of the current word (where current word is defined as everything up
  to the next end of word characters).
- Move Caret to Previous Word (`alt-b`). Move the caret to the start of the current word (where current word is defined as everything back
  to the next end of word characters, read backwards).
- Delete Next Word (`alt-d`). Delete characters to the end of the current word (defined as above).
- Delete Previous Word (`alt-BACKSPACE`). Delete characters back to the start of the current word (defined as above).

The word movement commands work with multiple carets.

### Modify Word

The modify word commands use word boundaries in the same way as the word movement commands defined [above](#word-movement).
They will modify the characters in the current or previous word or -- if selection is active -- modify the current selection.

The Capitalize Word commands will skip over non-word characters until it finds a word to capitalize. If Capitalize Word is used with an
active selection, each word in the region is capitalized.

The commands are:

- Upper Case Word at Point or Region (`alt-u`).
- Upper Case Previous Word (`shift-alt-u`).
- Lower Case Word at Point or Region (`alt-l`).
- Lower Case Previous Word (`shift-alt-l`).
- Capitalize Word at Point or Region (`alt-c`).
- Capitalize Previous Word (`shift-alt-c`).

The modify word commands work with multiple carets.

### Transpose Words

The transpose-words commands interchange the word at point with either the following or previous word. Non-word characters are skipped over,
so that if two transposed words are separated by non-word characters, the words change place with the same delimiters between them as before
the change.

The exact boundary between the words depend on the direction of the command: when using *Transpose Current and Next Word*, the current
word's boundaries are defined as from the first character of the word, up to the start of the next word. When using *Transpose Previous and
Current Word*, the current word is instead defined as from the end of the previous word up to the current word's last character. The upshot
of this is that both commands can be used repeatedly to move the current word forwards or backwards several places.

If the selection is active, the selected region is transposed with the following or previous word, respectively.

The commands are:

- Transpose Current and Next Word (`alt-t`). Change the order of the word at point (or region) and the following word.
- Transpose Previous and Current Word (`shift-alt-t`). Change the order of the word at point (or region) and the previous word.

The transpose word commands work with multiple carets.

### Delete whitespace on current line

The delete whitespace commands reduce the number of whitespace characters around point.

The commands are:

- Delete All Whitespace Around Point (`ctrl-BACKSPACE`). Delete whitespace before and after caret.
- Leave One Space at Point (`alt-SPACE`). Reduce all whitespace around point to only one space.

The delete whitespace commands work with multiple carets.

### Delete Blank Lines

The command *Delete Blank Lines* deletes lines that are either empty or only containing whitespace. If the caret is currently
on a non-blank line, all subsequent blank lines are deleted. If the current line is blank and the surrounding lines are non-blank, the
current line is deleted. If the current line is blank and form part of a consecutive number of blank lines, the blank lines are reduced to a
single blank line.

The commands are:

- Delete Blank Lines(`ctrl-x ctrl-o`).

### Duplicate and Comment

The duplicate commands duplicate the current line or the selected region. *Duplicate Line/Region and Comment* also comments the
original region or line.

The difference between IntelliJ's standard duplicate command and the plugin command *Duplicate Region* is that the latter leaves the caret
at its original position, and doesn't move it to the end of the new copy.

*Comment DWIM (Do What I Mean)* will comment the current line if no selection is active. If the selection is active, it will comment the
selected region, using line comments if the region's start is at a line start and the end is at either a line's start or end. Otherwise,
block comment will be used. If the line or region is already commented, the command instead un-comments the line or region.

The commands are:

- Duplicate Line/Region (`ctrl-c y`).
- Duplicate Line/Region and Comment (`ctrl-c c`).
- Comment DWIM (Do What I Mean) (`alt-SEMICOLON`).

The duplicate commands work with multiple carets.

### Rectangles

A rectangle is defined as the rectangular region limited by the upper left corner and the lower right corner of the active selection. To use
these commands, first select a region and then use the proper command. No characters outside the rectangle will be affected (although
they might move left or right).

The *Rectangle: Paste* command does not require a current selection, but will paste multiple lines starting at the same column as the
current
caret on each line.

The commands are:

- Rectangle: Copy (`ctrl-x alt-c`). Copy the contents of the rectangle to clipboard.
- Rectangle: Cut (`ctrl-x alt-k`). Copy the contents of the rectangle to clipboard, and delete it from the editor. Text to the right of the
  rectangle is adjusted leftward.
- Rectangle: Open (`ctrl-x alt-o`). Create a blank rectangle by shifting all text to the right of the rectangle.
- Rectangle: Clear (`ctrl-x alt-c`). Create a blank rectangle by replacing all text within the rectangle with space.
- Rectangle: Paste (`ctrl-x alt-p`). Paste the contents of the clipboard, starting at the same column on each line. Text to the right of the
  insertion point is shifted rightward.

### Paste

The paste commands enable the use of a paste history (kill ring) where a pasted snippet of text can be replaced by previous copied texts. By
repeatedly pressing *Paste: Previous Item in Clipboard History* after use of *Paste: Leave Caret After Pasted Region* or *Paste: Leave Caret
at Point*, the pasted text is replaced by the next item in the list of previously killed texts.

All paste commands push the opposite end of the pasted region as a mark to the [mark ring](#mark-ring") (without starting a selection). In
other words, when using *Paste: Leave Caret After Pasted Region*, a mark is pushed at the beginning of the pasted region, and vice versa
for *Paste: Leave Caret at Point*.

The items offered when using *Paste: Previous Item in Clipboard History* are filtered for duplicates and blank entries.

The commands are:

- Paste: Leave Caret After Pasted Region (`ctrl-y`). This command works as standard IntelliJ *Paste*, but it sets up the paste history so
  that further invocations of *Paste: Previous Item in Clipboard History* can suggest previously killed/copied texts.
- Paste: Leave Caret at Point (`ctrl-u ctrl y`). Works as Paste, but also leaves the caret at the current point and not at the end of the
  pasted text.
- Paste: Previous Item in Clipboard History (`alt-y`). Cycle through the history of killed text, replacing the previously pasted text.

### Mark Ring

The plugin maintains a mark history (mark ring), if the plugin's *Set/Push Mark for Selection* command is used. This makes it possible to
pop previous marks and go back to these previous locations. A separate mark history is maintained for each file where it is used. The
history only contains unique positions; a new mark replaces any earlier item at the same position in the file.

Besides maintaining a mark history, another difference between the plugin's command and the standard IntelliJ set-mark command, is that the
former always starts a new selection, whereas IntelliJ's command toggles selection. In other words, if a selection is already active, using
the plugin command starts a new selection at the current point, instead of only turning off the selection.

Adding to the mark history without starting a new selection can be achieved by quickly hitting the *Set/Push Mark for Selection* command
twice.

The commands [Isearch](#isearch) and [Search/replace](#searchreplace) set the mark at the beginning of a search so that one can return to
the position where the latest search started. The command [Exchange Point and Mark](#exchange-point-and-mark) also uses the mark history, as
described below.

The commands are:

- Set/Push Mark for Selection (`ctrl-SPACE`). Set the mark (and activate sticky selection). The mark is saved to the mark history. To only
  save the mark to history without starting selection, quickly hit the key binding twice (`ctrl-SPACE ctrl-SPACE`).
- Pop Mark (`ctrl-u ctrl-SPACE`). Pop an item from the mark history and return caret to the saved position. History is maintained per file.

### Exchange Point and Mark

When a selection is active, *Exchange Point and Mark* switches the position of the selection start and the current caret. The selected
region can then be expanded in another direction. The location of the caret before the exchange is added to the mark history.

If the selection is not active when the command is used, sticky selection is activated between the current point and the last saved mark.

The commands are:

- Exchange Point and Mark (`ctrl-x ctrl-x`). Switch positions of selection start and current point. Reactivate selection from previous mark
  if no selection is active.

### Recenter/Reposition

*Recenter* and *Reposition* cycles the location of the caret between three different locations: middle, top and bottom of the currently
visible part of the editor. *Recenter* scrolls the editor contents to move the caret, whereas *Reposition* only moves the caret without
scrolling the editor.

The commands are:

- Recenter Caret (`ctrl-l`). Cycle the position of the caret between middle, top and bottom by scrolling the editor contents.
- Reposition Caret (`alt-r`). Cycle the position of the caret between middle, top and bottom without scrolling the editor contents.

### Zap to Character

After activating one of the *Zap to character* commands, type the character which should be the end of the deleted region. Everything
between the caret and the next occurrence of the given character is deleted. The deleted part is added to the kill ring. There are four
variants of this command: kill the text including or not including the given character, forward or backward respectively.

The commands are:

- Zap To Character (`alt-z`). Kill everything up to and including the given character.
- Zap Up To Character (`shift-alt-z`). Kill everything up to but not including the given character.
- Zap Back To Character (`ctrl-alt-z`). Kill everything up to and including the given character backwards.
- Zap Back Up To Character (`ctrl-shift-alt-z`). Kill everything up to but not including the given character backwards.

The *Zap to Character* commands work with multiple carets.

### Go back in XRef History

When using any of the (IntelliJ standard) commands *Go to Declaration* or *Go to Declaration or Usages*, the plugin saves the caret position
from which the jump is made, creating a stack of previous positions. This stack can be popped with the command *Go Back to Previous Position
After Jumping to Declaration*, and one can thus easily go back to previous positions.

The commands are:

- Go Back to Previous Position After Jumping to Declaration (`alt-COMMA`). Pop one item from stack of previous positions, and return caret
  to that position.

### Kill Line

*Kill Line* (`ctrl-k`) works as the standard IntelliJ command *Cut up to Line End*: it kills (and copies) the rest of the current line. If
there is only whitespace between caret and end of the line, the newline character is also killed. This plugin command expands the standard
command such that the newline character is also killed if the caret is positioned on the very first position of the line.

The commands are:

- Kill Line (`ctrl-k`). Kill and copy the rest of the current line, including the final newline character if caret is on first position of
  line or there is only whitespace between caret and line end.

The *Kill Line* command works with multiple carets.
