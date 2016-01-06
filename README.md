# nativeLoad [![Build Status](https://travis-ci.org/bpetri/nativeLoad.svg?branch=master)](https://travis-ci.org/bpetri/nativeLoad) [![Stories in Ready](https://badge.waffle.io/bpetri/nativeLoad.png?label=ready&title=Ready)](https://waffle.io/bpetri/nativeLoad)
This Android application implements a launcher for Apache Celix. It also has Celix and Demonstrator bundles which can be installed/started/stopped/deleted using this application. Apache Celix is an implementation of the OSGi specification adapted to C.

This repository contains an Android library with the cross-compiled installation of Apache Celix for arm and armv7a. For source code for celix and the bundles used, see Reference materials.

## Reference materials
 [Source code Celix, Celix-bundles, Demonstrator-bundles and cross-compile scripts which were used.](https://github.com/marcojansen/android-inaetics)  
 [Source code QR-code generator used for easy configuration](https://github.com/marcojansen/QRGenerator)  
 [Apache Celix Github page](https://github.com/apache/celix)  
 [INAETICS Demonstrator page](https://github.com/INAETICS/demonstrator)

## Quick start guide

Follow this guide if you want to start developing this application

### Tools used:
- Android Studio 1.4.1  
- Android SDK with NDK [Download here](https://developer.android.com/sdk/index.html#Other)

### Importing
1. Open Android Studio and Check out project from Version Control -> Github
2. Use this link as Git Repository URL `https://github.com/bpetri/nativeLoad.git`
3. If everything worked, Gradle will check, download libraries and build the project
4. You're now ready to start developing

### Extra
- A tool to generate QR-codes is included in this project, it's called `swingQr.jar`
- Dockerfiles are included for cross-compiling Celix and the INAETICS demonstrator, check the wiki on how to do this. At the page where the source that was used is located there are also shell scripts to do this cross-compiling which works quicker than the Dockerfiles.
- In the folder `apache_ace_config` there are some files for configuring Apache Ace, check the wiki for more info

## Building
The application build can be reproduced by using the Dockerfile:
```
docker build -t nativeload .
docker run -t nativeload
```
