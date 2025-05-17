This is a Kotlin Multiplatform application for Space Launch Now based off Launch Library data from The Space Devs.

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