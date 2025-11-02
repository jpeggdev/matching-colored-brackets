# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**matching-colored-brackets** is an IntelliJ IDEA plugin project that is currently a template scaffold from the official IntelliJ Platform Plugin Template. The plugin is designed to provide colored bracket matching functionality for various file types within IntelliJ-based IDEs.

**Status**: Early stage project (v0.0.1) - template code has been customized with core bracket coloring functionality. Implementation includes bracket detection, color service, and annotator for multiple language support.

## Project Type & Purpose

- **Type**: IntelliJ Platform Plugin (for IntelliJ IDEA, Community Edition)
- **Language**: Kotlin (primary) with Gradle build system
- **Target Platform**: IntelliJ IDEA 2024.3+ (build 243+)
- **Java Version**: JDK 21 (LTS)
- **Purpose**: Enhance code editor experience by providing colored bracket matching across 8+ programming languages

## Plugin Identifiers

- **Plugin ID**: `com.github.jpeggdev.matchingcoloredbrackets`
- **Plugin Group**: `com.github.jpeggdev.matchingcoloredbrackets`
- **Plugin Name**: `matching-colored-brackets`
- **Repository URL**: https://github.com/jpeggdev/matching-colored-brackets
- **Vendor**: jpeggdev

## Directory Structure

```
matching-colored-brackets/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/github/jpeggdev/matchingcoloredbrackets/
│   │   │       ├── MyBundle.kt                               # Resource bundle for i18n
│   │   │       ├── annotator/
│   │   │       │   └── MatchingColoredBracketsAnnotator.kt   # Bracket coloring annotator (registered for multiple languages)
│   │   │       ├── services/
│   │   │       │   └── BracketColorService.kt                # Project-level service for color management and bracket logic
│   │   │       └── startup/
│   │   │           └── MatchingColoredBracketsStartupActivity.kt  # Project initialization activity
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml                     # Plugin manifest & extension declarations
│   │       └── messages/
│   │           └── MyBundle.properties            # Localization strings
│   └── test/
│       ├── kotlin/
│       │   └── com/github/jpeggdev/matchingcoloredbrackets/
│       │       └── MyPluginTest.kt                # Unit tests
│       └── testData/
│           └── rename/                            # Test data for refactoring tests
│               ├── foo.xml
│               └── foo_after.xml
├── gradle/                                        # Gradle wrapper files
├── .github/
│   ├── workflows/
│   │   ├── build.yml                              # CI: Build, Test, Verify, Release Draft
│   │   ├── release.yml                            # Publish to JetBrains Marketplace
│   │   └── run-ui-tests.yml                       # Manual UI testing workflow
│   └── dependabot.yml                             # Dependency updates
├── .run/                                          # IntelliJ run configurations
│   ├── Run Plugin.run.xml                         # gradle runIde
│   ├── Run Tests.run.xml                          # gradle check
│   └── Run Verifications.run.xml                  # gradle verifyPlugin
├── build.gradle.kts                               # Build configuration (Kotlin DSL)
├── settings.gradle.kts                            # Project root settings
├── gradle.properties                              # Gradle properties (versions, etc.)
├── gradle/libs.versions.toml                      # Dependency versions catalog
├── qodana.yml                                     # Code quality inspection config
├── codecov.yml                                    # Code coverage configuration
├── README.md                                      # Plugin documentation
├── CHANGELOG.md                                   # Version change history
└── CLAUDE.md                                      # Claude Code guidance (this file)
```

## Build System & Configuration

### Gradle Setup
- **Build Tool**: Gradle 9.0.0 (via wrapper)
- **Kotlin Version**: 2.1.20
- **Kotlin Stdlib**: Custom (not bundled by default, see: `kotlin.stdlib.default.dependency = false`)
- **JVM Toolchain**: Java 21

### Key Gradle Plugins
1. **IntelliJ Platform Gradle Plugin** (v2.9.0) - Core plugin development support
2. **Kotlin JVM Plugin** (v2.1.20) - Kotlin compilation
3. **Gradle Changelog Plugin** (v2.4.0) - Changelog management
4. **Gradle Qodana Plugin** (v2025.1.1) - Code quality analysis
5. **Gradle Kover Plugin** (v0.9.1) - Code coverage reports
6. **Java Plugin** - Standard Java compilation

### Dependencies
- **Testing**: JUnit 4.13.2, OpenTest4J 1.3.0
- **IntelliJ Platform**: Platform test framework for integration testing
- **Platform Type**: IC (IntelliJ Community Edition)
- **Platform Version**: 2024.3.6

### Performance Optimizations
- Configuration cache enabled for faster builds
- Build cache enabled for incremental compilation

## Plugin Configuration (plugin.xml)

**Extension Points Registered**:
1. **ProjectService**: `BracketColorService` - Color management and bracket logic service
2. **Annotators**: `MatchingColoredBracketsAnnotator` - Registered for: JAVA, Kotlin, XML, Python, JavaScript, TypeScript, HTML, CSS
3. **PostStartupActivity**: `MatchingColoredBracketsStartupActivity` - Initializes plugin on project open

**Plugin Dependencies**:
- Core dependencies: `com.intellij.modules.platform`, `com.intellij.modules.lang`

## Architecture & Key Patterns

### IntelliJ Plugin Architecture Patterns

1. **Service Injection Pattern**
   - Services are obtained via: `project.service<MyProjectService>()`
   - Project-level services: Created once per project, managed by IntelliJ
   - Application-level services: Single instance for entire IDE

2. **Resource Bundle Pattern (i18n)**
   - All UI strings should be externalized to `.properties` files
   - Access via `MyBundle.message("key", args...)`
   - Implements IntelliJ's `DynamicBundle` for automatic reloading

3. **Extension Points**
   - Declared in `plugin.xml`
   - Key extensions: `projectService`, `toolWindow`, `projectOpenedActivity`
   - All extensions are loaded lazily by the platform

4. **Coroutines in Plugin Development**
   - `ProjectActivity` uses suspend functions for async initialization
   - Avoid blocking EDT (Event Dispatch Thread)
   - Use `runInBackground` for long-running operations

### Core Implementation Components

1. **MyBundle.kt** - Resource bundle wrapper for internationalization
   - Provides localized messages via properties files
   - Pattern: Singleton object extending `DynamicBundle`

2. **BracketColorService.kt** - Project-level bracket color and logic service
   - Lifecycle: Created once per project, singleton pattern
   - Responsibilities:
     - Manages theme-aware color palette (7 cycling colors)
     - Detects and validates bracket characters: `()`, `[]`, `{}`, `<>`
     - Identifies bracket matching pairs
   - Key methods:
     - `getColorForDepth(depth)` - Returns `JBColor` (theme-aware) for nesting level
     - `isOpeningBracket()` / `isClosingBracket()` - Character classification
     - `getMatchingBracket()` - Returns matching pair for a bracket
     - `isBracketPair()` - Validates bracket pair validity
   - Color Palette: Red, Orange, Yellow, Green, Blue, Purple, Pink (cycles for deep nesting)

3. **MatchingColoredBracketsAnnotator.kt** - Main bracket coloring annotator
   - Implements `Annotator` interface for real-time syntax highlighting
   - Runs on all registered language types
   - Responsibilities:
     - Processes each PSI element in the editor
     - Calculates bracket nesting depth via stack-based algorithm
     - Applies color formatting to bracket elements
     - Filters out brackets in strings and comments
   - Key features:
     - String literal detection (skips brackets in `"strings"`, `'strings'`, `` `backticks` ``)
     - Comment handling (skips `//` line comments and `/* */` block comments)
     - Escape sequence handling for string detection
     - Special handling for angle brackets in generics (`<>`)
   - Depth calculation: Scans file from start to bracket position, tracking stack of opening brackets

4. **MatchingColoredBracketsStartupActivity.kt** - Project startup activity
   - Implements `ProjectActivity` interface with suspend function
   - Runs asynchronously when project opens
   - Initializes `BracketColorService` to ensure it's ready
   - Logs plugin initialization

5. **MyBundle.properties** - Resource strings
   - Located in `resources/messages/`
   - Currently minimal (template placeholder)

## Testing Setup

### Testing Framework
- **Framework**: IntelliJ Platform Test Framework (BasePlatformTestCase)
- **Test Runner**: JUnit 4
- **Test Location**: `/src/test/kotlin/com/github/jpeggdev/matchingcoloredbrackets/`

### Test Coverage
- **Tool**: Kover (Kotlin coverage tool)
- **Reports**: XML format, uploaded to CodeCov on CI
- **Configuration**: Informational CI threshold (0%)

### Example Tests (MyPluginTest.kt)
1. XML file parsing validation
2. XML structure inspection
3. Refactoring (rename) functionality
4. Service injection and state management

### Test Data
- Location: `/src/test/testData/`
- Contains sample XML files for testing refactoring operations

## Common Development Commands

```bash
# Build the plugin
./gradlew build

# Run IDE with plugin for testing
./gradlew runIde

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.github.jpeggdev.matchingcoloredbrackets.MyPluginTest"

# Run a specific test method
./gradlew test --tests "com.github.jpeggdev.matchingcoloredbrackets.MyPluginTest.testXMLRefactoring"

# Verify plugin compatibility
./gradlew verifyPlugin

# Build plugin distribution (creates JAR in build/distributions/)
./gradlew buildPlugin

# Build and sign the plugin (requires .env file with signing credentials)
source .env && ./gradlew buildPlugin

# Check code with Qodana
./gradlew runInspections

# Generate coverage report
./gradlew koverXmlReport

# Full verification (tests + plugin verification)
./gradlew check
```

### CI/CD Workflows (GitHub Actions)

#### Build Workflow (build.yml) - Runs on push to main and all PRs
- **Steps**:
  1. Gradle wrapper validation
  2. Build plugin
  3. Run tests & upload coverage to CodeCov
  4. Code quality inspection with Qodana
  5. Plugin verification against platform versions
  6. Create draft release (for main branch only)

#### Release Workflow (release.yml) - Triggered on GitHub release
- **Steps**:
  1. Publish to JetBrains Marketplace (requires secrets)
  2. Upload release assets
  3. Create changelog update PR
- **Secrets Required**:
  - `PUBLISH_TOKEN` - JetBrains Marketplace authentication
  - `CERTIFICATE_CHAIN` - Plugin signing certificate
  - `PRIVATE_KEY` - Plugin signing private key
  - `PRIVATE_KEY_PASSWORD` - Private key passphrase
  - `CODECOV_TOKEN` - CodeCov integration (optional)

#### UI Tests Workflow (run-ui-tests.yml) - Manual trigger
- Tests on Linux, Windows, and macOS
- Uses robot-server plugin for IDE UI automation
- Runs with Xvfb on Linux for headless operation

### Quality Gates
- **Qodana**: Code quality analysis on all PRs (JVM Community linter v2024.3)
- **Plugin Verification**: Compatibility check against recommended IDE versions
- **Test Coverage**: Reports to CodeCov (informational)
- **Project JDK**: Java 21 (configured in qodana.yml)

## Plugin Signing Configuration

### Overview
The plugin is configured for signing with a self-signed certificate. Signing is required for publishing to the JetBrains Marketplace.

### Setup Status
- ✅ **Certificate Generated**: Self-signed certificate and private key created
- ✅ **Local Development**: `.env` file configured with base64-encoded credentials
- ✅ **CI/CD Ready**: Instructions provided for GitHub Actions secrets configuration

### Files & Structure
- `.signing/plugin-signing.crt` - Certificate file (PEM format)
- `.signing/plugin-signing.key` - Private key (PEM format)
- `.signing/plugin-signing.crt.base64` - Base64-encoded certificate
- `.signing/plugin-signing.key.base64` - Base64-encoded private key
- `.env` - Local environment with signing credentials (NOT in git)
- `.env.example` - Template for environment variables
- `PLUGIN_SIGNING_SETUP.md` - Detailed setup guide

### Local Development
1. Load environment variables: `source .env`
2. Build with signing: `./gradlew buildPlugin`
3. Output: `build/distributions/matching-colored-brackets-0.0.1.zip`

### GitHub Actions CI/CD
To enable plugin signing in CI/CD pipelines:

1. Go to repository Settings → Secrets and variables → Actions
2. Add secrets:
   - `CERTIFICATE_CHAIN` - Base64-encoded certificate (.signing/plugin-signing.crt.base64)
   - `PRIVATE_KEY` - Base64-encoded private key (.signing/plugin-signing.key.base64)
   - `PRIVATE_KEY_PASSWORD` - Leave empty (key has no password)
   - `PUBLISH_TOKEN` - JetBrains Marketplace token (for publishing)

3. The build workflow will automatically sign the plugin using these secrets

### Security Notes
- The `.env` file is in `.gitignore` and must NEVER be committed
- GitHub Actions secrets are encrypted and only decrypted during workflow runs
- Certificate is valid for 5 years (until 2029)
- If compromised, regenerate certificate and update GitHub secrets

### More Information
See `PLUGIN_SIGNING_SETUP.md` for complete signing setup documentation including:
- How to obtain a JetBrains Marketplace publish token
- How to regenerate certificates if needed
- Troubleshooting signing issues
- Security best practices

## Important Notes & Limitations

### Current State - Core Feature Implementation Complete
The codebase has been customized from the template and now contains the core bracket coloring functionality:
- **BracketColorService**: Fully implemented with 7-color theme-aware palette and bracket logic
- **MatchingColoredBracketsAnnotator**: Fully implemented with real-time syntax highlighting, string/comment detection, and depth calculation
- **Plugin Configuration**: Updated with proper extension registrations for 8 programming languages

### Known Limitations & Future Improvements
1. **Angle Bracket Disambiguation**: The `<>` characters are color-coded but not context-aware (generic types vs. comparison operators)
2. **Escape Sequence Handling**: Basic escape sequence detection (assumes `\` followed by any char), doesn't handle all language-specific variations
3. **String Template Support**: May not correctly detect brackets in advanced string templates (e.g., Kotlin's `"""` or template literals)
4. **Performance**: Full-file scanning on every bracket might impact performance on very large files
5. **Configuration**: No user settings UI for customizing colors (uses hardcoded palette)

### TODO: Remaining Tasks
1. ✅ **DONE**: Set up plugin signing configuration
2. Configure deployment token for JetBrains Marketplace (optional - for publishing)
3. Configure marketplace ID in badges (after first publish)
4. Add unit tests for bracket detection and coloring logic
5. Add user settings UI for color customization (optional)

### Plugin Versioning
- **Semantic Versioning**: Project follows SemVer (https://semver.org)
- **Current Version**: 0.0.1
- **Since Build**: 243 (IntelliJ 2024.3+)
- **Pre-release Channels**: Auto-configured based on version suffix (e.g., `-alpha.3`)

### Gradle Configuration Features
- **Configuration Cache**: Enabled for faster builds
- **Build Cache**: Enabled for incremental builds
- **Kotlin Stdlib**: NOT bundled by default (lightweight plugin)
- **Java Compatibility**: Targets Java 21

## Key Dependencies & External Tools

### Build Dependencies
- IntelliJ Platform SDK (2024.3.6)
- Kotlin Standard Library (via platform)
- JUnit 4 & OpenTest4J for testing

### External Tools
- Gradle 9.0.0 (wrapper)
- Qodana (JetBrains code inspection)
- CodeCov (coverage reporting)
- JetBrains Marketplace (plugin distribution)

## Code Quality & Standards

### Analysis Tools
- **Qodana**: Automated static analysis on all CI runs
- **Kover**: Code coverage tracking
- **IntelliJ Plugin Verifier**: Platform compatibility validation

### Code Style
- **Language**: Kotlin (primary)
- **Language Version**: Latest stable (2.1.20)
- **Formatting**: Follows IntelliJ Kotlin conventions
- **IDE Integration**: `.idea/` contains IntelliJ configuration

## Development Workflow

### IDE Configuration
- Pre-configured run targets in `.run/` directory
- IntelliJ IDEA will auto-detect plugin and provide debugging
- IDE logs available at: `build/idea-sandbox/*/log/idea.log`
- Sandbox IDE opens with fresh settings (no personal plugins/settings)

## Publishing & Distribution

### Installation Methods (Once Published)
1. **IDE Plugin Marketplace**: Built-in plugin browser
2. **JetBrains Marketplace**: Web-based plugin directory
3. **Manual Installation**: Direct JAR file from releases

### Publishing Requirements
- JetBrains Marketplace account
- Plugin signing certificates
- Legal agreements acceptance
- Marketplace ID (to be obtained after first publish)


## Development Tips

### Working with Bracket Coloring
1. **Color Customization**: Update `BRACKET_COLORS` list in `BracketColorService.kt` to change the color palette
2. **Adding Language Support**: Register new annotators in `plugin.xml` for additional language types (copy existing `<annotator>` entries)
3. **Depth Calculation**: The `calculateBracketDepth()` method uses a stack-based algorithm; understand it before modifying bracket matching logic
4. **String/Comment Filtering**: The `isInsideStringLiteral()` method checks both element type names and parent class names for robustness

### Testing & Debugging
1. **Run the Plugin**: Use `./gradlew runIde` to launch a sandbox IDE with the plugin
2. **Enable Logging**: Check `build/idea-sandbox/*/log/idea.log` for plugin logs
3. **Testing**: Use `BasePlatformTestCase` for integration tests with IDE services (see `MyPluginTest.kt`)
4. **Debugging**: Set breakpoints in annotator methods to step through bracket detection

### Code Quality
1. **Service Usage**: Access the bracket color service via `BracketColorService.getInstance(project)`
2. **Theme Awareness**: Use `JBColor` for colors instead of plain `Color` to support light/dark themes
3. **Performance**: For very large files, consider caching bracket depth calculations
4. **Localization**: Add new string keys to `MyBundle.properties` for any user-facing text

### Contributing Guidelines
- Keep bracket logic in `BracketColorService.kt`
- Keep coloring/rendering logic in `MatchingColoredBracketsAnnotator.kt`
- Add tests to `MyPluginTest.kt` for new features
- Update CHANGELOG.md with changes

