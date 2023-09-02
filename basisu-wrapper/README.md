# Basis Universal Wrapper

A lightweight pure Java wrapper over the native [Basis Universal]([GitHub - BinomialLLC/basis_universal: Basis Universal GPU Texture Codec](https://github.com/BinomialLLC/basis_universal)) library (super-compressed textures).

This module is very similar to `basisu-wrapper` module of [gdx-basis-universal project](https://github.com/crashinvaders/gdx-basis-universal), but this implementation aims to support only the minimal required subset of the Basis Universal features:

- Retrieve Basis texture file info (read Basis header).

- Encode Basis textures using ETC1S and ASTC.

- Decode Basis textures to RGBA textures only (no transcoding to other GPU textures).

- Compile only for the desktop targets (Windows, Linux, macOS).

### Requirements and Compilation.

For setup and compilation hints, please read the README file from `basisu-wrapper` module of the [gdx-basis-universal](https://github.com/crashinvaders/gdx-basis-universal/tree/master/basisu-wrapper) project.




