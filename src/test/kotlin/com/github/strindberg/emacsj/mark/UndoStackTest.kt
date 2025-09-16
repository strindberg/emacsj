package com.github.strindberg.emacsj.mark

class UndoStackTest : junit.framework.TestCase() {

    fun `test Basic undo test`() {
        val stack = UndoStack<String>()

        stack.push("A")

        assertEquals("A", stack.undo("B"))

        // Undo again from "C", but stack is empty now, so should return null
        assertNull(stack.undo("C"))
    }

    fun `test Push, undo, redo, undo`() {
        val stack = UndoStack<String>()

        stack.push("A")
        assertEquals("A", stack.undo("B")) // undo from B → A
        assertEquals("B", stack.redo("A")) // redo from A → B
        assertEquals("A", stack.undo("B")) // undo again from B → A
    }

    fun `test Redo returns previous state after undo`() {
        val stack = UndoStack<String>()

        stack.push("A")
        stack.push("B")
        assertEquals("B", stack.undo("C")) // go back to B, redo stack has C
        assertEquals("C", stack.redo("B")) // redo to C
    }

    fun `test Empty stack behavior`() {
        val stack = UndoStack<String>()

        assertNull(stack.undo("A"))
        assertNull(stack.redo("A"))
    }

    fun `test Redo is cleared after push`() {
        val stack = UndoStack<String>()

        stack.push("A")
        stack.push("B")

        assertEquals("B", stack.undo("Pos1")) // Undo to B
        assertEquals("Pos1", stack.redo("B")) // Redo to Pos1

        // Now we undo again, this will go back to B (Pos1 isn't in the undo stack)
        assertEquals("B", stack.undo("Pos2"))

        // Push a new value, which should clear redo
        stack.push("C")

        // Redo stack should now be empty
        assertNull(stack.redo("C"))
    }

    fun `test Multiple undo redo chain`() {
        val stack = UndoStack<String>()

        stack.push("1")
        stack.push("2")
        stack.push("3")
        stack.push("4")

        assertEquals("4", stack.undo("5")) // back to 4
        assertEquals("3", stack.undo("4")) // back to 3
        assertEquals("2", stack.undo("3")) // back to 2
        assertEquals("3", stack.redo("2")) // forward to 3
        assertEquals("4", stack.redo("3")) // forward to 4
        assertEquals("5", stack.redo("4")) // forward to 5
        assertNull(stack.redo("5")) // no more redo
    }
}
