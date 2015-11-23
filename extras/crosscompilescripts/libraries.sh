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

mkdir -p compiled/$cpu_arch
rootdir=$(pwd)
mkdir -p libs
libdir=$rootdir/libs
cd $libdir

# libffi
if [ ! -d "libffi" ]; then
  git clone https://github.com/atgreen/libffi.git
  cd libffi
  git checkout v3.2.1
  cd ..
fi
cd libffi
./autogen.sh
mkdir -p build-android
cd build-android
../configure --prefix=$rootdir/compiled/$cpu_arch/libffi --host=arm-linux-androideabi --disable-shared --enable-static
echo "#define FFI_MMAP_EXEC_WRIT 1" >> fficonfig.h
echo "#define FFI_MMAP_EXEC_SELINUX 0" >> fficonfig.h
make && make install
cd $libdir

# uuid
if [ ! -d "libuuid-1.0.3" ]; then
  curl -L -O http://downloads.sourceforge.net/libuuid/libuuid-1.0.3.tar.gz && \
  tar -xvzf libuuid-1.0.3.tar.gz &&\
  rm libuuid-1.0.3.tar.gz
fi
cd libuuid-1.0.3 && mkdir -p build-android
cd build-android
../configure --host=arm-linux-androideabi --disable-shared --enable-static --prefix=$rootdir/compiled/$cpu_arch/uuid
make && make install
cd $libdir

# zlib
if [ ! -d "zlib-1.2.8" ]; then
  curl -L -O http://zlib.net/zlib-1.2.8.tar.gz
  tar -xvzf zlib-1.2.8.tar.gz
  rm zlib-1.2.8.tar.gz
fi
cd zlib-1.2.8
./configure --prefix=$rootdir/compiled/$cpu_arch/zlib
make && make install
cd $libdir

# curl
if [ ! -d "curl-7.38.0" ]; then
  curl -L -O http://curl.haxx.se/download/curl-7.38.0.tar.gz
  tar -xvzf curl-7.38.0.tar.gz
  rm curl-7.38.0.tar.gz
fi
cd curl-7.38.0 && mkdir -p build-android
cd build-android
../configure --host=arm-linux-androideabi --disable-shared --enable-static -disable-dependency-tracking --with-zlib=$rootdir/compiled/$cpu_arch/zlib --without-ca-bundle --without-ca-path --disable-ftp --disable-file --disable-ldap --disable-ldaps --disable-rtsp --disable-proxy --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi --disable-manual --target=arm-linux-androideabi --prefix=$rootdir/compiled/$cpu_arch/curl
make && make install
cd $libdir

# libxml
if [ ! -d "libxml2-2.7.2" ]; then
  curl -L -O http://xmlsoft.org/sources/libxml2-2.7.2.tar.gz
  tar -xvzf libxml2-2.7.2.tar.gz
  rm libxml2-2.7.2.tar.gz
fi
curl -L -O https://raw.githubusercontent.com/bpetri/libxml2_android/master/config.guess
curl -L -O https://raw.githubusercontent.com/bpetri/libxml2_android/master/config.sub
curl -L -O https://raw.githubusercontent.com/bpetri/libxml2_android/master/libxml2.patch
mv config.guess config.sub libxml2-2.7.2
patch -N -p1 < libxml2.patch
rm libxml2.patch
cd libxml2-2.7.2
mkdir -p build-android
cd build-android
../configure --host=arm-linux-androideabi --disable-shared --enable-static --prefix=$rootdir/compiled/$cpu_arch/libxml2
make && make install
cd $libdir

# jansson
if [ ! -d "jansson-2.7" ]; then
  curl -L -O http://www.digip.org/jansson/releases/jansson-2.7.tar.bz2
  tar -xvjf jansson-2.7.tar.bz2
  rm jansson-2.7.tar.bz2
fi
cd jansson-2.7 && mkdir -p build-android
cd build-android
../configure --host=arm-linux-androideabi --disable-shared --enable-static --prefix=$rootdir/compiled/$cpu_arch/jansson
make && make install

