![](https://i.imgur.com/7aOmSQb.png)

A simple utility to help you pack and manage texture atlases for [libGDX game framework](https://github.com/libgdx/libgdx).
It's mostly just a visual wrapper over [libGDX TexturePacker](https://libgdx.com/wiki/tools/texture-packer)
and provides some extra features on top of it.

This project is a successor of [Aurelien Ribon's application](https://web.archive.org/web/20170803205402/http://www.aurelienribon.com/blog/2012/06/texturepacker-gui-support-for-multiple-packs/) with the whole new GUI and features.

![](http://i.imgur.com/tEFWr68.png)

### Using the App
Just download the most recent version of the installer/distribution matching your OS from the [releases page](https://github.com/crashinvaders/gdx-texture-packer-gui/releases) and follow the installer instructions. 
Or simply download the distribution files archive, extract, and read `./readme.txt` for further details.

### System Requirements
The app works on any major desktop OS (Linux, macOS, Windows), where Java is available.

The requirements are as follows:
1. Java Runtime Environment (JRE 8.0 and up) should be installed on your system (`java` command is available from the command-line interface).
2. OpenGL (2.0 and up) compatible video drivers. 

All major changes are listed in [this file](https://github.com/crashinvaders/gdx-texture-packer-gui/blob/master/CHANGES).

### Command Line
The app has a headless batch mode that allows automation.

For example, to pack a specific atlases from the project:
```shell
gdx-texture-packer --batch --project "/path/to/project.tpproj" --atlases "atlas_name" "another_atlas_name"
```

Here's also a tool to compress any PNG/JPEG image to a KTX2/Basis texture.
```shell
gdx-texture-packer --basis-pack --container ktx2 --format uastc "/path/to/any.png|jpg"
```

Learn more about CLI options from the help message:
```shell
gdx-texture-packer --help
```

### Backlog
See what features are currently planned on the project's [Trello board](https://trello.com/b/mugauAoC)

### Releases
New versions come out as soon as the application gets significant new features or important fixes.
You can see all available versions from the [releases page](https://github.com/crashinvaders/gdx-texture-packer-gui/releases).

### Contribution and Contact
Any contribution is highly appreciated. You can help either by making a PR or reporting bugs/suggestions by creating new issues.
If you have any questions/ideas, and you think they don't fit the standard GitHub issue format, you are always welcome to contact me directly at anton@crashinvaders.com

Also, you can participate in translation. It can be done by translating all the strings in [bundle.properties](https://github.com/crashinvaders/gdx-texture-packer-gui/blob/master/assets/i18n/bundle.properties) and saving a copy as bundle_XX.properties, where XX is your language code.
