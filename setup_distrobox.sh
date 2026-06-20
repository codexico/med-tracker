#!/bin/bash

# Nome do container
CONTAINER_NAME="ubuntu22-android"

echo "=================================================="
echo "🚀 Criando o Distrobox para Android Development..."
echo "=================================================="

# 1. Criar o container Ubuntu (A melhor distro e mais suportada pelo Google para Android)
# Vamos usar o Ubuntu 22.04 LTS pois é a mais estável e testada pelas ferramentas do Android
# distrobox create --image ubuntu:22.04 --name $CONTAINER_NAME --yes

echo "=================================================="
echo "📦 Instalando as dependências essenciais..."
echo "=================================================="

# 2. Entrar no container e rodar os comandos de instalação
distrobox enter $CONTAINER_NAME -- bash -c '
    echo "Atualizando pacotes e ativando arquitetura 32-bits (necessário para o emulador e ADB)..."
    sudo dpkg --add-architecture i386
    sudo apt-get update -y && sudo apt-get upgrade -y
    
    echo "Instalando Java 17, ferramentas de build, fuse (para AppImages) e bibliotecas C++..."
    sudo apt-get install -y \
        openjdk-17-jdk openjdk-17-jre \
        wget curl git unzip zip nano software-properties-common \
        lib32z1 libbz2-1.0:i386 libc6:i386 libncurses5:i386 libstdc++6:i386 \
        libfuse2
    
    echo "Instalando o JetBrains Toolbox (Gerenciador Oficial para o Android Studio)..."
    wget -cO jetbrains-toolbox.tar.gz "https://data.services.jetbrains.com/products/download?code=TBA&platform=linux"
    tar -xzf jetbrains-toolbox.tar.gz
    DIR=$(find . -maxdepth 1 -type d -name "jetbrains-toolbox-*" | head -n 1)
    
    mkdir -p ~/.local/bin
    mv $DIR/jetbrains-toolbox ~/.local/bin/
    rm -rf jetbrains-toolbox.tar.gz $DIR
    
    echo "Exportando o ambiente do Java..."
    echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> ~/.bashrc
    echo "export PATH=\$PATH:\$JAVA_HOME/bin:~/.local/bin" >> ~/.bashrc
    
    echo "=================================================="
    echo "✅ Setup Concluído com Sucesso!"
    echo "=================================================="
    echo "Para começar, rode no seu terminal host:"
    echo "1. distrobox enter $CONTAINER_NAME"
    echo "2. jetbrains-toolbox"
    echo "Dentro do Toolbox, basta clicar para instalar o Android Studio!"
'


https://developer.android.com/studio/run/emulator-acceleration?utm_source=android-studio-app&utm_medium=app#vm-linux

sudo apt-get install cpu-checker
egrep -c '(vmx|svm)' /proc/cpuinfo

sudo kvm-ok

sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
