#!/bin/sh
# Variables to toolchain and compilers
export PATH=/home/marcojansen/workspace/toolchain/bin:$PATH
export CC=arm-linux-androideabi-gcc
export CXX=arm-linux-androideabi-g++

read -p "enter 1 for ARM, 2 for ARM-v7a: " inp
case $inp in
  1 ) echo "chose ARM"; cpu_arch=arm; break;;
  2 ) echo "chose ARM-v7a"; cpu_arch=arm-v7a; export CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16" break;;
esac

rootdir=$(pwd)
libdir=$rootdir/compiled/$cpu_arch
mkdir -p compiled/$cpu_arch

cd compiled/$cpu_arch
if [ -d "node-wiring-c" ]; then
  rm -r node-wiring-c
fi

cd $rootdir

if [ ! -d "node-wiring-c" ]; then
  echo "No node wiring source found!"
fi

cd node-wiring-c
mkdir -p build-android
cd build-android
cmake -DCELIX_DIR=$libdir/celix -DCMAKE_INSTALL_PREFIX:PATH=$libdir/node-wiring-c -DCURL_LIBRARY=$libdir/curl/lib/libcurl.a -DCURL_INCLUDE_DIR=$libdir/curl/include -DJANSSON_LIBRARY=$libdir/jansson/lib/libjansson.a -DJANSSON_INCLUDE_DIR=$libdir/jansson/include -DFFI_LIBRARY=$libdir/libffi/lib/libffi.a -DFFI_INCLUDE_DIR=$libdir/libffi/lib/libffi-3.2.1/include -DUUID_LIBRARY=$libdir/uuid/lib/libuuid.a -DUUID_INCLUDE_DIR=$libdir/uuid/include ..
make all
make deploy

