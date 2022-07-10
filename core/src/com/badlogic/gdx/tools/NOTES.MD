These classes are extension to original libGDX tools. List of current modifications:

- `TexturePacker` and `ImageProcessor` have got `#addImage(File, String)` to pass image file with custom name.
- `TexturePacker` got `PageFileWriter`. `jpegQuality` and `outputFormat` fields removed from `TexturePacker#Settings`. 
- All static methods were removed from `TexturePacker` (to cut redundant class dependencies).
- `KTXProcessor` code refactored so it can be used at runtime (it used to work only as a service class that can be launched through `#main(String[])` methods).
- `KTXProcessor` uses improved code to extract and store `etctool` runnable files.