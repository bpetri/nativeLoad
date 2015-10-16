[![Stories in Ready](https://badge.waffle.io/bpetri/nativeLoad.png?label=ready&title=Ready)](https://waffle.io/bpetri/nativeLoad)
#nativeLoad [![Build Status](https://travis-ci.org/bpetri/nativeLoad.svg?branch=master)](https://travis-ci.org/bpetri/nativeLoad)
This project implements a launcher for Apache Celix Bundles embedded in an Android Application. Apache Celix is an implementation of the OSGi specification adapted to C. 

This repository already contains a armv7 cross-compiled installation of Apache Celix. For different architectures the Docker file at [github.com/apache/celix](https://github.com/apache/celix/tree/feature/CELIX-247_android_support) may be re-used.


##Building
The application build can be reproduced by using the Dockerfile:
```
docker build -t nativeload .
docker run -t nativeload
```

