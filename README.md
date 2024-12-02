This is a Kotlin Multiplatform project for Coze API targeting Android, iOS.

# Folders explanation
* `/composeApp` is for code that will be shared across Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for API Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.
* `/iosApp` contains iOS applications. (This entry point is necessary for iOS app.) In this repo it contains the demo code to run(or test, anyway) Coze API in iOS Device.

# Project Dependencies
This project utilizes the [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) framework.
