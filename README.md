# Matching Colored Brackets

![Build](https://github.com/jpeggdev/matching-colored-brackets/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

A free and open-source IntelliJ IDEA plugin that colorizes matching brackets to improve code readability and navigation.

<!-- Plugin description -->

## Features

**Matching Colored Brackets** helps you quickly identify matching bracket pairs in your code by assigning different
colors to each nesting level. This makes it easier to read and navigate complex code structures, especially in deeply
nested code.

### Key Features:

- 🎨 **Colorful Bracket Matching** - Automatically colors matching brackets, parentheses, braces, and angle brackets
- 🔄 **Unlimited Nesting Levels** - Supports any depth of nesting with cycling colors
- 🌓 **Theme-Aware** - Different color palettes for light and dark themes
- 📝 **Universal Support** - Works with all file types and programming languages
- 🆓 **Completely Free** - No premium features or paywalls - everything is free and open-source
- 🚀 **Lightweight** - Minimal performance impact with efficient bracket detection

### Supported Bracket Types:

- Parentheses: `( )`
- Square brackets: `[ ]`
- Curly braces: `{ }`
- Angle brackets: `< >` (for generics and templates)

### How It Works:

The plugin automatically detects bracket pairs in your code and assigns colors based on their nesting depth. Each level
gets a different color from a carefully chosen palette that ensures good visibility and contrast. The colors cycle
through the palette for deeply nested structures.

<!-- Plugin description end -->

## Architecture

The plugin is structured around three core components:

### BracketColorService (`services/BracketColorService.kt`)

A project-level service that manages bracket colors and provides utilities for bracket detection:

- Maintains color palettes for light and dark themes
- Provides color lookups based on nesting depth (cycling through palette for deep nesting)
- Contains bracket matching logic and detection methods
- Handles theme-aware color selection via `JBColor`

### MatchingColoredBracketsAnnotator (`annotator/MatchingColoredBracketsAnnotator.kt`)

The core annotation engine that applies colors to brackets in the editor:

- Processes all elements in open files via PSI (Program Structure Interface)
- Calculates bracket nesting depth by traversing the PSI tree
- Skips brackets inside string literals and comments
- Applies colored text attributes to matching bracket pairs
- Special handling for angle brackets used in generics

### MatchingColoredBracketsStartupActivity (`startup/MatchingColoredBracketsStartupActivity.kt`)

Project initialization hook that ensures the plugin is ready when a project opens:

- Initializes the `BracketColorService` on project startup
- Provides logging for plugin initialization

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "matching-colored-brackets"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/jpeggdev/matching-colored-brackets/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Project Structure

```
src/main/kotlin/com/github/jpeggdev/matchingcoloredbrackets/
├── annotator/
│   └── MatchingColoredBracketsAnnotator.kt      # Core bracket coloring logic
├── services/
│   └── BracketColorService.kt                   # Color management & bracket utilities
└── startup/
    └── MatchingColoredBracketsStartupActivity.kt # Project initialization
```

## Development

### Building the Plugin

```bash
# Build the plugin distribution
./gradlew buildPlugin

# Run the plugin in a test IDE
./gradlew runIde

# Run tests
./gradlew test

# Full verification (tests + plugin compatibility check)
./gradlew check
```

### Key Concepts

- **Annotator Pattern**: The plugin uses IntelliJ's `Annotator` interface to process PSI elements and apply text
  attributes for coloring
- **PSI Tree Traversal**: Bracket depth is calculated by walking the PSI (Program Structure Interface) tree to count
  nesting levels
- **Theme Awareness**: Colors automatically adjust between light and dark themes using IntelliJ's `JBColor` API
- **String/Comment Filtering**: The annotator intelligently skips brackets inside string literals and comments

### Supported Brackets

The plugin colors all standard bracket types with proper matching:

- `( )` - Parentheses
- `[ ]` - Square brackets
- `{ }` - Curly braces
- `< >` - Angle brackets (for generics and templates)

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
