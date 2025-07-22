# imageviewer

This is "imageviewer", a 100% Java music player with a UI written in Java Swing.

![ImageViewer](imageviewer.png "ImageViewer")

Features:
- extremely customizable via Java extension classes
- highly configurable UI with LookAndFeel support
- quickly sort, rename, move, or symlink images
- very programmer-friendly if you want to write your own extensions

## How do I get it?

The easiest way is to clone the repo, build it with Maven, and run the jar file:

```shell
git clone https://github.com/scorbo2/imageviewer.git
cd imageviewer
mvn package
cd target
java -jar imageviewer-2.1.jar
```

If you have [install-scripts](https://github.com/scorbo2/install-scripts) installed, you can also
just run the `make-installer` command from the project root. This will (on linux) generate a tarball
containing an installer script and will provide a launcher script for more easily launching the application.

## User guide

TODO a few screenshots and a tour of the stock UI goes here, also maybe a discussion of some of the extensions...

## License

imageviewer is made available under the MIT license: https://opensource.org/license/mit

## Revision history

Originally written in 2017.  

[Full release notes](src/main/resources/ca/corbett/imageviewer/ReleaseNotes.txt)
