// This is a demo of the RBBB running as webserver with the Ether Card
// 2010-05-28 <jc@wippler.nl> http://opensource.org/licenses/mit-license.php

#include <EtherCard.h>

// ethernet interface mac address, must be unique on the LAN
static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };
static byte myip[] = { 192,168,0,204 };

byte Ethernet::buffer[500];
BufferFiller bfill;

void setup () {
  
  Serial.begin(9600);
  pinMode(13, OUTPUT);
  
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) // 10 is pin for LEONARDO
    Serial.println( "Failed to access Ethernet controller");
  ether.staticSetup(myip);
  ether.persistTcpConnection(true);
  ether.clientTcpReq
  blinkLED(5);
}


static word homePage() {
  Serial.println("RECEBEU.");
  bfill = ether.tcpOffset();
  bfill.emit_p(PSTR("Recebido !!: \r\n"));
  blinkLED(2);
  return bfill.position();
}

void loop () {
  word len = ether.packetReceive();
  word pos = ether.packetLoop(len);
  
  //if (pos)  // check if valid tcp data is received
  //  ether.httpServerReply(homePage()); // send web page data
}


void blinkLED(int c){
  for(int i = 0; i < c; i++){
    _blinkLED();
  }
}

void _blinkLED(){
    digitalWrite(13, HIGH);
    delay(400);
    digitalWrite(13, LOW);
    delay(400);
}
