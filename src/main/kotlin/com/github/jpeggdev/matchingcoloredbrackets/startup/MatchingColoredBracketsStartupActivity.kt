package com.github.jpeggdev.matchingcoloredbrackets.startup

import com.github.jpeggdev.matchingcoloredbrackets.services.BracketColorService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Project startup activity for the Matching Colored Brackets plugin.
 * Initializes the plugin when a project is opened.
 */
class MatchingColoredBracketsStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        thisLogger().info("Matching Colored Brackets plugin initialized for project: ${project.name}")

        // Ensure the bracket color service is initialized
        BracketColorService.getInstance(project)
    }
}