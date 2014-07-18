/*
 * DeviceManager.h
 *
 *  Created on: 05/02/2013
 *      Author: Ricardo JL Rufino
 */

#ifndef DeviceManager_H_
#define DeviceManager_H_

#include <Arduino.h>

#define MAX_DEVICE 10
#define READING_INTERVAL 100 // sensor reading interval (ms)

class Device
{
public:

	enum DeviceType{
			ANALOG = 1,
			DIGITAL = 2
	};

	const static uint8_t MAX_ANALOG_VALUE = 255;

	uint8_t id;
	uint8_t pin;
	uint16_t currentValue;
	uint8_t targetID; // associated device (used in sensors)

	bool sensor;
	DeviceType type;

	Device();
	Device(uint8_t iid, uint8_t ipin, DeviceType type);
	Device(uint8_t iid, uint8_t ipin, DeviceType type, bool sensor);

	/**
	 * Change value / state of Device
	 */
	bool setValue(uint16_t value);

	/**
	 * Get current value.
	 */
	uint16_t getValue();

	virtual bool hasChanged();

	virtual void init();

	void _init(uint8_t iid, uint8_t ipin, DeviceType type, bool sensor);

	int toString(char buffer[]);
};


class DeviceManager {
private:
	  Device devices[MAX_DEVICE];

	  // Debouncng of normal pressing (for Sensor's)
	  long time;

	  bool addDevice(uint8_t pin, Device::DeviceType type, bool sensor, uint8_t id);

	  /**
	   * Registered Callback function
	   * Params: pinNumber, newValue
	   */
	  void (*callbackPtr)(uint8_t, uint16_t);


public:

	uint8_t deviceLength;

	DeviceManager();
	bool addSensor(uint8_t pin, Device::DeviceType type, uint8_t targetID);
	// bool addSensor(Sensor* sensor);
	bool addDevice(uint8_t pin, Device::DeviceType type);
	Device* getDevice(uint8_t);
	Device* getDeviceAt(uint8_t);

	void setDefaultListener(void (*pt2Func)(uint8_t, uint16_t));
	void checkStatus();
	void setValue(uint8_t id, uint16_t value);
	void sendToAll(uint16_t value);
	void init();

};

#endif /* DeviceManager_H_ */
