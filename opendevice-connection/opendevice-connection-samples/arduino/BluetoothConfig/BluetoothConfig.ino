#include "Arduino.h"

// If Arduino
#ifndef ENERGIA
  #include <SoftwareSerial.h>
  SoftwareSerial BSerial(14, 15); //(RX, TX)
#endif

// If Energia IDE 
#ifdef ENERGIA  
  #define BSerial Serial1  // PC4=RX1,PC5_TX1 on 
#endif


void setup(){
  Serial.begin(9600);
  BSerial.begin(9600);
  
  Serial.print("Init:OK");
  BSerial.print("AT+VERSION"); 
  delay(1000);
  
  // Configuration
  //BSerial.print("AT+NAMEBT-MCU-3"); // SET NAME
  //delay(1000);
  //BSerial.print("AT+PIN1234");
  //delay(1000);
  //BSerial.print("AT+BAUD4"); // Set baudrate to 9600
  //BSerial.print("AT+BAUD7"); // Set baudrate to 57600  
  //delay(1000);

}

void loop(){
  if (BSerial.available())
    Serial.write(BSerial.read());
    
  // Manual Configuration using Serial Monitor
  // USE: No Line Ending on HC-06 !!!
  if (Serial.available())
    BSerial.write(Serial.read());
}