package com.github.jpeggdev.matchingcoloredbrackets

import com.github.jpeggdev.matchingcoloredbrackets.services.BracketColorService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.JBColor

class BracketColorServiceTest : BasePlatformTestCase() {

    private lateinit var bracketColorService: BracketColorService

    override fun setUp() {
        super.setUp()
        bracketColorService = project.service<BracketColorService>()
    }

    fun testBracketIdentification() {
        // Test opening brackets
        assertTrue(bracketColorService.isOpeningBracket('('))
        assertTrue(bracketColorService.isOpeningBracket('['))
        assertTrue(bracketColorService.isOpeningBracket('{'))
        assertTrue(bracketColorService.isOpeningBracket('<'))
        assertFalse(bracketColorService.isOpeningBracket(')'))
        assertFalse(bracketColorService.isOpeningBracket('a'))

        // Test closing brackets
        assertTrue(bracketColorService.isClosingBracket(')'))
        assertTrue(bracketColorService.isClosingBracket(']'))
        assertTrue(bracketColorService.isClosingBracket('}'))
        assertTrue(bracketColorService.isClosingBracket('>'))
        assertFalse(bracketColorService.isClosingBracket('('))
        assertFalse(bracketColorService.isClosingBracket('a'))
    }

    fun testMatchingBrackets() {
        // Test matching bracket pairs
        assertEquals(')', bracketColorService.getMatchingBracket('('))
        assertEquals('(', bracketColorService.getMatchingBracket(')'))
        assertEquals(']', bracketColorService.getMatchingBracket('['))
        assertEquals('[', bracketColorService.getMatchingBracket(']'))
        assertEquals('}', bracketColorService.getMatchingBracket('{'))
        assertEquals('{', bracketColorService.getMatchingBracket('}'))
        assertEquals('>', bracketColorService.getMatchingBracket('<'))
        assertEquals('<', bracketColorService.getMatchingBracket('>'))
        assertNull(bracketColorService.getMatchingBracket('a'))
    }

    fun testBracketPairValidation() {
        // Test valid bracket pairs
        assertTrue(bracketColorService.isBracketPair('(', ')'))
        assertTrue(bracketColorService.isBracketPair('[', ']'))
        assertTrue(bracketColorService.isBracketPair('{', '}'))
        assertTrue(bracketColorService.isBracketPair('<', '>'))

        // Test invalid bracket pairs
        assertFalse(bracketColorService.isBracketPair('(', ']'))
        assertFalse(bracketColorService.isBracketPair('[', ')'))
        assertFalse(bracketColorService.isBracketPair('{', '>'))
        assertFalse(bracketColorService.isBracketPair('a', 'b'))
    }

    fun testColorAssignment() {
        // Test that different depths get different colors
        val color0 = bracketColorService.getColorForDepth(0)
        val color1 = bracketColorService.getColorForDepth(1)
        val color2 = bracketColorService.getColorForDepth(2)

        assertNotSame(color0, color1)
        assertNotSame(color1, color2)
        assertNotSame(color0, color2)

        // Test that colors cycle for deep nesting
        val color7 = bracketColorService.getColorForDepth(7)
        val color14 = bracketColorService.getColorForDepth(14)
        assertEquals(color0, color7) // Should cycle after 7 colors
        assertEquals(color0, color14) // Should cycle again

        // Test negative depth returns gray
        assertEquals(JBColor.GRAY, bracketColorService.getColorForDepth(-1))
    }

    fun testJBColorThemeAwareness() {
        // Test that JBColors are returned (theme-aware)
        val jbColor0 = bracketColorService.getColorForDepth(0)
        val jbColor1 = bracketColorService.getColorForDepth(1)

        assertNotNull(jbColor0)
        assertNotNull(jbColor1)
        assertNotSame(jbColor0, jbColor1)
    }
}
