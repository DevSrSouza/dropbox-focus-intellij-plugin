<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# dropbox-focus-intellij-plugin Changelog

## [Unreleased]

## [0.2.2]
Added
- Adding support to IDEA 231.* ( IntelliJ 2023.1 and Android Studio Hedgehog Canary 2023.1)
- Adding support for Settings block with Varargs include, example: `include(":moduleA", ":moduleB")`

## [0.2.1]
### Added
- Adding support to IDEA 222.* and 223.* ( IntelliJ 2022.3 EAP and Android Studio Flamingo 2022.2)

## [0.2.0]
### Added
- Adding a clear focus button to side tool window
- Adding clear focus action (SHIFT+SHIFT "Clear Focus")

### Fixed
- Gradle sync not being triggered in Android Studio 2022.1 Canary 1

## [0.1.1]
### Changed
- Fix IntelliJ missing dependency on com.intellij.modules.java

## [0.1.0]
### Added
- Startup Dialog for Focus projects
- Module folder right-click action to "Focus on Module" with Sync
- Root project folder rick-click action to "Clear focus" with Sync
- Focus Tool Window to Select Module to Focus
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)