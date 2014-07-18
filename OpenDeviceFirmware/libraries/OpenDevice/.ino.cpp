//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2014-06-22 21:26:32

#include "Arduino.h"
#include "Middleware.h"
#include <OpenDeviceConnection.h>
#include <Command.h>
#include <DeviceManager.h>
#include <SoftwareSerial.h>
#include <IRremote.h>
void setup() ;
void loop() ;
void sensorChanged(uint8_t id, long value) ;
void dataReceived(byte flag, uint8_t numOfValues);
void processCommand();
void notifyReceived(int status);
void setStatusLed(int deviceID, long value);
void sendCommand(long cmd[]);
void sendCommandCached();
void debug(char c );
void debug(const char str[]);
void debug(String str);
void debugChange(int id, long value);


#include "Middleware.ino"
