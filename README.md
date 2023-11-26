# EmacsJ

<!-- Plugin description -->

A collection of very useful, Emacs-inspired commands for text editing and search.
<br/>

This plugin is a collection of commands adding a number of Emacs-inspired functionality to IntelliJ-based IDEs: some commands are
modified versions of commands that IntelliJ already has, some are commands that do not exist in IntelliJ.
<br/>

The main features are:

- Incremental search modelled on Emacs' Isearch, with text and regexp search.
- Search/replace with a very light-weight interface.
- Word commands: transpose, upper-case, lower-case, capitalize, move, delete
- Rectangle commands: copy, open, clear, paste
- Whitespace commands: delete space around point, delete empty lines
- Easy access to clipboard history à la Emacs
- A mark history with ability to pop mark, and exchange point and mark
- Recenter and relocate caret
  <br/>

Documentation: [Github](https://github.com/strindberg/emacsj)
<br/>

To use the plugin, simply install it, choose the commands you are interested in, and either use the key bindings suggested by the
plugin, or choose your own bindings in IntelliJ's preferences. If a command doesn't seem to do anything, double check that no other command
is using the same key binding. The plugin does not alter IntelliJ in any way other than add to the list of available commands.

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "emacsj"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/strindberg/emacsj/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Features

## Isearch

Incremental search. Each letter typed is added to the search, and the nearest match is highlighted, as well as all other visible matches.
The order of the current match amongst all matches is displayed together with the total number of matches. Keep adding letters to narrow
your search.

While searching, pressing the search keybinding again jumps to the next match, if available. Once the matches are exhausted at the end of
the file, the search bar indicates that no more matches can be found. Pressing the search key again at this stage restarts the search at the
beginning of the file (or the end if reverse search is active). The direction of the search can be changed at any time with the
corresponding key.

There are four ways to start a search:

- Search Forward Text (`ctrl-s`). The search text is interpreted as literal text, and the search direction is forward in the editor.
- Search Backward Text (`ctrl-r`). The search text is interpreted as literal text, and the search direction is backward in the editor.
- Search Forward Regexp (`ctrl-alt-s`). The search text is interpreted as a egular expression, and the search direction is forward in the
  editor.
- Search Backward Regexp (`ctrl-alt-r`). The search text is interpreted as a regular expression, and the search direction is backward in the
  editor.

Directly after a search has been initiated, pressing one of the search keys again will bring up the previous text used for search. This is
the quickest way to resume a search. History is kept separate between text search and regexp search, so only previous searches by the same
type are offered. The search history does not separate forward and backward searches.

Except for the keys below, any command key will abort search and execute the command. This makes Isearch very useful as a navigation command
as well: just search for the word where you want to end up, and keep on navigating with no extra key press required.

While searching, the following keys are active:

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

Text from the clipboard can be pasted while searching: the contents of the clipboard will be added to the current search string.

The keybindings `ENTER`, `ESCAPE` or `ctrl-g`, and `BACKSPACE` are non-configurable.

Isearch text uses smart case, such that the search will be performed without case sensitivity if the whole search string is lower case
characters, but switch to case-sensitive search if the search string contains one or more capital letters. If you need to search for a lower
case string, and not match upper case letters, use regexp search which is always case-sensitive.

## Search/replace

Search/replace asks for a text or regexp to search for, and then the replacement to be used. When replacement starts, each match found
triggers a request for confirmation.

There are two variants:

- Search and Replace Text(`alt-s`). Both arguments are interpreted as literal texts.
- Search and Replace Regexp(`shift-alt-s`). Both arguments are interpreted as regular expressions.

Once the replacement text has been given, search is performed from the location of the caret, and at each match you can choose whether
to perform the change or not. The search stops at the end of the file.

When Saerch/replace is invoked, the last Search/replace command (if any) is suggested in the search bar. Pressing ENTER accepts the
suggestion and initiates the search.

If a selection is active when Search/replace is started (i.e. a region is selected), the replacement is only performed within the current
selection.

When using regexp search, back references into groups in the matched string can be used in the replace string either in java style
(`%1, %2, ...`) or with backslash (`\1, \2, ...`). The whole match can be referenced by `$0` or `\&`.

While replacing, the following keys are active:

- `y` `SPACE`: perform the replacement and move to the next match.
- `n`: do not perform the replacement and move to the next match.
- `!`: perform the replacement on this and all the following matches.
- `.`: perform the replacement on this match and then stop.
- `ctrl-shift-ENTER`: add new line character to the search text or replace text.
- `alt-p`: browse backward in the list of previous Search/replace (of the current type) performed.
- `alt-n`: browse forward in the list of previous Search/replace (of the current type) performed.

The keybindings `y`, `n`, `!`, and `.` are non-configurable.

Search/replace text uses smart case, such that the search will be performed without case sensitivity if the whole search string and the
whole replacement string is lower case characters, but switch to case-sensitive search if the search string or replace string contains one
or more capital letters. If you need to search and replace two lower case strings, and not match upper case letters, use regexp
Search/replace which is always case-sensitive.

Isearch works with multiple carets.

## Word Movement

The word movement commands are similar to the default IntelliJ word movements, but with a slight difference in how word boundaries and
non-word characters are handled. In standard IntelliJ, "kill to word end" will delete characters up to the next non-word character, whereas
the corresponding command "Delete next word" in this plugin will delete past non-word characters and to the end of the following proper
word. The same goes for word movements such as Next Word and Previous Word.

If the setting "use camel case" is true, these commands take that into account.

The commands are:

- Next Word (`alt-f`). Move the caret to the end of the current word (where current word is defined as everything up
  to the next end of word characters).
- Previous Word (`alt-b`). Move the caret to the start of the current word (where current word is defined as everything back to the next end
  of word characters, read backwards).
- Delete Next Word (`alt-d`). Delete characters to the end of the current word (defined as above).
- Delete Previous Word (`alt-BACKSPACE`). Delete characters back to the start of the current word (defined as above).

The word movement commands work with multiple carets.

## Modify Word

The modify word commands word boundaries in the same way as the word movement commands defined [above](#word-movement).
They will modify the characters in the current or previous word or - if selection is active - modify the current selection.

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

## Transpose Words

The transpose words commands transpose the word at point with either the following or previous word. Non-word characters are skipped over,
so that if two transposed words are separated by non-word characters, the words change place with the same delimiters between them as before
the change.

If the selection is active, the selected region is transposed with the following (forward) or previous (backward) word.

The commands are:

- Transpose Current and Next Word (`alt-t`). Change the order of the word at point (or region) and the following word.
- Transpose Previous and Current Word (`shift-alt-t`). Change the order of the word at point (or region) and the previous word.

The transpose word commands work with multiple carets.

## Delete whitespace on current line

The delete whitespace commands reduce the number of whitespace characters around point.

The commands are:

- Delete all Whitespace Around Point (`ctrl-BACKSPACE`). Delete whitespace before and after caret.
- Only One Space at Point (`alt-SPACE`). Reduce all whitespace around point to only one space.

The delete whitespace commands work with multiple carets.

## Delete Empty Lines

The command Delete Blank Lines deletes lines that are either empty or only containing whitespace. If the caret is currently
on a non-blank line, all subsequent blank lines are deleted. If the current line is blank and the surrounding lines are non-blank, the
current line is deleted. If the current line is blank and form part of a consecutive number of blank lines, the blank lines are reduced to a
single blank line.

The commands are:

- Delete Blank Lines(`ctrl-x ctrl-o`).

## Duplicate (and Comment)

The duplicate commands duplicate the current line or the selected region. Duplicate Line/Region and Comment furthermore comments the
original line of copy.

The difference between IntelliJ's standard duplicate command and the plugin command Duplicate Region is that the latter leaves the caret at
its original position, and doesn't move it to the end of the new copy.

The commands are:

- Duplicate Line/Region (`ctrl-c y`).
- Duplicate Line/Region and Comment (`ctrl-c c`).

The duplicate commands work with multiple carets.

## Rectangles

A rectangle is defined as the rectangular region limited by the upper left corner and the lower right corner of the active selection. Thus,
to use these commands, first select a region and then use the proper command. No characters to the left or to the right of the rectangle
will be affected (although they might move left or right).

The Paste Rectangle command does not require a current selection.

The commands are:

- Copy Rectangle (`ctrl-x alt-c`). Copy the contents of the rectangle to clipboard.
- Cut Rectangle (`ctrl-x alt-k`). Copy the contents of the rectangle to clipboard, and delete it from the editor. Text to the right of the
  rectangle is adjusted leftward.
- Open Rectangle (`ctrl-x alt-o`). Create a blank rectangle by shifting all text to the right of the rectangle.
- Clear Rectangle (`ctrl-x alt-c`). Create a blank rectangle by replacing all text within the rectangle with space.
- Paste Rectangle (`ctrl-x alt-p`). Paste the contents of the clipboard. If the clipboard contents are multi-line, each line is pasted with
  the same start column as the rectangle's upper left-hand corner. Text to the right of the insertion point is shifted rightward.

## Paste

The paste commands enable the use of a paste history where a pasted snippet of text can be replaced by previous killed texts. By repeatedly
pressing Paste: Previous Item in History after one use of Paste or Paste and Leave Caret at Point, the pasted text is replaced by the next
item in the list of previously killed texts.

The items offered when using Paste: Previous Item are filtered for duplicates and blank entries.

The commands are:

- Paste (`ctrl-y`). This command works as standard IntelliJ paste, but it sets up the paste history so that further invocations of Paste:
  Previous Item in History can suggest previously killed texts.
- Paste and Leave Caret at Point (`ctrl-u ctrl y`). Works as Paste, but also leaves the caret at the current point and not at the end of the
  pasted text.
- Paste: Previous Item in History (`alt-y`). Cycle through the history of killed text, replacing the previously pasted text.

## Push/Pop Mark

The plugin maintains a mark history, if the plugin push mark command is used. This makes it possible to pop previous marks and go back to
these previous locations. A separate mark history is maintained for each file where it is used. The history only contains unique positions;
a duplicate replaces any earlier item at the same position in the file.

Besides maintaining a mark history, another difference between the plugin command and the standard IntelliJ set-mark command, is the the
former always starts a new selection, whereas IntelliJ's command toggles selection. In other words, if a selection is already active, using
the plugin command starts a new selection at the current point, instead of only turning off the selection.

Adding to the mark history without starting a new selection can be achieved by quickly hitting the set-mark command twice.

The commands [iSearch](#isearch) and [Search/replace](#searchreplace) set the mark at the beginning of a search so that one can return to
the position where the latest search started. The command [Exchange Point and Mark](#exchange-point-and-mark) also uses the mark history, as
described below.

The commands are:

- Push Mark (`ctrl-SPACE`). Set the mark (and activate sticky selection). The mark is saved to the mark history. To only save the mark to
  history without starting selection, quickly hit the key binding twice (`ctrl-SPACE ctrl-SPACE`).
- Pop Mark (`ctrl-u ctrl-SPACE`). Pop an item from the mark history and return caret to the saved position. History is maintained per file.

## Exchange Point and Mark

When using sticky selection, Exchange Point and Mark switches the position of the selection start and the current caret. The selected region
can then be expanded in another direction. The location of the caret before the exchange is added to the mark history.

If the selection is not active when the command is used, sticky selection is activated between the current point and the last saved mark.

The commands are:

- Exchange Point and Mark (`ctrl-x ctrl-x`). Switch positions of selection start and current point. Reactivate selection from previous mark
  if no selection is active.

## Recenter/Reposition

Recenter and Reposition cycles the location of the caret between three different locations: middle, top and bottom of the currently visible
part of the editor. Recenter scrolls the editor contents to move the caret, whereas Reposition only moves the caret without scrolling the
editor.

The commands are:

- Recenter Caret (`ctrl-l`). Cycle the position of the caret between middle, top and bottom by scrolling the editor contents.
- Reposition Caret (`alt-r`). Cycle the position of the caret between middle, top and bottom without scrolling the editor contents.

## Kill Line

Kill Line (`ctrl-k`) works as the standard IntelliJ command Cut up to Line End: it kills (and copies) the rest of the current line. If there
is only whitespace between caret and end of the line, the new line character is also killed. This plugin command expands the command such
that the newline character is also killed if the caret is positioned on the very first position of the line.

The commands are:

- Kill Line (`ctrl-k`). Kill and copy the rest of the current line, including the final newline character if caret is on first position of
  line or there is only whitespace between caret and line end.

The Kill Line command works with multiple carets.
