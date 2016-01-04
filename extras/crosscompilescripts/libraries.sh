#!/bin/sh
# Downloads and crosscompiles libraries needed for Apache Celix and Inaetics demonstrator
# You have to build the toolchains first in order to crosscompile.
rootdir=$(pwd)


arm() {
  # check for toolchain
  if [ ! -d "toolchain" ]; then
    echo "No toolchain found in: $rootdir/toolchain"
    exit 1
  fi
  # Variables to toolchain and compilers
  export PATH=$rootdir/toolchain/bin:$PATH
  export CC=arm-linux-androideabi-gcc
  export CXX=arm-linux-androideabi-g++
  host=arm-linux-androideabi
}

arm64() {
  # check for arm64 toolchain
  if [ ! -d "toolchain64" ]; then
    echo "No toolchain64 found in: $rootdir/toolchain64"
    exit 1
  fi
  # Variables to toolchain and compilers
  export PATH=$rootdir/toolchain64/bin:$PATH
  export CC=aarch64-linux-android-gcc
  export CXX=aarch64-linux-android-g++
  host=aarch64-linux-android
}

# input which architecture to compile for
ask_input() {
  read -p "1) ARM          2) ARM-v7a          3) Aarch64 : " inp
  case $inp in
    1 )
 	echo "chose ARM" 
	cpu_arch=arm 
	arm;;
    2 )
 	echo "chose ARM-v7a" 
	cpu_arch=arm-v7a 
	arm
	export CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16";;
    3 )
	echo "chose ARM-v8a"
	cpu_arch=arm-v8a
  	arm64;;
  esac
}

make_directories() {
  mkdir -p compiled/$cpu_arch
  mkdir -p libs
  libdir=$rootdir/libs
  installdir=$rootdir/compiled/$cpu_arch
  cd $libdir
}

download_libraries() {
  #libffi
  if [ ! -d "libffi" ]; then
    git clone https://github.com/atgreen/libffi.git
  fi

  #libuuid
  if [ ! -d "libuuid-1.0.3" ]; then
    curl -L -O http://downloads.sourceforge.net/libuuid/libuuid-1.0.3.tar.gz
    tar -xvzf libuuid-1.0.3.tar.gz
    rm libuuid-1.0.3.tar.gz
  fi

  #zlib
  if [ ! -d "zlib-1.2.8" ]; then
    curl -L -O http://zlib.net/zlib-1.2.8.tar.gz
    tar -xvzf zlib-1.2.8.tar.gz
    rm zlib-1.2.8.tar.gz
  fi

  #curl
  if [ ! -d "curl-7.45.0" ]; then
    curl -L -O http://curl.haxx.se/download/curl-7.45.0.tar.gz
    tar -xvzf curl-7.45.0.tar.gz
    rm curl-7.45.0.tar.gz
  fi

  #libxml with patch
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

  #jansson
  if [ ! -d "jansson-2.7" ]; then
    curl -L -O http://www.digip.org/jansson/releases/jansson-2.7.tar.bz2
    tar -xvjf jansson-2.7.tar.bz2
    rm jansson-2.7.tar.bz2
  fi
 
}

make_libffi() {
  cd libffi
  ./autogen.sh
  mkdir -p build-$cpu_arch
  cd build-$cpu_arch
  ../configure --prefix=$installdir/libffi --host=$host --disable-shared --enable-static
  echo "#define FFI_MMAP_EXEC_WRIT 1" >> fficonfig.h
  echo "#define FFI_MMAP_EXEC_SELINUX 0" >> fficonfig.h
  make && make install
  cd $libdir
}

make_uuid() {
  cd libuuid-1.0.3 && mkdir -p build-$cpu_arch
  cd build-$cpu_arch
  ../configure --host=$host --disable-shared --enable-static --prefix=$installdir/uuid
  make && make install
  cd $libdir
}

make_zlib() {
  cp -R zlib-1.2.8 zlib-copy
  cd zlib-copy
  ./configure --prefix=$installdir/zlib
  make && make install
  cd $libdir
  rm -r zlib-copy
}

make_curl() {
  cd curl-7.45.0 && mkdir -p build-$cpu_arch
  cd build-$cpu_arch
  ../configure --host=$host --disable-shared --enable-static -disable-dependency-tracking --with-zlib=$installdir/zlib --without-ca-bundle --without-ca-path --disable-ftp --disable-file --disable-ldap --disable-ldaps --disable-rtsp --disable-proxy --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi --disable-manual --enable-threaded-resolver --target=$host --prefix=$installdir/curl
  make && make install
  cd $libdir
}

make_libxml() {
  cd libxml2-2.7.2
  mkdir -p build-$cpu_arch
  cd build-$cpu_arch
  ../configure --host=$host --disable-shared --enable-static --prefix=$installdir/libxml2
  make && make install
  cd $libdir
}

make_jansson() {
  cd jansson-2.7 && mkdir -p build-$cpu_arch
  cd build-$cpu_arch
  ../configure --host=$host --disable-shared --enable-static --prefix=$installdir/jansson
  make && make install
}

main() {
  ask_input
  make_directories
  download_libraries
  make_libffi
  make_uuid
  make_zlib
  make_curl
  make_libxml
  make_jansson
}

main


