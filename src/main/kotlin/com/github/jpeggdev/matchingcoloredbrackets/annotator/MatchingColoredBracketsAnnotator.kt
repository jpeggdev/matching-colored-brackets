package com.github.jpeggdev.matchingcoloredbrackets.annotator

import com.github.jpeggdev.matchingcoloredbrackets.services.BracketColorService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType

/**
 * Annotator responsible for coloring matching brackets in the editor.
 * This runs on every file and applies colors based on nesting depth.
 */
class MatchingColoredBracketsAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Skip if element is whitespace or comment
        if (element is PsiWhiteSpace || element is PsiComment) {
            return
        }

        val text = element.text
        if (text.isEmpty()) return

        // Check if this is a string literal - we don't want to color brackets inside strings
        if (isInsideStringLiteral(element)) {
            return
        }

        val project = element.project
        val colorService = BracketColorService.getInstance(project)

        // Process single character elements (most brackets)
        if (text.length == 1) {
            val char = text[0]
            if (colorService.isOpeningBracket(char) || colorService.isClosingBracket(char)) {
                val depth = calculateBracketDepth(element, char)
                applyBracketColor(element, holder, colorService, depth)
            }
        }
        // Handle angle brackets which might be part of generics (e.g., List<String>)
        else if (text == "<<" || text == ">>") {
            handleAngleBrackets(element, holder, colorService)
        }
    }

    /**
     * Calculate the nesting depth of a bracket using a stack-based approach.
     * This ensures matching pairs get the same color.
     */
    private fun calculateBracketDepth(element: PsiElement, bracket: Char): Int {
        val colorService = BracketColorService.getInstance(element.project)

        // Get the file content
        val file = element.containingFile
        val document = file?.viewProvider?.document ?: return 0
        val text = document.text
        val elementStartOffset = element.textOffset

        if (elementStartOffset < 0) return 0

        // Stack to track opening brackets and their depths
        val bracketStack = mutableListOf<Pair<Char, Int>>()
        var inString = false
        var stringChar = ' '
        var inComment = false
        var inLineComment = false

        // Scan from beginning of file to current bracket position (inclusive)
        for (i in 0..elementStartOffset) {
            if (i >= text.length) break
            val char = text[i]
            val nextChar = if (i + 1 < text.length) text[i + 1] else ' '

            // Handle comments
            if (!inString) {
                when {
                    inLineComment && char == '\n' -> inLineComment = false
                    inLineComment -> continue
                    char == '/' && nextChar == '/' -> inLineComment = true
                    char == '/' && nextChar == '*' -> inComment = true
                    char == '*' && nextChar == '/' && inComment -> {
                        inComment = false
                    }
                    inComment -> continue
                }
            }

            // Handle strings
            when {
                (char == '"' || char == '\'' || char == '`') && (i == 0 || text[i - 1] != '\\') -> {
                    if (!inString) {
                        inString = true
                        stringChar = char
                    } else if (char == stringChar) {
                        inString = false
                    }
                }
            }

            // Process brackets (skip if in string or comment)
            if (!inString && !inComment && !inLineComment) {
                when {
                    colorService.isOpeningBracket(char) -> {
                        // If this is the bracket we're looking for
                        if (i == elementStartOffset) {
                            return bracketStack.size
                        }
                        // Push to stack with current depth
                        bracketStack.add(char to bracketStack.size)
                    }
                    colorService.isClosingBracket(char) -> {
                        // Find matching opening bracket
                        val matchingOpen = colorService.getMatchingBracket(char)
                        // Find and remove the matching bracket from stack
                        for (j in bracketStack.size - 1 downTo 0) {
                            if (bracketStack[j].first == matchingOpen) {
                                val depth = bracketStack[j].second
                                // If this is the bracket we're looking for
                                if (i == elementStartOffset) {
                                    return depth
                                }
                                // Remove the matched bracket
                                bracketStack.removeAt(j)
                                break
                            }
                        }
                    }
                }
            }
        }

        // Default return
        return 0
    }

    /**
     * Apply color to a bracket element.
     */
    private fun applyBracketColor(
        element: PsiElement,
        holder: AnnotationHolder,
        colorService: BracketColorService,
        depth: Int
    ) {
        val color = colorService.getColorForDepth(depth)
        val textAttributes = TextAttributes().apply {
            foregroundColor = color
        }

        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(element.textRange)
            .enforcedTextAttributes(textAttributes)
            .create()
    }

    /**
     * Special handling for angle brackets which might be used in generics.
     */
    private fun handleAngleBrackets(
        element: PsiElement,
        holder: AnnotationHolder,
        colorService: BracketColorService
    ) {
        // For now, treat angle brackets similar to other brackets
        val text = element.text
        if (text.isNotEmpty()) {
            val char = if (text.startsWith('<')) '<' else '>'
            val depth = calculateBracketDepth(element, char)
            applyBracketColor(element, holder, colorService, depth)
        }
    }

    /**
     * Check if an element is inside a string literal or comment.
     * This is a generic approach that works across different languages.
     */
    private fun isInsideStringLiteral(element: PsiElement): Boolean {
        // Check if element is in a comment
        if (element is PsiComment) {
            return true
        }

        // Check the element type name for common string/literal patterns
        val elementTypeName = element.elementType?.toString() ?: ""
        if (elementTypeName.contains("STRING", ignoreCase = true) ||
            elementTypeName.contains("LITERAL", ignoreCase = true) ||
            elementTypeName.contains("COMMENT", ignoreCase = true)) {
            return true
        }

        // Check if any parent is a string literal or comment
        var parent = element.parent
        while (parent != null) {
            val parentTypeName = parent.elementType?.toString() ?: ""
            // Check for common string and literal patterns in the class name
            if (parent.javaClass.simpleName.contains("String", ignoreCase = true) ||
                parent.javaClass.simpleName.contains("Literal", ignoreCase = true) ||
                parent.javaClass.simpleName.contains("Comment", ignoreCase = true) ||
                parentTypeName.contains("STRING", ignoreCase = true) ||
                parentTypeName.contains("LITERAL", ignoreCase = true) ||
                parentTypeName.contains("COMMENT", ignoreCase = true)) {
                return true
            }
            parent = parent.parent
        }

        return false
    }
}