# File Browser Module

## Overview

A comprehensive Android Compose file browser component for the WanBaoHe project. This module provides a fully-featured file browsing experience with Material 3 design, high performance, and extensive customization options.

## Features

### Core Functionality
- **Dual Launch Modes:**
  - Navigate to a specific file/folder via URI (supports both `content://` and `file://` schemes)
  - Start from system root/primary storage when no URI is provided
  - Automatic highlight of target file when navigated via URI

- **File Operations:**
  - Browse files and directories
  - Navigate into subdirectories
  - Navigate back to parent directories
  - File click callbacks for custom handling
  - Refresh current directory

- **Sorting Options:**
  - Sort by name (alphabetically)
  - Sort by modification date
  - Sort by file size
  - Sort by file type
  - Toggle between ascending and descending order
  - Directories always appear before files

- **File Information Display:**
  - File/folder name
  - File size (formatted: B, KB, MB, GB)
  - Last modified date and time
  - Type-specific icons
  - Highlighted files for navigation targets

### Architecture

The module follows clean architecture principles with clear separation of concerns:

```
feature/file-browser/
├── model/               # Data models
│   ├── FileItem.kt     # File representation
│   ├── SortConfig.kt   # Sort configuration
│   └── FileBrowserState.kt  # UI state
├── screenLogic/        # Business logic
│   └── FileBrowserComponent.kt  # Component (ViewModel equivalent)
├── screen/             # UI screens
│   └── FileBrowserScreen.kt  # Main screen
├── ui/                 # Reusable UI components
│   ├── FileListItem.kt
│   ├── FileList.kt
│   ├── EmptyStates.kt
│   └── SortMenu.kt
└── utils/              # Utility classes
    ├── FileHelper.kt   # File operations
    └── FileSorter.kt   # Sorting logic
```

### Design Principles

#### 1. Fine-Grained Components
Each UI component is designed to be:
- **Single Responsibility:** Each component does one thing well
- **Reusable:** Components can be used independently
- **Composable:** Small components combine to build complex UI
- **Well-documented:** Comprehensive KDoc comments

#### 2. Material 3 Design
- Uses Material 3 color scheme
- Follows Material 3 typography system
- Implements Material 3 shapes and spacing
- Flat design without heavy shadows
- Proper elevation hierarchy

#### 3. Performance Optimization
- **Lazy Loading:** Uses `LazyColumn` for efficient scrolling
- **Async Operations:** All I/O on background threads
- **State Management:** Efficient state updates with Kotlin Flow
- **Item Keys:** Proper keys for list items to optimize recomposition
- **Image Loading:** Lazy loading with Coil integration ready

#### 4. Responsive Design
- **Portrait/Landscape:** Adapts to screen orientation
- **Different Screen Sizes:** Adjusts padding and layout
- **Large Screens:** Ready for tablet/foldable optimization
- **RTL Support:** Properly handles right-to-left layouts

### State Management

The module uses a sealed interface for state management:

```kotlin
sealed interface FileBrowserState {
    data object Idle          // Initial state
    data object Loading       // Loading files
    data class Success        // Files loaded successfully
    data class Empty          // Directory is empty
    data class Error          // Error occurred
    data object NoPermission  // No storage permission
}
```

### Usage

#### Basic Usage

```kotlin
// Create component
val component = fileBrowserComponentFactory(
    componentContext = componentContext,
    initialUri = null,  // Start from default directory
    onFileClick = { fileItem ->
        // Handle file click
        Log.d("FileBrowser", "Clicked: ${fileItem.name}")
    }
)

// Show screen
FileBrowserScreen(
    component = component,
    onGoBack = { /* Navigate back */ }
)
```

#### Navigate to Specific File

```kotlin
val fileUri = Uri.parse("content://com.android.providers.downloads.documents/document/798")

val component = fileBrowserComponentFactory(
    componentContext = componentContext,
    initialUri = fileUri,  // Navigate to this file
    onFileClick = { fileItem ->
        // Handle file click
    }
)
```

### Internationalization

All user-facing strings are externalized to `strings.xml`:
- English strings provided
- Ready for localization
- RTL layout support built-in

### Key Components

#### FileBrowserComponent
The brain of the module, managing:
- State management with Kotlin Flow
- File loading from URIs
- Navigation stack
- Sorting configuration
- File highlighting

#### FileBrowserScreen
Main screen composable featuring:
- Top app bar with path breadcrumb
- Sort menu for file ordering
- Content area with state-based rendering
- Back handling integration

#### FileList
Optimized list component with:
- Lazy loading for performance
- Auto-scroll to highlighted items
- Animation support
- Proper item keys for optimization

#### FileListItem
Individual file item displaying:
- File/folder icon
- File name
- Size and modification date
- Highlight support

### Error Handling

Comprehensive error states:
- **Empty Folder:** Friendly message when no files
- **Loading Error:** Retry option for failed loads
- **No Permission:** Clear message about storage access
- **Invalid URI:** Graceful fallback to default directory

### Dependencies

- **Decompose:** Component-based navigation
- **Hilt:** Dependency injection
- **Coroutines:** Async operations
- **Coil:** Image loading (integration ready)
- **Material 3:** UI components
- **Compose:** UI framework

### Testing Considerations

The architecture supports testing:
- **Component:** Business logic testable in isolation
- **FileHelper:** File operations with dependency injection
- **FileSorter:** Pure functions, easy to test
- **State:** Predictable state transitions

### Performance Tips

1. **Large Directories:** Efficiently handles thousands of files with lazy loading
2. **Memory:** No memory leaks, proper lifecycle handling
3. **Threading:** All I/O operations on background threads
4. **Recomposition:** Minimized with proper state management

### Future Enhancements

Potential additions:
- File selection (single/multiple)
- File operations (copy, move, delete)
- Search functionality
- File filters by type
- Thumbnails for images/videos
- Breadcrumb navigation
- Grid view option
- Favorites/bookmarks

### License

Part of the WanBaoHe project.

### Contributing

Follow the project's coding standards:
- Material 3 design guidelines
- KDoc comments for all public APIs
- Error handling for all operations
- Internationalized strings
- Performance considerations

