# Crosscompiling

In order to crosscompile you first need to download the latest android-ndk http://developer.android.com/ndk/index.html
Then set your NDK_HOME variable to the folder where the ndk is located.

1. To build the required toolchains for arm armv7a and armv8a run the toolchains.sh from terminal.
2. To download/crosscompile the required libraries run the libraries.sh.
3. To crosscompile celix run the celix.sh		(Requires celix source to be in the same folder as the script!)
4. To crosscompile demonstrator run the demonstrator.sh (Requires demonstrator source to be in the same folder as the script!)

Correct order in short:
1. toolchains.sh		(neccesary for crosscompiling)
2. libraries.sh			(neccesary for celix)
3. celix.sh			(neccesary for demonstrator) 
4. demonstrator.sh

All the crosscompiled libraries will be located in compiled/architecture. The downloaded libraries used for crosscompiling can be found in libs. the used toolchains can be found in toolchain and toolchain64.

PS.  Make sure to execute all the scripts from the directory they're located.
PS2. You have to download the celix and demonstrator source yourself and place it in the same folder as the scripts.
