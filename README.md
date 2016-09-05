![](http://i.imgur.com/h0CUJ3B.png)

Simple utility to help you pack and manage texture atlases for [LibGDX game framework](https://github.com/libgdx/libgdx).
It's mostly just visual wrapper over [LibGDX TexturePacker classes](https://github.com/libgdx/libgdx/tree/master/extensions/gdx-tools/src/com/badlogic/gdx/tools/texturepacker)
and offers more convenient way of using it.

This project is a successor of [Aurelien Ribon's project](https://github.com/aurelienRibon/libgdx-texturepacker-gui) with whole new GUI and features.

![](http://i.imgur.com/pemUEYU.png)

### Backlog
See what features are currently planned from project's [Trello board](https://trello.com/b/mugauAoC)

### Releases
New versions are coming out as soon as application gets significant new features or important fixes. You can see all available versions from [releases page](https://github.com/crashinvaders/gdx-texture-packer-gui/releases).

### Contribution
Any contribution is highly appreciated. You can help either by making PR or reporting bugs/suggestions by creating new issues.

Also you can participate in translation. It can be done by translating all the strings in [bundle.properties](https://github.com/crashinvaders/gdx-texture-packer-gui/blob/master/core/assets/i18n/bundle.properties) and saving copy as bundle_XX.properties, where XX is your language code.

### Story behind
Many years ago [Aurelien Ribon](https://github.com/aurelienRibon) wrote fantastic and very useful tool to manage texture atlases for LibGDX projects. It was very convenient and highly spread among the community. But unfortunately, after some time project became stagnated and was not maintained anymore. I continued to use last published version (3.2.0) for my every LibGDX project and as far as I get in touch with it, I keep coming with ideas of how UX can be improved and some new features as well. And one day I realized that I had already accumulated too much and decided to ask Aurelien if I can continue development of his project. He kindly shared sources on GitHub and gave me white card for any changes. And here I happily started. fiBut after I got reviewed all the code, I found that it's mostly very outdated (LibGDX 0.9.9) and not well structured so it's been hard to put any new features in there. Then after few weeks of ground breaking refactoring I figured out that it's much easier to write a new project from the scratch and use old one as general reference. And here we are, with first release version [4.0.0](https://github.com/crashinvaders/gdx-texture-packer-gui/releases/tag/4.0.0) I finally achieved my goal and got up to date codebase ready to grow and expand. And I'm very hope that this is just a beginning!
