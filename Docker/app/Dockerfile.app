FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && apt upgrade -y \
    && apt install -y tmux curl wget git vim openjdk-17-jdk \
    && apt install -y build-essential open-ssl libssl-dev libssl3 libgl1-mesa-dev libqt5x11extras5 '^libxcb.*-dev' libx11-xcb-dev libglu1-mesa-dev libxrender-dev libxi-dev libxkbcommon-x11-dev libxkbcommon-dev 1
    && apt install -y libxdamage-dev pluseaudio libxcursor-dev \
    && apt install -y zlibg1-dev libcurlpp-dev cmake dbus-x11 libcanberra-gtk-module gnome-keyring mosquitto-dev
    
CMD ["/bin/bash"]

