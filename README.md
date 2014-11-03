# What is it?

This is an android application aimed to help casters playing Dungeons & Dragons 5 edition. It
lets you navigate through spells and see their details. Planned features to be implemented
soon (hopefully):

- add all spells from dnd edition 5;
- search spells by name / description;
- mark spells as favorites and switch between "favorites only" and "all spells" modes.
- distinguish different class spells in the list by color and/or icon; class based filtering
  of spells.

# Screenshots

![Spell list](https://github.com/servy/dnd5spellbook/raw/master/screenshot/screen1.png "Spell list")
![Spell list](https://github.com/servy/dnd5spellbook/raw/master/screenshot/screen2.png "Spell details")

# Building the project

Project uses gradle build system and gradle android plugin. For more information check
[it's documentation](http://tools.android.com/tech-docs/new-build-system/user-guide).

You have to specify your android SDK path either by creating a **local.properties** file at
the project root with `sdk.dir=<path to the sdk>` content or by setting `ANDROID_HOME`
environment variable.

Then you can assemble apk files by invoking `gradlew assemble` from the command line (`gradlew.bat`
is used for Windows systems and `gradlew` script for non-Windows). The debug apk is signed and can
be uploaded to the emulator. Note, that the release apk has to be
[signed](http://developer.android.com/tools/publishing/app-signing.html) in order to be installed
on an actual phone.

As this project was created in
[Android studio](https://developer.android.com/sdk/installing/studio.html) you should not have
much trouble importing it into this IDE.
