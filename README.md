# Dropbox Focus IntelliJ Plugin [WIP]

![Build](https://github.com/DevSrSouza/dropbox-focus-intellij-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->
This IntelliJ and Android Studio Plugin provide tooling for Gradle Plugin [Focus by Dropbox](https://github.com/dropbox/focus).
The reason is to make it easy to switch focus and preventing open a big project without giving you the option to only select the module that you want to work on.

- When plugin detect that the project that is being open was Focus plugin, it will prompt a Dialog to Select if you want to select a Module to Focus, Clear current Focus or just leave as it is.
- Focus on a Module by right-clicking on a Module folder and selecting "Focus on Module"
- Clearing current focus by right-clicking on root project folder
 
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Dropbox Focus"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/DevSrSouza/dropbox-focus-intellij-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
