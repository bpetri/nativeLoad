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
mkdir -p compiled/$cpu_arch
libdir=$rootdir/compiled/$cpu_arch


cd $libdir

if [ ! -d "celix" ]; then
  echo "No celix install found at: $libdir/celix"
  exit
fi

if [ -d "demonstrator" ]; then
  rm -r demonstrator
fi


cd $rootdir

if [ ! -d "demonstrator" ]; then
  echo "No demonstrator source found!"
fi

cd demonstrator
cmake -DCELIX_DIR=$libdir/celix -DCMAKE_BUILD_TYPE=Debug -DJANSSON_LIBRARY=$libdir/jansson/lib/libjansson.a -DJANSSON_INCLUDE_DIR=$libdir/jansson/include . && make && make deploy
mv deploy $libdir/demonstrator

