#!/bin/sh
# Variables to toolchain and compilers
export PATH=/home/mjansen/workspace/toolchain/bin:$PATH
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
if [ -d "celix" ]; then
  rm -r celix
fi

cd $rootdir

if [ ! -d "celix" ]; then
  echo "No celix source found!"
fi

cd celix
mkdir -p build-android
cd build-android
cmake -DENABLE_TESTING=OFF -DANDROID=TRUE -DBUILD_EXAMPLES=OFF -DBUILD_REMOTE_SERVICE_ADMIN=ON -DBUILD_REMOTE_SHELL=ON -DBUILD_RSA_DISCOVERY_CONFIGURED=ON -DBUILD_RSA_DISCOVERY_ETCD=ON -DBUILD_RSA_EXAMPLES=ON -DBUILD_RSA_REMOTE_SERVICE_ADMIN_DFI=OFF -DBUILD_RSA_REMOTE_SERVICE_ADMIN_HTTP=ON -DBUILD_RSA_TOPOLOGY_MANAGER=ON -DJANSSON_LIBRARY=$libdir/jansson/lib/libjansson.a -DJANSSON_INCLUDE_DIR=$libdir/jansson/include -DCURL_LIBRARY=$libdir/curl/lib/libcurl.a -DCURL_INCLUDE_DIR=$libdir/curl/include -DLIBXML2_LIBRARIES=$libdir/libxml2/lib/libxml2.a -DLIBXML2_INCLUDE_DIR=$libdir/libxml2/include/libxml2 -DZLIB_LIBRARY=$libdir/zlib/lib/libz.a -DZLIB_INCLUDE_DIR=$libdir/zlib/include -DUUID_LIBRARY=$libdir/uuid/lib/libuuid.a -DUUID_INCLUDE_DIR=$libdir/uuid/include -DCMAKE_INSTALL_PREFIX:PATH=$libdir/celix ..
make && make install-all

