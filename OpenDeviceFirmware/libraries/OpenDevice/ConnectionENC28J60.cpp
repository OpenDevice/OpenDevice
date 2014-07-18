/*
 * ConnectionENC28J60.cpp
 *
 *  Created on: 22/06/2014
 *      Author: ricardo
 */

#include "ConnectionENC28J60.h"

ConnectionENC28J60::ConnectionENC28J60(uint16_t port) : DeviceConnection(){
	_port = port;
	 server = new EthernetServer(_port);
	 client = NULL;
	_connected = false;
}

ConnectionENC28J60::~ConnectionENC28J60() {
	// TODO Auto-generated destructor stub
}

void ConnectionENC28J60::init(){
	// OpenDeviceConnection::init();
}

void ConnectionENC28J60::connect(){
    uint8_t mac[6] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
    IPAddress myIP(192, 168, 0, 204);
    Ethernet.setSSPIN(4, 4); // Default is 10;
    Ethernet.begin(mac, myIP);
	server->begin();
}

bool ConnectionENC28J60::checkDataAvalible(void) {

	// Serial.println("DB:WAITING..");

	if(!_connected){
		connect();
		delay(1000);
		_connected = true;
	}


	if (EthernetClient newClient = server->available()) {

		// only if new Client.
		if(!client || client != &newClient){

			client = &newClient;

			Serial.println("DB:CONNECTED!");
		}

	}

	if (client && client->connected()) {

		com = (Stream*) client;
		DeviceConnection::checkDataAvalible(); // Call supper class

	}
}
