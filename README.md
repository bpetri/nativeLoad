#nativeLoad [![Build Status](https://travis-ci.org/bpetri/nativeLoad.svg?branch=master)](https://travis-ci.org/bpetri/nativeLoad) [![Stories in Ready](https://badge.waffle.io/bpetri/nativeLoad.png?label=ready&title=Ready)](https://waffle.io/bpetri/nativeLoad)
This project implements a launcher for Apache Celix Bundles embedded in an Android Application. Apache Celix is an implementation of the OSGi specification adapted to C. 

This repository already contains a armv7 cross-compiled installation of Apache Celix. For different architectures the Docker file at [github.com/apache/celix](https://github.com/apache/celix/tree/feature/CELIX-247_android_support) may be re-used.

##Quick start guide

Follow this guide if you want to start developing this application  
###Tools used:
- Android Studio 1.4.1  
- Android SDK with NDK [Download here](https://developer.android.com/sdk/index.html#Other)

###Importing 
1. Open Android Studio and Check out project from Version Control -> Github
2. Use this link as Git Repository URL `https://github.com/bpetri/nativeLoad.git`
3. If everything worked, Gradle will check, download libraries and build the project
4. You're now ready to start developing

###Extra
- A tool to generate QR-codes is included in this project, it's called `swingQr.jar`
- Dockerfiles are included for crosscompiling Celix and the INAETICS demonstrator, check the wiki on how to do this
- In the folder `apache_ace_config` there are some files for configuring Apache Ace, check the wiki for more info

##Building
The application build can be reproduced by using the Dockerfile:
```
docker build -t nativeload .
docker run -t nativeload
```

