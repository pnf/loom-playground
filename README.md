# loom-playground

First, you'll need to build a jdk with loom, per https://wiki.openjdk.java.net/display/loom/Main

Before starting, make sure you have a boot jdk of at least version 10 installed.

``` bash
$ cd $WHEREEVER
$ hg clone http://hg.openjdk.java.net/loom/loom
$ cd loom
$ hg update -r fibers
$ sh configure  # after installing a jdk 11
$ make images
```

If you're using IntellIJ, the jdk you want to add is `$WHEREEVER/loom/build/macosx-x86_64-server-release/images/jdk`
(or something analogous for linux), not the `jdk` directory directly in `...release/`.

Note that, since the loom branch is (as of October 2018) is off of Java 12, we also have graal available,
which you can enable with `-XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler` in the run targets.

