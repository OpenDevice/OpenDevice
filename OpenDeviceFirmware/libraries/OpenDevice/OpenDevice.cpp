/*
 * OpenDevice.cpp
 *
 *  Created on: 27/06/2014
 *      Author: ricardo
 */

#include "OpenDevice.h"

// Static member initialization...
DeviceConnection* OpenDevice::deviceConnection = NULL;
DeviceManager* OpenDevice::deviceManager = NULL;
Command OpenDevice::lastCMD;
bool OpenDevice::autoControl = false;

OpenDevice::OpenDevice() {
	init();
}

OpenDevice::OpenDevice(DeviceConnection *_deviceConnection, DeviceManager *_deviceManager){
	OpenDevice::deviceConnection = _deviceConnection;
	OpenDevice::deviceManager = _deviceManager;
	init();
}

//OpenDevice::~OpenDevice() {
//	// TODO Auto-generated destructor stub
//}


void OpenDevice::init() {
}

void OpenDevice::begin(DeviceManager* _deviceManager, DeviceConnection* _deviceConnection) {

	deviceManager = _deviceManager;
	deviceConnection = _deviceConnection;

	if(deviceManager){
		deviceManager->setDefaultListener(&(OpenDevice::sensorChanged));
		deviceManager->init(); // Init Sensors and Devices.
	}

	if(deviceConnection){
		deviceConnection->setDefaultListener(&(OpenDevice::onMessageReceived));
	}


}

void OpenDevice::loop() {

	if(deviceConnection){
		deviceConnection->checkDataAvalible();
	}

	if(deviceManager){
		deviceManager->checkStatus();
	}
}

void OpenDevice::onMessageReceived(Command cmd){
	OpenDevice::lastCMD = cmd;


	bool cont = true; /**  Chama handlers, se retornar false abota a continuacao; */

	if(!cont) return;

	if(cmd.type== CommandType::ON_OFF){ // TODO:MUDAR NOME? Device CONTROL. ?

		Device* device = deviceManager->getDevice(cmd.deviceID);

		debugChange(cmd.deviceID, cmd.value);

		if (device != NULL) {
			device->setValue(cmd.value);
			notifyReceived(ResponseStatus::SUCCESS);
		} else {
			notifyReceived(ResponseStatus::NOT_FOUND);
		}


	// Send response Ex: GET_DEVICES_RESPONSE;ID;[ID,PIN,VALUE,...];[ID,PIN,VALUE,...];[ID,PIN,VALUE,...]
	}else if(cmd.type== CommandType::GET_DEVICES){

		deviceConnection->doStart();
		deviceConnection->print(CommandType::GET_DEVICES_RESPONSE);
		deviceConnection->doToken();
		deviceConnection->print(cmd.id);
		deviceConnection->doToken();

		for (int i = 0; i < deviceManager->deviceLength; ++i) {
			Device *device = deviceManager->getDeviceAt(i);
			// Write array to connection..
			char buffer[50]; // FIXME: daria para usar o mesmo buffer do deviceConnection ??
			device->toString(buffer);
			deviceConnection->print(buffer);

			if(i < deviceManager->deviceLength){
				deviceConnection->doToken();
			}

		}

		deviceConnection->doEnd();

	}
}


void OpenDevice::sensorChanged(uint8_t id, uint16_t value){
	Device* sensor = deviceManager->getDevice(id);
	Device* device = deviceManager->getDevice(sensor->targetID);

//    if(id < 100) // FIXME: detectar se é o IR.
//    	value = !value;        // NOTA: Os valores do Swicth sao invertidos

   debugChange(id, value);

	if(autoControl){
		if(device != NULL){
			// Sepre que uma alteracao for detectada, será invertido o seu estado atual.
			// Caso o dispositivo seja digital, ele ira reconhecer apenas dois valores, 0..1
			if(device->type == Device::DIGITAL){
				device->setValue( ! device->getValue() );
			}else{
				long cval = device->getValue();
				if(cval == 0) device->setValue(Device::MAX_ANALOG_VALUE); // set max
				else device->setValue(0); // set max
			}
		}
	}

	// SEND: ANALOG_REPORT
	// ==========================
//	lastCMD.id = 0;
//	lastCMD.type = (sensor->type == Device::DIGITAL ? CommandType::ON_OFF : CommandType::ANALOG_REPORT);
//	lastCMD.deviceID = sensor->id;
//	lastCMD.value = value;
//	sendCommand(lastCMD);
}


void OpenDevice::sendCommand(Command cmd){
	if(deviceConnection){
		deviceConnection->send(cmd);
	}
}


/** Send reply stating that the command was received successfully */
void OpenDevice::notifyReceived(uint8_t status){
  lastCMD.type = CommandType::DEVICE_COMMAND_RESPONSE;
  lastCMD.value = status;
  lastCMD.data =  NULL;
  lastCMD.length = 0;
  sendCommand(lastCMD);
}

void OpenDevice::debugChange(uint8_t id, uint16_t value){

	if(DEBUG_MODE){
		// TODO: verificar se da pra usar o PRINT'F
		deviceConnection->doStart();
		deviceConnection->print("DB:CHANGE:");
		deviceConnection->print(id);
		deviceConnection->print("=");
		deviceConnection->print(value);
		deviceConnection->doEnd();
	}
}

void OpenDevice::freeRam() {

  extern int __heap_start, *__brkval;
  int v;
  Serial.print(F("DB:EPROM:"));
  Serial.print(E2END);
  Serial.print("-");
  Serial.print(F("DB:RAM:"));
  Serial.println((int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval));
}

void OpenDevice::debug(const __FlashStringHelper* data) {
	if(DEBUG_MODE){
		deviceConnection->doStart();
		deviceConnection->print("DB:");
		deviceConnection->print(data);
		deviceConnection->doEnd();
	}
}

void OpenDevice::debug(const char str[]){
	if(DEBUG_MODE){
		deviceConnection->doStart();
		deviceConnection->print("DB:");
		deviceConnection->print(str);
		deviceConnection->doEnd();
	}
}


