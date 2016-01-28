#!/bin/bash

mkdir jars
cd zips
for dir in */ ; do
  dirName=${dir%/}
  cd $dir
  for singleBundle in `find . -name \*.zip`
  do
    #echo ${singleBundle}
    echo ""
    bundleName=${singleBundle%.zip}
    bundleName=${bundleName:2}
    mkdir t
    unzip -qq ${singleBundle} -d t
    cd t
    cat META-INF/MANIFEST.MF | grep 'Bundle-SymbolicName'
    sed -i "s/Bundle-SymbolicName: .*/Bundle-SymbolicName: ${bundleName}_${dirName}/" META-INF/MANIFEST.MF
    cat META-INF/MANIFEST.MF | grep 'Bundle-SymbolicName'
    symbolicName=`cat META-INF/MANIFEST.MF | grep 'Bundle-Symbolic' | cut -d' ' -f2 | tr -d '\n' | tr -d '\r'`
    #echo "New Bundle-SymbolicName: " ${symbolicName}
    version=`cat META-INF/MANIFEST.MF | grep 'Bundle-Version' | cut -d' ' -f2 | tr -d '\n' | tr -d '\r'`
    bundleFile="${symbolicName}.jar"
    #echo "BUNDLEFILE is ${bundleFile}"
    jar cfm ../../../jars/${bundleFile} META-INF/MANIFEST.MF lib*.so *.descriptor
    cd ..
    rm -rf t
  done
  cd ..
done
