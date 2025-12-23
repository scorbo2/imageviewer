# ImageViewer - Copilot Instructions

## Project Overview

**ImageViewer** is a 100% Java desktop image viewer application with a UI written in Java Swing. It is an extensible, highly configurable image browsing and management tool that allows users to sort, rename, move, and symlink images quickly. The application supports a powerful extension mechanism for adding custom functionality.

- **Repository**: https://github.com/scorbo2/imageviewer
- **Version**: 2.3 is in production 2.4 is in development
- **License**: MIT
- **Language**: Java 17+
- **Build System**: Maven 3.9+
- **Framework**: Java Swing for UI
- **Size**: ~28MB (includes dependencies), 75 Java source files, ~15,000 LOC
- **Main Entry Point**: `ca.corbett.imageviewer.Main`

## Build & Test Instructions

### Prerequisites
- **Java**: Java 17 or higher (tested with OpenJDK 17.0.17)
- **Maven**: Apache Maven 3.9+ (tested with 3.9.11)

### Building the Project

**ALWAYS run commands in this exact order:**

1. **Clean the project** (optional but recommended after major changes):
   ```bash
   mvn clean
   ```
   - Takes ~3-5 seconds
   - Removes the `target/` directory

2. **Compile the code**:
   ```bash
   mvn compile
   ```
   - Takes ~1-2 seconds on incremental builds
   - Takes ~10-15 seconds on first build (downloads dependencies)

3. **Run tests**:
   ```bash
   mvn test
   ```
   - Takes ~1-2 seconds
   - Runs 36 unit tests (as of v2.3)
   - All tests should pass
   - Tests are in: `src/test/java/ca/corbett/imageviewer/ui/imagesets/`

4. **Build the complete package** (includes compile, test, jar, javadoc, and sources):
   ```bash
   mvn package
   ```
   - Takes ~30-40 seconds on clean build
   - Takes ~2-5 seconds on incremental build
   - Produces:
     - `target/imageviewer-2.4-SNAPSHOT.jar` (~308KB) - Main application JAR
     - `target/imageviewer-2.4-SNAPSHOT-sources.jar` (~205KB) - Source JAR
     - `target/imageviewer-2.4-SNAPSHOT-javadoc.jar` (~617KB) - Javadoc JAR
     - `target/lib/` - Directory with all dependencies

5. **Run the application**:
   ```bash
   cd target
   java -jar imageviewer-2.4-SNAPSHOT.jar
   ```
   - Optional: Add `-v` flag to display version and exit
   - Optional: Add directory path as argument to start in that directory

### Important Build Notes

- **Always run `mvn clean` before `mvn package`** if you encounter any unexpected build issues or after changing dependencies
- Dependencies are automatically downloaded from Maven Central on first build
- The build generates three JARs: main, sources, and javadoc
- Javadoc warnings are suppressed with `-Xdoclint:all -Xdoclint:-missing`
- If running on Linux with [install-scripts](https://github.com/scorbo2/install-scripts) installed, an installer tarball will be automatically generated in the `target/` directory via the `make-installer` profile

### Common Build Issues

- **Issue**: `mvn package` fails with dependency resolution errors
  - **Solution**: Run `mvn clean` first, then retry `mvn package`
  
- **Issue**: Tests fail unexpectedly
  - **Solution**: Check that Java 17+ is being used: `java --version`
  
- **Issue**: Out of date classes
  - **Solution**: Run `mvn clean compile` to force recompilation

## Project Structure

### Root Directory Files
```
imageviewer/
├── pom.xml                      # Maven project configuration
├── README.md                    # User-facing documentation
├── LICENSE                      # MIT License
├── .editorconfig                # Editor formatting rules (4 spaces, UTF-8)
├── .gitignore                   # Git ignore patterns
├── installer.props              # Configuration for make-installer script
├── update_sources.json          # Auto-update configuration
├── imageviewer.jpg              # Screenshot for README
├── extension_manager.jpg        # Screenshot for README
└── src/                         # Source code
```

### Source Directory Structure
```
src/
├── main/
│   ├── java/ca/corbett/imageviewer/
│   │   ├── Main.java                           # Application entry point
│   │   ├── Version.java                        # Version constants
│   │   ├── ImageOperation.java                 # Image operation types
│   │   ├── ImageOperationHandler.java          # Image operation execution
│   │   ├── extensions/
│   │   │   ├── ImageViewerExtension.java       # Extension base class (KEY FILE)
│   │   │   ├── ImageViewerExtensionManager.java # Extension management
│   │   │   └── builtin/                        # Built-in extensions
│   │   └── ui/
│   │       ├── MainWindow.java                 # Main application window
│   │       ├── ThumbPanel.java                 # Thumbnail display
│   │       ├── ThumbContainerPanel.java        # Thumbnail container
│   │       ├── ImageInstance.java              # Image data model
│   │       ├── actions/                        # UI actions
│   │       ├── dialogs/                        # Dialog windows
│   │       ├── imagesets/                      # Image set management
│   │       ├── layout/                         # Layout managers
│   │       └── threads/                        # Background threads
│   └── resources/ca/corbett/imageviewer/
│       ├── ReleaseNotes.txt                    # Version history
│       ├── images/                             # Application icons and images
│       └── logging.properties                  # Logging configuration
└── test/
    └── java/ca/corbett/imageviewer/ui/imagesets/
        ├── ImageSetTest.java                   # Image set unit tests
        ├── ImageSetManagerTest.java            # Manager unit tests
        └── ImageSetPanelTest.java              # Panel unit tests
```

### Key Dependencies (from pom.xml)
- **swing-extras 2.6.0-SNAPSHOT**: Custom Swing components (ca.corbett)
- **jackson-databind 2.15.2**: JSON processing
- **commons-io 2.19.0**: File I/O utilities
- **sqlite-jdbc 3.41.2.2**: SQLite database support
- **junit-jupiter-engine 5.12.1**: Unit testing (test scope)

### Configuration Files
- **pom.xml**: Maven build configuration
  - Java source/target: 17
  - UTF-8 encoding
  - Maven plugins: compiler, source, javadoc, dependency, jar
  - Main class: `ca.corbett.imageviewer.Main`
  
- **.editorconfig**: Code formatting rules
  - Indent: 4 spaces
  - Charset: UTF-8
  - Line endings: LF
  - Max line length: 120
  - Java-specific formatting rules (extensive)

- **.gitignore**: Excludes `target/`, IDE files (.idea/, .vscode/, etc.), and OS-specific files

## Architecture

### Application Flow
1. `Main.main()` initializes logging and parses command-line arguments
2. Look and Feel is loaded via `LookAndFeelManager`
3. `ImageViewerExtensionManager` loads extensions from extensions directory
4. `MainWindow` is created and displayed
5. User can browse filesystem or image sets
6. Extensions can hook into various lifecycle points

### Extension System
- Extensions extend `ImageViewerExtension` (abstract base class)
- Placed in JAR files in the extensions directory
- Can provide:
  - Custom thumbnails
  - Menu items and actions
  - Extra UI panels
  - Toolbar buttons
  - Image operation hooks
- Extension manager handles discovery and lifecycle

### Key Extension Points
- `getThumbnail()`: Custom thumbnail generation
- `getMenuItems()`: Add menu items to File/Edit/View/Help menus
- `getCustomMenus()`: Add custom top-level menus
- `getExtraPanel()`: Add panels to main window
- `getImageTabToolBarButtons()`: Add toolbar buttons
- Various lifecycle hooks (startup, shutdown, image operations, etc.)

### Browse Modes
The application supports two browse modes:
1. **FileSystem**: Browse images from filesystem directories
2. **ImageSet**: Browse images from custom-defined image sets (collections)

UI and available actions change based on the current browse mode.

## Coding Conventions

### Code Style (from .editorconfig)
- **Indentation**: 4 spaces (no tabs)
- **Line endings**: LF (Unix-style)
- **Encoding**: UTF-8
- **Max line length**: 120 characters
- **Brace style**: End of line (K&R style)
- **Import organization**: @*, *, |, javax.**, java.**, |, $*
- **Always use braces** for if/while/for statements
- **Javadoc**: Required for public APIs, using `-Xdoclint:all -Xdoclint:-missing`

### Common Patterns
- Use Java Swing for all UI components
- Follow standard Java naming conventions
- Extensions are discovered dynamically via classpath scanning
- Logging uses `java.util.logging` (JUL)
- TODO comments exist in codebase but are not blockers

## Testing

### Running Tests
```bash
mvn test
```

### Test Suite
- **Location**: `src/test/java/ca/corbett/imageviewer/ui/imagesets/`
- **Framework**: JUnit Jupiter (JUnit 5)
- **Test Count**: 36 tests (as of v2.3)
- **Coverage**: Focuses on image set management functionality
- **All tests should pass** - if any fail, investigate before committing

### Test Classes
1. `ImageSetTest.java`: Tests for ImageSet data model (11 tests)
2. `ImageSetManagerTest.java`: Tests for ImageSetManager operations (25 tests)
3. `ImageSetPanelTest.java`: Tests for ImageSetPanel UI component

## Validation Checklist

Before completing any code changes, ensure:

1. ✅ Code compiles without errors: `mvn compile`
2. ✅ All tests pass: `mvn test`
3. ✅ Package builds successfully: `mvn package`
4. ✅ Code follows .editorconfig formatting rules
5. ✅ No new warnings in Javadoc generation
6. ✅ New public APIs have Javadoc comments
7. ✅ Changes are compatible with Java 17+

## Additional Notes

### Extension Development
If adding or modifying extensions:
- Review `ImageViewerExtension.java` Javadoc thoroughly
- Extensions must be packaged as JAR files
- Test extension loading from the extension manager dialog
- Available extensions listed at: http://www.corbett.ca/apps/ImageViewer/

### No CI/CD
This repository does not have GitHub Actions or other CI/CD pipelines configured. All validation must be done locally using Maven commands.

### Installer Generation
On Linux systems with [install-scripts](https://github.com/scorbo2/install-scripts) installed, `mvn package` will automatically generate an installer tarball using `installer.props` configuration.

---

**Trust these instructions.** Only perform additional searches if information here is incomplete or incorrect. This file was validated against actual build runs and comprehensive repository exploration.
