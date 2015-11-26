#!/bin/sh
rootdir=$(pwd)

# Check if NDK_HOME variabele is set
checkndkhome() {
  if [ -z "$NDK_HOME" ]; then
    echo "NDK_HOME variabele is not set!"
    exit 1
  fi
  echo "using NDK_HOME=$NDK_HOME"
}

# Make arm toolchain (armv7a and arm5)
normaltoolchain() {
  if [ -d "toolchain" ]; then
    echo "There's already a toolchain folder"
    exit 1
  fi
  $NDK_HOME/build/tools/make-standalone-toolchain.sh --platform=android-18 --install-dir=$rootdir/toolchain --toolchain=arm-linux-androideabi-4.9
}

# Make aarch64 toolchain (armv8a)
aarch64toolchain() {
  if [ -d "toolchain64" ]; then
    echo "There's already a toolchain64 folder"
    exit 1
  fi
  $NDK_HOME/build/tools/make-standalone-toolchain.sh --platform=android-21 --install-dir=$rootdir/toolchain64 --toolchain=aarch64-linux-android-4.9
}



main() {
  checkndkhome
  normaltoolchain
  aarch64toolchain
}

main

