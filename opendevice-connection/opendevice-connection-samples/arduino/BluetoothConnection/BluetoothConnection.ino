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


#define LED RED_LED // Change LED PIN (in arduino is 13)
// #define LED 13


void setup(){
  Serial.begin(9600);
  BSerial.begin(9600);
  pinMode(LED, OUTPUT);
  Serial.print("Init:OK");
  delay(1000);
}


void loop(){

  if (BSerial.available() > 0) {

    byte value = BSerial.read();  // read value
    Serial.print("READ:");
    Serial.println(value);
    BSerial.print("READ:");
    BSerial.println(value);

    if(value == HIGH || value == LOW ){
      digitalWrite(RED_LED,value);
    }

  }
}


