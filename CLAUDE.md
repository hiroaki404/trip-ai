# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform project targeting Android and Desktop (JVM) using Compose Multiplatform. The application uses a shared codebase architecture where common UI and business logic reside in `commonMain`, while platform-specific implementations use the expect/actual pattern.

## Essential Commands

### Build
```bash
# Build entire project
./gradlew :composeApp:build

# Android Debug APK
./gradlew :composeApp:assembleDebug

# Desktop (JVM) JAR
./gradlew :composeApp:jvmJar
```

### Run
```bash
# Run Desktop application
./gradlew :composeApp:run

# Install on Android device/emulator
./gradlew :composeApp:installDebug
```

### Test
```bash
# Run all tests across platforms
./gradlew :composeApp:allTests

# Run checks (includes tests)
./gradlew :composeApp:check

# Run JVM tests only
./gradlew :composeApp:jvmTest
```

### Clean
```bash
./gradlew :composeApp:clean
```

## Architecture

### Multiplatform Structure
- **composeApp/src/commonMain**: Shared code for all platforms
  - Compose UI definitions using Material 3
  - Business logic and data models
  - Shared resources in `composeResources/`

- **composeApp/src/androidMain**: Android-specific implementations

- **composeApp/src/jvmMain**: Desktop-specific implementations
  - Entry point: `main.kt` with `MainKt` as main class

### Platform Abstraction Pattern
Use expect/actual pattern for platform-specific functionality:
- Define `expect` declarations in `commonMain/Platform.kt`
- Implement `actual` declarations in platform-specific modules (e.g., `Platform.jvm.kt`)

### Dependency Management
All library versions are managed in `gradle/libs.versions.toml` using Gradle Version Catalogs. Reference dependencies using `libs.*` notation in build files.

### Key Technologies
- Kotlin 2.2.20 (JVM Target: 11)
- Compose Multiplatform 1.9.0
- Material 3 for design system
- Kotlinx Coroutines 1.10.2

## Code Conventions

### Compose UI
- All Composable functions use `@Composable` annotation
- Use `@Preview` for preview-able components
- Wrap root Composables with `MaterialTheme`
- Apply `safeContentPadding()` modifier for safe area handling

### State Management
Use `remember { mutableStateOf() }` pattern with `by` delegate:
```kotlin
var state by remember { mutableStateOf(initialValue) }
```

### Resources
Access shared resources via generated code:
```kotlin
import trip_ai.composeapp.generated.resources.Res
import trip_ai.composeapp.generated.resources.resource_name
```

## Development Workflow

When implementing new features:
1. Add shared logic/UI in `commonMain`
2. Use expect/actual for platform-specific APIs
3. Build for both platforms to verify compatibility
4. Run tests: `./gradlew :composeApp:allTests`
5. Test both platforms: `./gradlew :composeApp:run` and Android device/emulator