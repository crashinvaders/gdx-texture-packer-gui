### 4.12.0

- Headless batch processing mode support. Read more https://github.com/crashinvaders/gdx-texture-packer-gui#command-line
- Option to select multiple atlases for processing. New action - `packMultipleAtlases`. See [#67](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/67)
- Atlases use new libGDX atlas output format by default ("Legacy format" is unchecked).
- Add "show in file manager" contextual options.
- Basis Universal updated to 1.16.4 and is nativelly supported on Linux arm32/arm64 (Raspberry Pi) and macOS arm64 (M1/M2 chips).
- Fixed file conflict errors when attempting to install from RPM package. See [#140](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/140)
- Fixed a crash on a project save when the output dir is set to any ascendant parent dir of the project containing folder. See [#126](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/126)
- Fixed grid layout adds redundant top padding.

### 4.11.0

- Use OS native file dialogs (instead of cross-platform unified VisUI FileChooser).
- Brand new settings dialog uniting several specific feature configuration dialogs. Also includes more friendly tools to deal with custom hotkeys.
- Explicit packing warning notifications. See [#131](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/131)
- Simplified Chinese localization. Thanks to @ZhReimu. See [#127](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/127)
- Fixed pack clone dialog OK button crashes the app. See [#130](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/130)
- Fixed "Legacy format" and "Pretty print" pack settings are not getting saved. Thanks to @Frosty-J. See [#132](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/132)
- Fixed changing the app language to Chinese (either simplified of traditional has no effect. Thanks to @Frosty-J. See [#133](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/133))
- Fixed anim8-gdx png compression always uses "Scatter" dithering algorithm and ignores the user selected one.

### 4.10.2

- Fixed cannot change current atlas by clicking on pack list items. See [#122](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/122)
- Fixed ninepatch images (`.9.png` extension) are being ignored and packed as plain regions.
- Fixed pixel interpolation type is ignored for scale factors less than one.
- Possible fix for the "No default view controller found" rare crash during application startup. See [#92](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/92)

### 4.10.1

- Fixed file path editing in FileChooser may result in a crash. See [#98](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/98)
- Support for macOS arm64 (M1 chip). See [#110](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/110)
- Option to append input file extensions to region names. See [#108](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/108)
- Window validates and fixes its dimensions on startup. See [#113](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/113)
- Fixed rare crash when the downloaded extension cache index is malformed. See [#100](https://github.com/crashinvaders/gdx-texture-packer-gui/issues/100)
- Minor UI improvements and some essential call to action animations for newcomers.
- Smaller OS-specific distribution size (due to natives strip).

### 4.10.0

- Support for Basis Universal super-compressed atlas textures. See https://github.com/BinomialLLC/basis_universal
- Support for the new (libGDX 1.9.13) texture atlas format. Which outputs much smaller data files.
- The app now forbids writing output atlas files to any directory matching the input files/dirs to avoid accidental source image files overwrite.
- Old PNG8 compression is replaced with anim8-gdx implementation. See https://github.com/tommyettinger/anim8-gdx
- General project save/load errors now gracefully handled and a crash log is available.
- Fixed the main application window sometimes doesn't appear on startup on macOS devices.
- Fixed KTX(ETC1/ETC2) compression fails with NPE.
- Fixed crash when a project fails to load on startup.
- Fixed the input file absolute path sometimes is displayed wrong when the project is saved to a deeper directory hierarchy than the source images.
- Fixed the app freezes during the packing process on macOS.

### 4.9.0

- JRE 8+ is required to run the app.
- Migrated to LWJGL3 desktop backend. Expect lots of compatibility fixes.
- Simplified drag-n-drop. No visible overlay during the file drag action anymore.
- Pngquant (Imagequant) added as a PNG compression option.
- Show atlas region size on preview hover.
- Highlight a related atlas region on input file list mouse hover.
- "startOnFirstThread" JVM argument is no longer required on macOS.
- Linux DEB & RPM distribution packages.
- Fixed high DPI scaling issue.
- Fixed file based 9-patch pad values get displayed wrong (in case they are not defined explicitly in the input image).
- Fixed startup crash when an installed module's local files deleted on the file system.

### 4.8.1

- Fixed texture regions get written with wrong padding.
- Fixed texture unpacker extracts regions with wrong sizes occasionally.

### 4.8.0

- Atlases can be saved as indexed-mode PNG8 image (compression type). Optionally dithering colors that don't match exactly.
- Asynchronous (non blocking) page preview loading.
- Multiple of four page size option added.
- Menu bar items (File, Pack, Tools, Help) have got appropriate key shortcuts (ALT+F/P/T/H).
- Fixed new version notification occasionally leads to a crash.
- Fixed programmatic 9-patch pad values are omitted if they are all zero.
- Fixed 9-patch editor crashes when all patch/content points at the same corner.
- Fixed 9-patch editor writes file based images ("*.9.png" extension) with black stroke visual artifacts.
- Fixed global settings section values get reset upon UI reload (e.g. language change).
- Fixed global keyboard shortcuts no longer works when there is a modal dialog shown.
- Fixed exclude files do not work when are under input dir with "recursive" option enabled.

### 4.7.3

- Fixed launching of nine patch editor leads to crash.
- Fixed packing of multiple atlases occasionally fails.
- Fixed some UI elements don't respond to a mouse button click.
- GitHub error reporting workflow improved with better API integration.

### 4.7.2

- Fixed wrong page size when padding is not zero (regression bug).
- Spinner widgets replace with seek bars (where suitable).
- UI texture/font filtering changed to linear/linear.

### 4.7.1

- Fixed wrong page size when padding is not zero.

### 4.7.0

- Save prompt before project closing if any changes are detected.
- Scaling resampling option per scale factor entry.
- If output directory doesn't exist, it will be created automatically during packing.
- Interface scaling option (check Tools -> Interface scaling).
- Support for user defined hotkeys (check Tools -> Customize hotkeys).
- Visual indication for newly added pack files.
- Memory consumption optimized run configuration. The app will never exceed 1GB of RAM and free up memory faster.
- Application crashes get captured and reported directly to GitHub issues.
- Fixed crash when input text contains square brackets.
- Fixed memory leak with the new TTF fonts.
- Fixed shortcut keys do not function when an input field is focused.
- Fixed crash when a pack file no longer exists during packing or properties dialog.

### 4.6.0

- Application eats much less system CPU resources when idle.
- 9-patch editor improvements (preview pane, value number fields).
- Support for CJK characters (downloadable extension module).
- Traditional Chinese localization, thanks to Tokenyet.
- Log output into external files (easy to grab and send with a report).
- Fixed KTX ETC2 compression fails to write page files to a directory with spaces.

### 4.5.0

- Page image file type configuration. ETC(KTX) file format support.
- Nine patch editor. Nine patches can be created from a plain images on the fly.
- Recursive and flatten paths flags for an input directory.
- "Grid layout" algorithm now sorts regions and packs them in a name/index order.
- Fixed "Use aliases" flag is missing in a save file.
- Fixed old pack data file does not get replaced with a new one properly, when custom extension is being used.
- Fixed crash when project contains input dir that is a parent of the project save file itself.

### 4.4.0

- Each pack now consists of list of input/ignore files (no more single source dir).
- File drag'n'drop support (image files and project file).
- Fixed occasional output image file locking after Pngtastic compression.
- .tpproj file extension association for Windows (if program installed through installer).

### 4.3.2

- German localization added, thanks to TeraFog.
- Fixed occasional crash upon language change.
- Fixed "Flatten paths" checkbox (it works now!).
- Fixed no window on application startup in some cases.
- macOS distribution configuration.

### 4.3.1

- Input/output paths that are equal to project file dir, now correctly save and load.
- Minor layout and localization text fixes.

### 4.3.0

- "bleed", "limitMemory" and "flattenPaths" flags were added to pack settings.
- Full set of tooltips for all pack setting properties.
- Russian localization added.
- Common UI improvements and some new inspection data is shown for packing and atlas preview.

### 4.2.0

- Support of TinyPNG/TinyJPG compression service, see https://tinypng.com
- Export with multiple scale factors.
- Atlas preview background color can be changed.
- Double click on pack list triggers packing procedure.
- Fixed: relative file paths now correctly saved in UNIX format.

### 4.1.0

- Parallel processing for atlas packing.
- Compression option for png atlases, see https://github.com/depsypher/pngtastic
- Texture unpacker added under tools section.
- "square" and "grid" pack setting added.
- Option to choose position for newly created and copied packs.

### 4.0.0

- App rewritten from the scratch.
- New reworked UI based on VisUI and LML.
- All reported bugs (for 3.2.0) were fixed, see https://github.com/aurelienRibonBackup/libgdx-texturepacker-gui/issues
- New atlas preview widget, now with basic inspection capabilities.

### 3.2.0

- Last Aurelien Ribon's release (2012).
