# Architecture Overview
This document serves as a critical, living template designed to equip agents with a rapid and comprehensive understanding of the SourceSync SDK UI Android codebase's architecture, enabling efficient navigation and effective contribution from day one.

## 1. Project Structure
This section provides a high-level overview of the project's directory and file structure, categorized by architectural layer and functional responsibilities for Android SDK UI components.

```
[SourceSync SDK UI Android Root]/
├── build.gradle.kts                     # Root project build configuration
├── demo-mobile/                         # Mobile demo application
│   ├── build.gradle.kts                # Mobile demo build configuration
│   ├── proguard-rules.pro              # ProGuard rules for demo app
│   └── src/                            # Demo app source code
│       ├── androidTest/                 # Android instrumentation tests
│       ├── main/                       # Main demo source
│       │   ├── assets/                 # Demo templates and resources
│       │   ├── java/io/sourcesync/sdk/ui/demo_mobile/
│       │   │   ├── ActivationViewLayout.kt  # Demo layout showcasing SDK
│       │   │   ├── MainActivity.kt          # Main demo activity
│       │   │   └── TemplateLoader.java      # JSON template loading utility
│       │   ├── res/                    # Android resources
│       │   └── AndroidManifest.xml     # Demo app manifest
│       └── test/                       # Unit tests
├── demo-tv/                             # Android TV demo application
│   └── build.gradle.kts                # TV demo build configuration
├── docs/                                # Project documentation
├── gradle/                              # Gradle wrapper and version catalogs
├── gradle.properties                    # Global project properties
├── gradlew                              # Gradle wrapper script (Unix)
├── gradlew.bat                          # Gradle wrapper script (Windows)
├── jitpack.yml                          # JitPack configuration for publishing
├── LICENSE                              # Project license file
├── local.properties                     # Local development properties
├── README.md                            # Project overview and integration guide
├── settings.gradle.kts                  # Multi-module project settings
└── sourcesync-sdk-ui/                   # Main SDK library module
    ├── build/                           # Build output directory
    ├── build.gradle.kts                 # Library build configuration
    ├── consumer-rules.pro               # ProGuard rules for consuming apps
    ├── proguard-rules.pro              # Internal ProGuard rules
    └── src/                            # Source code directory
        ├── androidTest/                 # Android instrumentation tests
        ├── main/                       # Main source code
        │   ├── AndroidManifest.xml     # Library manifest
        │   └── java/io/sourcesync/sdk/ui/
        │       ├── helpers/             # Utility and helper classes
        │       │   ├── CustomUrlHandler.kt    # DivKit URL action handler
        │       │   └── PicassoDivImageLoader.kt # Image loading integration
        │       ├── legacy_views/        # Deprecated/legacy view components
        │       ├── models/              # Data models and positioning
        │       │   └── ActivationPosition.kt # Positioning and alignment models
        │       ├── utils/               # Layout utilities and extensions
        │       │   └── LayoutUtils.kt          # View layout and cleanup utilities
        │       └── view/                # Core UI components and views
        │           ├── ActivationConfig.kt     # Configuration builder for activation views
        │           └── ActivationView.kt       # Unified activation display component
        └── test/                       # Unit tests directory
```

## 2. High-Level System Diagram
The SourceSync SDK UI Android provides UI components that integrate with the core SourceSync SDK to display interactive activation overlays within media applications.

```
[Media Player App] <--> [SourceSync SDK UI] <--> [KMP SourceSync Core SDK] <--> [SourceSync Backend]
                                |
                                +--> [DivKit Rendering] <--> [Picasso Image Loading]
                                |
                                +--> [Activation Templates (JSON)]
```

**Data Flow:**
1. Media app integrates SourceSync SDK UI components
2. SDK UI receives activation data from SourceSync Core SDK  
3. JSON templates are processed through DivKit for rendering
4. Images are loaded via Picasso integration
5. User interactions trigger callbacks to parent application

## 3. Core Components

### 3.1. UI Components Layer

#### 3.1.1. ActivationView
**Name:** Unified Activation Display Component

**Description:** Central UI component that renders both preview and detail modes for activations. Replaces separate preview/details components with a single configurable view. Handles DivKit integration, touch events, and lifecycle management.

**Technologies:** Kotlin, DivKit, Android Views, Picasso

**Key Features:**
- Unified preview/detail display modes
- Configurable positioning and alignment  
- Outside click detection with video control preservation
- Proper resource cleanup and memory management

#### 3.1.2. ActivationConfig
**Name:** Configuration Builder for Activation Views

**Description:** Builder pattern implementation for configuring ActivationView behavior, positioning, handlers, and DivKit integration. Provides fluent interface for SDK consumers to customize activation display.

**Technologies:** Kotlin Builder Pattern, DivKit Configuration

**Key Features:**
- Fluent configuration API
- Screen metrics integration
- URL handler configuration
- Visual error toggle for debugging

### 3.2. Helper Components

#### 3.2.1. CustomUrlHandler
**Name:** DivKit URL Action Handler

**Description:** Comprehensive URL handler for DivKit that processes close actions, external URLs, custom schemes, and deep links. Integrates with Android system intents and provides callback mechanisms.

**Technologies:** DivKit ActionHandler, Android Intents, URI Processing

**Key Features:**
- Multi-protocol URL handling (http/https, mailto, tel, sms)
- Custom scheme processing
- System app integration with fallback handling
- Comprehensive error handling and logging

#### 3.2.2. PicassoDivImageLoader  
**Name:** Picasso-DivKit Image Loading Bridge

**Description:** Custom DivKit image loader implementation using Picasso for efficient image loading, caching, and memory management. Provides both callback-based and ImageView-direct loading methods.

**Technologies:** Picasso, OkHttp, DivKit ImageLoader Interface

**Key Features:**
- Disk and memory caching
- Target lifecycle management
- Coroutine-based byte array loading
- Thread-safe target tracking

### 3.3. Model Layer

#### 3.3.1. ActivationPosition
**Name:** Positioning and Alignment Models

**Description:** Data models defining activation positioning within screen boundaries. Includes alignment enums and utility methods for converting string representations to typed enums.

**Technologies:** Kotlin Data Classes, Enums

**Key Features:**
- Screen dimension integration
- Horizontal/vertical alignment options
- String-to-enum conversion utilities
- Default fallback behavior

### 3.4. Utility Layer

#### 3.4.1. LayoutUtils
**Name:** Layout Operations and View Management

**Description:** Utility object providing layout parameter calculations, view cleanup operations, and DivKit integration helpers. Handles safe resource cleanup and percentage-based sizing.

**Technologies:** Android Layout Management, DivKit Parsing, View Hierarchy

**Key Features:**
- JSON to DivData parsing
- Recursive RecyclerView cleanup
- Percentage-based layout parameters
- Safe cleanup with error handling

### 3.5. Demo Components

#### 3.5.1. ActivationViewLayout (Demo)
**Name:** Demo Layout Showcasing SDK Integration

**Description:** Demo layout demonstrating ActivationView integration with timer-based activation display. Shows preview-to-details interaction pattern for SDK evaluation.

**Technologies:** Kotlin, Android RelativeLayout, CountDownTimer

**Key Features:**
- 30-second timer-based demo flow
- Preview to details transition demonstration
- Template loading from assets
- Resource cleanup and lifecycle management

#### 3.5.2. TemplateLoader (Demo)
**Name:** JSON Template Loading Utility

**Description:** Utility class for loading JSON templates from Android assets for demo purposes. Handles file reading and JSON parsing for activation templates.

**Technologies:** Java, Android Assets, JSON Processing

**Key Features:**
- Asset-based template loading
- JSON parsing and validation
- Error handling for missing templates

## 4. Data Stores

### 4.1. Template Storage
**Type:** JSON Assets / External API

**Purpose:** Stores activation templates with card and template definitions for DivKit rendering

**Key Schemas:** 
- preview templates (card + templates objects)
- details templates (expanded content definitions)

### 4.2. Image Cache
**Type:** Picasso Disk/Memory Cache

**Purpose:** Caches loaded images to improve performance and reduce network requests

**Storage Location:** Android application cache directory

## 5. External Integrations / APIs

### 5.1. DivKit Framework
**Purpose:** UI rendering engine for JSON-based layouts
**Integration Method:** Direct library integration with custom components

### 5.2. Picasso Image Loading
**Purpose:** Efficient image loading and caching
**Integration Method:** Custom DivImageLoader implementation

### 5.3. KMP SourceSync Core SDK
**Purpose:** Activation data management and business logic
**Integration Method:** Direct dependency and callback interfaces

### 5.4. System Intents (Android)
**Purpose:** External URL handling (browser, email, phone, SMS)
**Integration Method:** Android Intent system with package manager resolution

## 6. Deployment & Infrastructure

**Distribution Method:** Maven Central Repository / JitPack

**Build System:** Gradle with Kotlin DSL

**CI/CD Pipeline:** GitHub Actions (inferred from .github/ directory)

**Publishing:** Sonatype Maven Central with signing + JitPack alternative

**Demo Apps:** Separate mobile and TV demonstration applications

## 7. Security Considerations

**URL Validation:** Comprehensive URI parsing with error handling to prevent malformed URL crashes

**Intent Resolution:** System app verification before launching external applications

**ProGuard Rules:** Consumer rules provided to prevent obfuscation of public APIs

**Resource Cleanup:** Proper lifecycle management to prevent memory leaks and resource retention

## 8. Development & Testing Environment

**Local Setup:** Multi-module Gradle project with version catalogs

**Testing Frameworks:** Kotlin Test, JUnit, MockK (inferred from gradle structure)

**Code Quality:** ProGuard rules for library optimization

**Demo Integration:** Standalone demo applications for testing and showcase

**Mixed Language Support:** Both Kotlin and Java source files supported

## 9. Future Considerations / Roadmap

### 9.1. Architectural Improvements
- **Compose Migration:** Consider migration from Android Views to Jetpack Compose for modern UI development
- **Coroutine Integration:** Enhanced async operations with structured concurrency
- **Modularization:** Further separation of concerns with feature-based modules

### 9.2. Performance Optimizations
- **Image Loading:** Investigate WebP support and adaptive image sizing
- **Memory Management:** Enhanced target lifecycle management in PicassoDivImageLoader
- **View Recycling:** RecyclerView integration for list-based activations

### 9.3. API Surface Improvements
- **Kotlin Multiplatform:** Potential alignment with KMP patterns for cross-platform consistency
- **DSL Enhancements:** More Kotlin-idiomatic configuration APIs
- **Testing Utilities:** Provided testing helpers for SDK consumers

### 9.4. Legacy Migration
- **API Consolidation:** Remove deprecated APIs and streamline public interface
- **Documentation Updates:** Ensure all legacy patterns are updated in documentation

## 10. Repository Relationships

### 10.1. KMP SourceSync Core SDK
**Relationship:** Direct dependency providing activation data and business logic

**Integration Points:**
- Activation data models and callback interfaces
- Event tracking and analytics integration
- Content management and timing logic

**Dependency Direction:** SDK UI depends on Core SDK

### 10.2. iOS SDK Equivalent
**Relationship:** Parallel implementation with similar architecture patterns

**Shared Concepts:**
- ActivationView unified component approach
- Configuration builder patterns
- URL handling and external integrations
- Template-based rendering (DivKit on both platforms)

**Platform Differences:**
- iOS uses DivKit for iOS, Android uses DivKit for Android
- iOS uses different image loading libraries
- Platform-specific UI frameworks (UIKit vs Android Views)

### 10.3. Cross-Platform Considerations
**Alignment Goals:**
- Consistent API surface between iOS and Android SDKs
- Shared activation template formats
- Common configuration patterns and naming conventions

**Platform Adaptations:**
- Android-specific Intent handling vs iOS URL schemes
- Different lifecycle management approaches
- Platform-specific UI positioning and constraints

## 11. Project Identification

**Project Name:** SourceSync SDK UI Android

**Primary Contact/Team:** Source Digital Development Team


## 12. Glossary / Acronyms

**DivKit:** Yandex's declarative UI framework for rendering JSON-based layouts

**KMP:** Kotlin Multiplatform - shared code approach across platforms

**Activation:** Interactive content overlay displayed during media playback

**Preview Mode:** Initial activation display encouraging user interaction

**Details Mode:** Expanded activation content shown after user engagement

**Template:** JSON structure defining activation appearance and behavior

**Target:** Picasso interface for handling image loading callbacks and lifecycle

**ProGuard:** Android code obfuscation and optimization tool

**JitPack:** Alternative Maven repository for GitHub-hosted projects