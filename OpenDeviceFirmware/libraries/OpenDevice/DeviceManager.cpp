/*
 * DeviceManager.cpp
 *
 *  Created on: 05/02/2013
 *      Author: Ricardo JL Rufino
 */

#include "DeviceManager.h"



// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Device
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Device::Device(){
	_init(0, 0, DIGITAL, false);
}

Device::Device(uint8_t _id, uint8_t _pin, DeviceType _type){
	_init(_id, _pin, _type, false);
}

Device::Device(uint8_t _id, uint8_t _pin, DeviceType _type, bool _sensor){
	_init(_id, _pin, _type, _sensor);
}

void Device::_init(uint8_t _id, uint8_t _pin, DeviceType _type, bool _sensor){

	id = _id;
	pin = _pin;
	type = _type;
	sensor = _sensor;
	targetID = 0;

	if(_sensor){
		currentValue = LOW;
	}else{
		currentValue = HIGH; //TODO: isso deve ser difinido, pois tomada é HIGHT e lanpada deve ser LOW
	}
}

bool Device::setValue(uint16_t value){
	currentValue = value;

	if(type == Device::DIGITAL){
		digitalWrite(pin, (value == 1 ? HIGH : LOW));
	}else{
		analogWrite(pin, value);
	}

	return true;
}

uint16_t Device::getValue(){

	if(sensor){
		return currentValue; // return last filtered value... (see: hasChanged)
	}else{
		if(type == Device::DIGITAL){
			return digitalRead(pin);
		}else{
			return analogRead(pin);
		}
	}

}

// FOR SENSOR device...
bool Device::hasChanged(){

	uint16_t v = 0;

	if(type == Device::DIGITAL){
		v = ! digitalRead(pin); // READ and Invert state because is a INPUT_PULLUP
	}else{
		v = analogRead(pin);
		//if(currentValue != v){
			// call analog filter...
		//}
	}

	// check if has changed
	if(currentValue != v){
		currentValue = v;
		return true;
	}

	return false;
}

void Device::init(){
	// Do nothing for now.
}


// [ID, PIN, VALUE, TARGET, SENSOR?, TYPE]
int Device::toString(char buffer[]){
	return sprintf (buffer, "[%d,%d,%d,%d,%d,%d]", id, pin, getValue(), targetID, sensor, type);
}


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// DeviceManager
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


DeviceManager::DeviceManager() {
	init();
}

void DeviceManager::setDefaultListener(void (*pt2Func)(uint8_t, uint16_t)){
	callbackPtr = pt2Func;
}


void DeviceManager::init(){
	time = 0;
    for (int i = 0; i < deviceLength; i++) {
    	devices[i].init();
    }
}


bool DeviceManager::addSensor(uint8_t pin, Device::DeviceType type, uint8_t targetID){
	bool v = addDevice(pin, type, true, 0);
	if(v) devices[deviceLength-1].targetID = targetID;
	return v;
}

bool DeviceManager::addDevice(uint8_t pin, Device::DeviceType type){
	return addDevice(pin, type, false, 0);
}

bool DeviceManager::addDevice(uint8_t pin, Device::DeviceType type, bool sensor, uint8_t id){
	  if (deviceLength < MAX_DEVICE) {

		if(sensor){
			if(type && Device::DIGITAL){
				pinMode(pin, INPUT_PULLUP); // Enable internal pull-up resistor..
			}
		}else{
			pinMode(pin, OUTPUT);
		}

		if(id == 0) id = deviceLength + 1;
	    devices[deviceLength] = Device(id, pin, type, sensor);
	    deviceLength++;

	    return true;
	  }
	  else return false;
}

void DeviceManager::checkStatus(){

	// Arduino DOC (http://arduino.cc/en/Reference/analogRead):
	// Takes about 100 microseconds (0.0001 s) to read an analog input, so the maximum reading rate is about 10,000 times

	if(time == 0) time = millis();

	if (millis() - time > READING_INTERVAL){ // don't sample analog/digital more than XXXms
	    for (int i = 0; i < deviceLength; i++) {
	    	Device *device = (Device*) &devices[i];
	    	if(device->sensor && device->hasChanged()){
	    		(*callbackPtr)(device->id, device->currentValue); // call calback function..
	    	}
	    }

	    time = millis();
	}

}

void DeviceManager::setValue(uint8_t id, uint16_t value){
    for (int i = 0; i < deviceLength; i++) {
    	if(devices[i].id == id){
    		devices[i].setValue(value);
    		break;
    	}
    }
}

void DeviceManager::sendToAll(uint16_t value){
    for (int i = 0; i < deviceLength; i++) {
    	devices[i].setValue(value);
    }
}

Device* DeviceManager::getDevice(uint8_t id){
    for (int i = 0; i < deviceLength; i++) {
    	if(devices[i].id == id){
    		return &devices[i];
    	}
    }

    return NULL;
}

Device* DeviceManager::getDeviceAt(uint8_t index){

	if(index > 0 && index < deviceLength){
		return &devices[index];
	}

    return NULL;
}

