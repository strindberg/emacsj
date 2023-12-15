package com.github.strindberg.emacsj.word

class WordUtilsTest : junit.framework.TestCase() {

    fun `test Boundaries 00`() {
        val text = "ab dc"
        assertEquals(
            "ab",
            currentWordBoundaries(text, 0, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 01`() {
        val text = "ab dc"
        assertEquals(
            "ab",
            currentWordBoundaries(text, 1, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 02`() {
        val text = "ab dc"
        assertEquals(
            "ab",
            currentWordBoundaries(text, 2, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 04`() {
        val text = "ab dc"
        assertEquals(
            "dc",
            currentWordBoundaries(text, 4, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 05`() {
        val text = "ab dc"
        assertEquals(
            "dc",
            currentWordBoundaries(text, 5, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 06`() {
        val text = " ab dc"
        assertEquals(
            " ab",
            currentWordBoundaries(text, 0, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Boundaries 07`() {
        val text = " ab dc"
        assertEquals(
            " ab",
            currentWordBoundaries(text, 1, isCamel = false, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 00`() {
        val text = "ab dc"
        assertEquals(
            "ab",
            currentWordBoundaries(text, 0, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 01`() {
        val text = "ab dc"
        assertEquals(
            "ab",
            currentWordBoundaries(text, 1, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 02`() {
        val text = "ab dc"
        assertEquals(
            "dc",
            currentWordBoundaries(text, 2, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 04`() {
        val text = "ab dc"
        assertEquals(
            "dc",
            currentWordBoundaries(text, 4, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 05`() {
        val text = "ab dc"
        assertEquals(
            "dc",
            currentWordBoundaries(text, 5, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 06`() {
        val text = "ab dc "
        assertEquals(
            "dc ",
            currentWordBoundaries(text, 5, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Reverse Boundaries 07`() {
        val text = "ab dc "
        assertEquals(
            "dc ",
            currentWordBoundaries(text, 6, isCamel = false, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 00`() {
        val text = "AbDc"
        assertEquals(
            "Ab",
            currentWordBoundaries(text, 0, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 01`() {
        val text = "AbDc"
        assertEquals(
            "Ab",
            currentWordBoundaries(text, 1, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 02`() {
        val text = "AbDc"
        assertEquals(
            "Ab",
            currentWordBoundaries(text, 2, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 04`() {
        val text = "AbDc"
        assertEquals(
            "Dc",
            currentWordBoundaries(text, 4, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 05`() {
        val text = " AbDc"
        assertEquals(
            " Ab",
            currentWordBoundaries(text, 0, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Boundaries 06`() {
        val text = " AbDc"
        assertEquals(
            " Ab",
            currentWordBoundaries(text, 1, isCamel = true, isForward = true).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 00`() {
        val text = "AbDc"
        assertEquals(
            "Ab",
            currentWordBoundaries(text, 0, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 01`() {
        val text = "AbDc"
        assertEquals(
            "Ab",
            currentWordBoundaries(text, 1, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 02`() {
        val text = "AbDc"
        assertEquals(
            "Dc",
            currentWordBoundaries(text, 2, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 04`() {
        val text = "AbDc"
        assertEquals(
            "Dc",
            currentWordBoundaries(text, 4, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 05`() {
        val text = "AbDc "
        assertEquals(
            "Dc ",
            currentWordBoundaries(text, 4, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }

    fun `test Camel Reverse Boundaries 06`() {
        val text = "AbDc "
        assertEquals(
            "Dc ",
            currentWordBoundaries(text, 5, isCamel = true, isForward = false).let { (start, end) ->
                text.substring(start, end)
            }
        )
    }
}
