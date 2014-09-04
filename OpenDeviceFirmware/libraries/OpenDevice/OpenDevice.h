/*
 * OpenDevice.h
 *
 *  Created on: 27/06/2014
 *      Author: ricardo
 */

#ifndef OPENDEVICE_H_
#define OPENDEVICE_H_

#include <Arduino.h>
#include "Command.h"
#include "DeviceConnection.h"
#include "DeviceManager.h"

#define DEBUG_MODE 0

class OpenDevice {

private:
	static void init();

	// Internal Listeners..
	static void onMessageReceived(Command);
	static void notifyReceived(uint8_t);
	static void sensorChanged(uint8_t id, uint16_t value);

	// Utils....
	static void freeRam();
	static void debugChange(uint8_t id, uint16_t value);



public:

	static bool autoControl; // Changes in the sensor should affect bonded devices..
	static Command lastCMD; // Command received / send.
	static DeviceConnection *deviceConnection;
	static DeviceManager *deviceManager;

	OpenDevice();
	OpenDevice(DeviceConnection*, DeviceManager*);
	// virtual ~OpenDevice();
	static void begin(DeviceManager*, DeviceConnection*);
	static void loop();


	static void sendCommand(Command cmd);
	static void debug(const __FlashStringHelper* data);
	static void debug(const char str[]);

};

#endif /* OPENDEVICE_H_ */
