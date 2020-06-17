#!/bin/bash

#
# Script to automatic install of OpenDevice in Linux Machine
# It also install Java if not installed
# Install using:
# > curl -Ls https://goo.gl/mgQGNH | bash
# Required: unzip


# Installation Path
ODEV_PATH=/opt/opendevice



main() {

  # CHECK_JAVA_INSTALLED
  hash java 2>/dev/null || { 
    printf "Java is required but it's ${BOLD}not installed. ${NORMAL}\n"; 
    install_java

    hash java 2>/dev/null || { 
      printf "${RED}>>> Automatic java installation failed. Try to install manually  ${NORMAL}\n"
      exit 1
    }
  }

  if [ -d "$ODEV_PATH" ]; then
    printf "${YELLOW}You already have OpenDevice installed.${NORMAL}\n"
    printf "You'll need to remove $ODEV_PATH if you want to re-install.\n"
    exit
  fi

  printf "${YELLOW}Downloding OpenDevice...${NORMAL}\n"

  ODEV_URL="https://api.github.com/repos/OpenDevice/OpenDevice/releases/latest"

#  ODEV_LATEST=$(curl -s -i -H "Accept: application/vnd.github.v3+json" $ODEV_URL | grep -Po '"browser_download_url": "\K(.*.zip)')
  ODEV_LATEST=$(curl -s -i -H "Accept: application/vnd.github.v3+json" $ODEV_URL | sed -n 's/.*"browser_download_url": "\(.*\)"/\1/p')

  echo $ODEV_LATEST

  curl -o opendevice.zip $ODEV_LATEST

  mkdir -p $ODEV_PATH

  unzip opendevice.zip -d $ODEV_PATH

  rm opendevice.zip

  print_banner
  echo 'Installation completed successfully, start using: '
  echo ''
  printf "${YELLOW}/opt/opendevice/bin/opendevice ${BOLD}start${NORMAL}"
  echo ''
  printf "(next:) ${YELLOW}/opt/opendevice/bin/opendevice ${BOLD}log${NORMAL} (to see logs, ${BOLD}recomended on frist start${NORMAL})"
  echo ''
  printf "${GREEN}Please follow us on facebook: https://www.facebook.com/opendevice/${NORMAL}\n"
}

print_banner() {
  printf "${GREEN}"
  echo '   ____                   ____            _          '
  echo '  / __ \____  ___  ____  / __ \___ _   __(_)_______  '
  echo ' / / / / __ \/ _ \/ __ \/ / / / _ \ | / / / ___/ _ \ '
  echo '/ /_/ / /_/ /  __/ / / / /_/ /  __/ |/ / / /__/  __/ '
  echo '\____/ .___/\___/_/ /_/_____/\___/|___/_/\___/\___/  '
  echo '    /_/                                              '
  echo ''
  printf "${NORMAL}"
}

install_java() {
  printf "${YELLOW}Installing java ...${NORMAL}\n"

  export DEBIAN_FRONTEND=noninteractive
  ## TODO: install -- sudo add-apt-repository ppa:openjdk-r/ppa
  ## download from: https://github.com/frekele/oracle-java/releases
  apt -y -q install openjdk-8-jdk || result=1

  #  Not found try repository 2
  if [[ $result > 0 ]]; then
      printf "${YELLOW}You may in new debian version, trying alternative method...${NORMAL}\n"
      apt install apt-transport-https ca-certificates wget dirmngr gnupg software-properties-common || result=1
      wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
      add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
      apt update || result=1
      printf "${YELLOW}Now install java ${NORMAL}\n"
      apt -y -q install adoptopenjdk-8-hotspot || result=1
  fi

  ## TODO: For java 11, add only: sudo apt install default-jdk 

}

init() {
  # Use colors, but only if connected to a terminal, and that terminal
  # supports them.
  if which tput >/dev/null 2>&1; then
      ncolors=$(tput colors)
  fi
  if [ -t 1 ] && [ -n "$ncolors" ] && [ "$ncolors" -ge 8 ]; then
    RED="$(tput setaf 1)"
    GREEN="$(tput setaf 2)"
    YELLOW="$(tput setaf 3)"
    BLUE="$(tput setaf 4)"
    BOLD="$(tput bold)"
    NORMAL="$(tput sgr0)"
  else
    RED=""
    GREEN=""
    YELLOW=""
    BLUE=""
    BOLD=""
    NORMAL=""
  fi

  # Only enable exit-on-error after the non-critical colorization stuff,
  # which may fail on systems lacking tput or terminfo
  set -e

  print_banner
}

init 
main
