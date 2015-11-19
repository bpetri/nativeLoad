
FROM ubuntu:14.04
MAINTAINER Bjoern Petri <bjoern.petri@sundevil.de>


WORKDIR /build/resources

ENV ANDROID_HOME /build/resources/android-sdk-linux
ENV PATH $PATH:/build/resources/android-ndk-r10e

# install needed tools
RUN apt-get -q update && apt-get -q install -y \
    gcc-multilib \
    lib32z1 \
    lib32stdc++6 \
    make \ 
    curl \
    p7zip-full \
    default-jdk  

# Extracting ndk
RUN curl -L -O http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin && \
	7z x android-ndk-r10e-linux-x86_64.bin | grep -v "Extracting"

# Installing sdk
RUN curl -L -O http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz && \
    tar -zxf android-sdk_r24.4.1-linux.tgz && \
    cd android-sdk-linux && \ 
    echo y | tools/android update sdk --all --filter tools,platform-tools,build-tools-23.0.2,android-23,extra-android-support,extra-android-m2repository,extra-google-m2repository --no-ui && \
             cd  -

ADD . nativeLoad

CMD cd nativeLoad && ./gradlew build 
