package com.github.jpeggdev.matchingcoloredbrackets.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Service responsible for managing bracket colors and depth tracking.
 * Provides a consistent color palette for matching brackets at different nesting levels.
 */
@Service(Service.Level.PROJECT)
class BracketColorService() {

    companion object {
        fun getInstance(project: Project): BracketColorService = project.service()

        // Define a vibrant color palette for bracket levels (theme-aware)
        private val BRACKET_COLORS = listOf(
            JBColor(Color(255, 85, 85), Color(255, 95, 95)),       // Red
            JBColor(Color(255, 184, 108), Color(255, 194, 118)),   // Orange
            JBColor(Color(253, 203, 110), Color(255, 213, 120)),   // Yellow
            JBColor(Color(100, 200, 130), Color(110, 210, 140)),   // Green
            JBColor(Color(120, 180, 255), Color(130, 190, 255)),   // Blue
            JBColor(Color(200, 140, 255), Color(210, 150, 255)),   // Purple
            JBColor(Color(255, 140, 200), Color(255, 150, 210))    // Pink
        )
    }

    /**
     * Get JBColor (theme-aware color) for a specific depth.
     * Automatically switches between light and dark theme colors.
     * Colors cycle through the palette for deeply nested brackets.
     */
    fun getColorForDepth(depth: Int): JBColor {
        if (depth < 0) return JBColor.GRAY

        val index = depth % BRACKET_COLORS.size
        return BRACKET_COLORS[index]
    }

    /**
     * Checks if a character is an opening bracket.
     */
    fun isOpeningBracket(char: Char): Boolean {
        return char in setOf('(', '[', '{', '<')
    }

    /**
     * Checks if a character is a closing bracket.
     */
    fun isClosingBracket(char: Char): Boolean {
        return char in setOf(')', ']', '}', '>')
    }

    /**
     * Gets the matching bracket for a given bracket character.
     */
    fun getMatchingBracket(bracket: Char): Char? {
        return when (bracket) {
            '(' -> ')'
            ')' -> '('
            '[' -> ']'
            ']' -> '['
            '{' -> '}'
            '}' -> '{'
            '<' -> '>'
            '>' -> '<'
            else -> null
        }
    }

    /**
     * Determines if two brackets form a matching pair.
     */
    fun isBracketPair(open: Char, close: Char): Boolean {
        return when (open) {
            '(' -> close == ')'
            '[' -> close == ']'
            '{' -> close == '}'
            '<' -> close == '>'
            else -> false
        }
    }
}