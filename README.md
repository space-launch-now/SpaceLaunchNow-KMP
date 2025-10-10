This is a Kotlin Multiplatform application for Space Launch Now based off Launch Library data from The Space Devs.

## 🚀 CI/CD Pipeline

This project uses an automated CI/CD pipeline with:
- ✅ Automated version bumping via [Conventional Commits](https://www.conventionalcommits.org/)
- ✅ Automatic changelog generation
- ✅ Firebase Distribution for testers
- ✅ GitHub Releases with artifacts
- ✅ PR validation and testing

**Quick Start:**
- 📘 [CI/CD Quick Reference](docs/CICD_QUICK_REFERENCE.md) - Fast commands and examples
- 📝 [Conventional Commits Guide](docs/CONVENTIONAL_COMMITS.md) - How to write commit messages
- 📚 [Full CI/CD Documentation](docs/CICD_PIPELINE.md) - Complete pipeline details

**TL;DR:** Use `feat:` for features, `fix:` for bugs. Merges to master auto-deploy! 🎉

## 🧪 Testing

This project is actively building comprehensive unit test coverage:
- 🎯 [Testing Quick Reference](docs/TESTING_QUICK_REF.md) - Get started in 30 seconds
- 📋 [Testing Task List](docs/TESTING_TASKS.md) - Find tasks to work on
- 📖 [Testing Guide](docs/TESTING_GUIDE.md) - Detailed patterns and examples

**Want to contribute tests?** Check the task list for available work!

## Prerequisites

### Java 21 Requirement

This application requires **Java 21** to run. The Compose Hot Reload plugin specifically needs JetBrains JDK 21.

**Windows Setup:**
- If you have Android Studio installed, you can use the bundled JBR:
  ```
  JAVA_HOME=D:\tools\Android Studio Q4-2024\jbr
  ```
- Set the JAVA_HOME environment variable or use the Gradle property:
  ```
  ./gradlew :composeApp:desktopRun "-Dorg.gradle.java.home=D:\tools\Android Studio Q4-2024\jbr"
  ```

### VS Code Integration

The project includes VS Code tasks configured with the correct Java path:
- **Run Desktop App**: `Ctrl+Shift+P` → "Tasks: Run Task" → "Run Desktop App"
- **Build Desktop App**: Compile Kotlin for desktop
- **Clean Build**: Clean and rebuild the project
- **Regenerate API**: Regenerate API clients from OpenAPI spec

## Setup

### API Key Configuration

This application uses The Space Devs API which requires an API key. To set up your API key:

1. Create a `.env` file in the project root if it doesn't already exist
2. Add your API key to the file in the following format:
   ```
   API_KEY=your_api_key_here
   ```
3. The application will automatically load the API key from the `.env` file

Note: The `.env` file is ignored by Git to prevent exposing your API key in version control.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…