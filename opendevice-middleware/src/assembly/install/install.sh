#!/bin/bash
main() {
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

  # CHECK_JAVA_INSTALLED
  hash java 2>/dev/null || { echo >&2 "Java is required but it's not installed.  Aborting."; exit 1; }

  if [ ! -n "$ODEV_PATH" ]; then
    ODEV_PATH=/opt/opendevice
  fi

  if [ -d "$ODEV_PATH" ]; then
    printf "${YELLOW}You already have OpenDevice installed.${NORMAL}\n"
    printf "You'll need to remove $ODEV_PATH if you want to re-install.\n"
    exit
  fi

  # Prevent the cloned repository from having insecure permissions. Failing to do
  # so causes compinit() calls to fail with "command not found: compdef" errors
  # for users with insecure umasks (e.g., "002", allowing group writability). Note
  # that this will be ignored under Cygwin by default, as Windows ACLs take
  # precedence over umasks except for filesystems mounted with option "noacl".
  umask g-w,o-w

  printf "${BLUE}Downloding OpenDevice...${NORMAL}\n"

  ODEV_URL="https://api.github.com/repos/OpenDevice/OpenDevice/releases/latest"

#  ODEV_LATEST=$(curl -s -i -H "Accept: application/vnd.github.v3+json" $ODEV_URL | grep -Po '"browser_download_url": "\K(.*.zip)')
  ODEV_LATEST=$(curl -s -i -H "Accept: application/vnd.github.v3+json" $ODEV_URL | sed -n 's/.*"browser_download_url": "\(.*\)"/\1/p')

  echo $ODEV_LATEST

  wget -O opendevice.zip $ODEV_LATEST

  sudo unzip opendevice.zip -d $ODEV_PATH

  rm opendevice.zip

  printf "${GREEN}"
  echo '  __  ____  ____  __ _  ____  ____  _  _  __  ___  ____ '
  echo ' /  \(  _ \(  __)(  ( \(    \(  __)/ )( \(  )/ __)(  __)'
  echo '(  O )) __/ ) _) /    / ) D ( ) _) \ \/ / )(( (__  ) _) '
  echo ' \__/(__)  (____)\_)__)(____/(____) \__/ (__)\___)(____)'
  echo ''
  echo 'Instalation Finished, start using: '
  printf "${YELLOW}"
  echo '/opt/opendevice/bin/opendevice start'
  printf "${GREEN}"
  echo 'Please follow us on facebook: https://www.facebook.com/opendevice/'
  echo ''
  printf "${NORMAL}"
}

main
