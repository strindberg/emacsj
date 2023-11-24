# EmacsJ

<!-- Plugin description -->

A collection of Emacs-inspired commands for text editing and search.

This plugin is a collection of commands which attempt to make IntelliJ-based IDEs behave more closely to the Emacs way: some commands are 
modified versions of commands that IntelliJ already has, some are commands that do not exist in IntelliJ out of the box. To use the plugin, 
simply install the plugin, choose the commands you are interested in, and either use the key bindings suggested by the plugin, or choose 
your own bindings in IntelliJ's preferences. If a key binding doesn't seem to do anything, double check that no other command is using 
the same keybinding. 

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "emacsj"</kbd> >
  <kbd>Install</kbd>
  
- Manually:

  Download the [latest release](https://github.com/strindberg/emacsj/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Documentation

## Isearch

Incremental search. Each letter typed is added to the search, and the nearest match is highlighted, as well as all other visible matches.
The order of the current match amongst all matches is displayed together with the total number of matches. Keep adding letters to narrow
your search.

While searching, pressing the search keybinding again jumps to the next match, if available. Once the matches are exhausted at the end of
the file, the search bar indicates that no more matches can be found. Pressing the search key again at this stage restarts the search at the
beginning of the file. The direction of the search can be changed at any time with the corresponding key.

There are four ways to start a search:

-   Search Forward Text (`ctrl-s`). The written text is interpreted as literal text, and the search direction is forward in the editor.
-   Search Backward Text (`ctrl-r`). The written text is interpreted as literal text, and the search direction is backward in the editor.
-   Search Forward Regexp (`ctrl-alt-s`). The written text is interpreted as regular expression, and the search direction is forward in the editor.
-   Search Backward Regexp (`ctrl-alt-r`). The written text is interpreted as regular expression, and the search direction is backward in the
    editor.

When search has been initiated, pressing one of the search keys again will bring up the previous text used for search. This is the quickest 
way to resume a search. History is kept separate between text search and regexp search, so only previous searches by the same type are
offered. The search history does not separate forward and backward searches.

While searching, the following keys are active:

-   `ESCAPE` or `ctrl-g`: abort the search and return the caret to the point where the search started.
-   `ENTER`: abort the search and leave the caret where it currently is.
-   `BACKSPACE`: if several matches with the current search string have been visited, go back to in the history of matches. If not, remove
     the last character from the search string.
-   `ctrl-w`: add the next word in the editor to the search string.
-   `ctrl-alt-e`: add the rest of the current editor line to the search string.
-   `ctrl-alt-y`: add the character at point to the search string.
-   `alt-p`: choose backward in the list of previous searches (of the current type) performed.
-   `alt-n`: choose backward in the list of previous searches (of the current type) performed.
-   `ctrl-shift-ENTER`: add new line character to the search string.

Text from the clipboard can be pasted during searching: the contents of the clipboard will be added to the current search string.

Isearch text uses smart case, such that the search will be performed without case sensitivity if the whole search string is lower case
characters, but switch to case-sensitive search if the search string contains one or more capital letters. If you need to search for a lower
case string, and not match upper case letters, use regexp search which is always case-sensitive.


## Search/replace

Search and replace asks for a text or regexp to search for, and then the replacement to be used. When replacement starts, each match found
triggers a request for confirmation.

There are two variants:

-   Search and replace text(`alt-s`). Both arguments are interpreted as literal texts.
-   Search and replace regexp(`shift-alt-s`). Both arguments are interpreted as regular expressions.

Once the replacement text has been given, search is performed from the location of the caret, and at each match you can choose whether
to perform the change or not.

If the selection is active when Search/replace is started, the replacement is only performed within the current selection.

When using regexp search, back references into groups in the matched string can be used in the replace string either in java style 
(`%1, %2, ...`) or with backslash (`\1, \2, ...`). The whole match can be referenced by `$0` or `\&`.

While replacing, the following keys are active:

-   `y` `SPACE`: perform the replacement and move to the next match.
-   `n`: do not perform the replacement and move to the next match.
-   `!`: perform the replacement on this and all the following matches.
-   `.`: perform the replacement on this match and then stop.
-   `alt-p`: choose backward in the list of previous Search/replace (of the current type) performed.
-   `alt-n`: choose backward in the list of previous Search/replace (of the current type) performed.
-   `ctrl-shift-ENTER`: add new line character to the search text or replace text.

Search/replace text uses smart case, such that the search will be performed without case sensitivity if the whole search string  and the whole
replacement string is lower case characters, but switch to case-sensitive search if the search string or replace string contains one or 
more capital letters. If you need to search and replace two lower case strings, and not match upper case letters, use regexp Search/replace
which is always case-sensitive.

## Word Movement

The word movement commands are the same as default IntelliJ movements, but with a slight difference in how word boundaries as non-word
characters are handled. In standard IntelliJ, "kill to word end" will delete character up to the next non-word character, whereas the
corresponding command "Delete next word" in this plugin will delete past non-word characters and into the following proper word. The same
goes for word movements such as "next word" and "previous word".

If the setting "use camel case" is true, these commands take that into account.

The included commands are:

-   Next word (`alt-f`). Move the caret to the end of the current word (where current word is defined as everything up
    to the next end of word characters).
-   Previous word (`alt-b`). Move the caret to the start of the current word (where current word is defined as everything up to the next end 
    of word characters, read backwards).
-   Delete next word (`alt-d`). Delete characters to the end of the current word (defined as above).
-   Delete previous word (`alt-BACKSPACE`). Delete characters back to the start of the current word (defined as above).


## Modify Word

The modify word commands use the same definition of word boundaries as the word movement commands defined [above](#word-movement). 
They will modify the characters in the current or previous word or &#x2014; if selection is active &#x2014; modify the current selection.

The commands are:

-   Upper case current word or region (`alt-u`).
-   Upper case previous word (`shift-alt-u`).
-   Lower case current word or region (`alt-l`).
-   Lower case previous word (`shift-alt-l`).
-   Capitalize current word or region (`alt-c`).
-   Capitalize previous word (`shift-alt-c`).


## Transpose Words

The transpose words commands use the same definition of word boundaries as the word movement commands defined [above](#word-movement).

If the selection is active, the active selection is transposed with the following or previous word, respectively.

The commands are:

-   Transpose Current and Next Word (`alt-t`). Change the order of the current word (or region) and the following word.
-   Transpose Previous and Current  Word (`shift-alt-t`). Change the order of the current word (or region) and the previous word.


## Delete whitespace on current line

The commands are:

-   Delete all Whitespace Around Point (`ctrl-BACKSPACE`). Delete whitespace before and after caret.
-   Only One Space at Point (`alt-SPACE`). Reduce all whitespace around point to only one space.


## Delete Empty Lines

The command Delete Blank Lines(`ctrl-x ctrl-o`) deletes lines that are either empty or only containing whitespace. If the caret is currently
on a non-blank line, all subsequent blank lines are deleted. If the current line is a blank and the surrounding lines are non-blank, the
current line is deleted. If the current line is blank and part of a consecutive number of blank lines, the blank lines are reduced to a
single blank line.


## Duplicate

Duplicate Line/Region and Comment (`ctrl-c c`) duplicates the current line or active region, and comments the original copy.


## Rectangles

A rectangle is defined as the rectangular region limited by the upper left corner and the lower right corner of the active selection.

The commands are:

-   Copy rectangle (`ctrl-x alt-c`). Copy the contents of the rectangle to clipboard.
-   Cut rectangle (`ctrl-x alt-k`). Copy the contents of the rectangle to clipboard, and delete it from the editor. Text to the right of the
    rectangle is adjusted leftward.
-   Open rectangle (`ctrl-x alt-o`). Create a blank rectangle from the current selection by shifting all text to the right of the rectangle.
-   Clear rectangle (`ctrl-x alt-c`). Create a blank rectangle form the current selection by replacing all text within the rectangle with space.
-   Paste rectangle (`ctrl-x alt-p`). Paste the contents of the clipboard. If the clipboard contents are multi-line, each line is pasted with
    the same start column as the current upper left-hand corner.


## Paste

The paste commands enable the use of a paste history where a pasted snippet of text can be replaced by previous killed texts. By repeatedly
pressing Paste: Previous Item in History (`alt-y`) after one use of paste (`ctrl-y`), the pasted text is replaced by the next item.

The commands are:

-   Paste (`ctrl-y`). This command works as standard IntelliJ paste, but it sets up the paste history so that further invocations of Paste:
    Previous Item in History can suggest previously copied snippets.
-   Paste and Leave Caret at Point (`ctrl-u ctrl y`). Works as Paste, but also leaves the caret at the current point and not at the end of the
    pasted text. Also works with paste history.
-   Paste: Previous Item in History (`alt-y`). Cycle through the history of killed text, replacing the previously pasted snippet.


## Push/Pop Mark

The plugin maintains a mark history, if the plugin push mark command is used. This makes it possible to pop previous marks and go back to
these previous locations. A separate mark history is maintained for each file where it is used.

The commands [iSearch](#isearch) and [Search/replace](#searchreplace) set the mark at the beginning of a search so that one can return to 
the position where the latest search started. The command [Exchange Point and Mark](#exchange-point-and-mark) also uses the mark history.

The commands are:

-   Push Mark (`ctrl-SPACE`). Set the mark (and activate sticky selection). The mark is saved to the mark history. To only save the mark to
    history without starting selection, quickly hit the key binding twice (`ctrl-SPACE ctrl-SPACE`).
-   Pop Mark (`ctrl-u ctrl-SPACE`). Pop an item from the mark history and return caret to the saved position. History is maintained per file.


## Exchange Point and Mark

When using sticky selection, Exchange Point and Mark (`ctrl-x ctrl-x`) switches the position of the selection start and the current caret. The
selected region can then be expanded in another direction.

If the selection is not active when the command is used, sticky selection is activated between the current point and the last saved mark.

The commands are:

-   Exchange Point and Mark (`ctrl-x ctrl-x`). Switch positions of selection start and current point. Reactivate selection from previous mark if
    no selection is active.


## Recenter/Reposition

Recenter (`ctrl-l`) and reposition (`alt-r`) cycles the location of the caret between three different locations: middle, top and bottom of the
currently visible part of the editor. Recenter scrolls the editor contents to move the caret, whereas Reposition only move the caret without
scrolling the editor.

The commands are:

-   Recenter Caret (`ctrl-l`). Cycle the position of the caret between middle, top and bottom by scrolling the editor contents.
-   Reposition Caret (`alt-r`). Cycle the position of the caret between middle, top and bottom without scrolling the editor contents.


## Kill Line

Kill Line (`ctrl-k`) works as the standard IntelliJ command Cut up to Line End: it kills (and copies) the rest of the current line. If there is
only whitespace between caret and end of the line, the new line character is also killed. The addition from this plugin command that the
newline character is also killed if the caret is positioned on the very first position of the line.

The commands are:

-   Kill Line (`ctrl-k`). Kill and copy the rest of the current line, including the final newline character if caret is on first position of
    line or there is only whitespace between caret and line end.
