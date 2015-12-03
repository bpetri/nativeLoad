#!/bin/sh
# Crosscompiles Celix using already compiled libraries and toolchain from android-ndk
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
  libffi_loc=lib/libffi.a
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
  libffi_loc=lib64/libffi.a
}

ask_input() {
  # input which architecture to compile for
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
	export CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16" ;;
    3 )
	echo "chose ARM-v8a"
	cpu_arch=arm-v8a
	arm64;;
  esac
}

checks_and_makedirs() {
  libdir=$rootdir/compiled/$cpu_arch
  mkdir -p compiled/$cpu_arch

  # check if there is celix source found
  if [ ! -d "celix" ]; then
    echo "No celix source found in root dir: $rootdir/celix"
    exit 1
  fi

  # remove old if already compiled earlier
  cd compiled/$cpu_arch
  if [ -d "celix" ]; then
    rm -r celix
  fi
  
  cd $rootdir

  cd celix
  # remove old build folder
  if [ -d "build-$cpu_arch" ]; then
    rm -r build-$cpu_arch
  fi
  mkdir -p build-$cpu_arch
}

make_celix() {
  cd build-$cpu_arch
  cmake -DENABLE_TESTING=OFF -DCMAKE_SYSTEM_NAME="Android" -DANDROID=TRUE -DBUILD_EXAMPLES=OFF -DBUILD_REMOTE_SERVICE_ADMIN=ON -DBUILD_REMOTE_SHELL=ON -DBUILD_RSA_DISCOVERY_CONFIGURED=ON -DBUILD_RSA_DISCOVERY_ETCD=ON -DBUILD_RSA_EXAMPLES=ON -DBUILD_RSA_REMOTE_SERVICE_ADMIN_DFI=ON -DBUILD_RSA_REMOTE_SERVICE_ADMIN_HTTP=ON -DBUILD_RSA_TOPOLOGY_MANAGER=ON -DJANSSON_LIBRARY=$libdir/jansson/lib/libjansson.a -DJANSSON_INCLUDE_DIR=$libdir/jansson/include -DCURL_LIBRARY=$libdir/curl/lib/libcurl.a -DCURL_INCLUDE_DIR=$libdir/curl/include -DLIBXML2_LIBRARIES=$libdir/libxml2/lib/libxml2.a -DLIBXML2_INCLUDE_DIR=$libdir/libxml2/include/libxml2 -DZLIB_LIBRARY=$libdir/zlib/lib/libz.a -DZLIB_INCLUDE_DIR=$libdir/zlib/include -DUUID_LIBRARY=$libdir/uuid/lib/libuuid.a -DUUID_INCLUDE_DIR=$libdir/uuid/include -DFFI_LIBRARY=$libdir/libffi/$libffi_loc -DFFI_INCLUDE_DIR=$libdir/libffi/lib/libffi-3.99999/include -DCMAKE_INSTALL_PREFIX:PATH=$libdir/celix ..
  make && make install-all
}

main() {
  ask_input
  checks_and_makedirs
  make_celix
}

main






