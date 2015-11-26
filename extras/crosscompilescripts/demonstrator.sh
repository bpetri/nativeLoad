#!/bin/sh
# Crosscompiles demonstrator using already compiled Celix and toolchain from android-ndk
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
}

askinput() {
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

checksandmakedirs() {
  mkdir -p compiled/$cpu_arch
  libdir=$rootdir/compiled/$cpu_arch

  # Check for demonstrator source in rootdir
  if [ ! -d "demonstrator" ]; then
    echo "No demonstrator source found at: $rootdir/demonstrator"
    exit 1
  fi

  cd $libdir

  # Check if celix is compiled for current architecture
  if [ ! -d "celix" ]; then
    echo "No celix install found at: $libdir/celix"
    exit 1
  fi
  # Remove older compiled demonstrator
  if [ -d "demonstrator" ]; then
    rm -r demonstrator
  fi


  cd $rootdir/demonstrator

  # Remove old build folder
  if [ -d "build-$cpu_arch" ]; then
    rm -r build-$cpu_arch
  fi
  mkdir -p build-$cpu_arch
}

makeDemonstrator() {
  cd build-$cpu_arch
  cp $rootdir/celix/build-$cpu_arch/config.h $rootdir/demonstrator/build-$cpu_arch/config.h
  cmake -DCELIX_DIR=$libdir/celix -DCMAKE_BUILD_TYPE=Debug -DJANSSON_LIBRARY=$libdir/jansson/lib/libjansson.a -DJANSSON_INCLUDE_DIR=$libdir/jansson/include ..
  make && make deploy
  mv deploy $libdir/demonstrator
}

main() {
  askinput
  checksandmakedirs
  makeDemonstrator
}

main




