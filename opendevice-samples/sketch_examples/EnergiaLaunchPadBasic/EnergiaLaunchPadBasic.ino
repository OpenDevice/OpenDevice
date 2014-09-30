

// TODO: ADICIONAR DOCUMENTAÇO E LIN PARA O TUTORIAL...
#include <OpenDevice.h>

// Connection Setup.
DeviceConnection deviceConnection(Serial1); // PC4=RX1,PC5_TX1 on Lauchpad


void setup(){
  OpenDevice::debugMode = true;
  OpenDevice::debugTarget = 0;
  
  Serial.begin(9600);
  Serial1.begin(9600); // Bluetooth
  

  OpenDevice::debug("Starting...");

  OpenDevice::addDevice(RED_LED, Device::DIGITAL);   // ID:1
  OpenDevice::addDevice(GREEN_LED, Device::DIGITAL); // ID:2
  OpenDevice::addDevice(BLUE_LED, Device::DIGITAL);  // ID:3

  OpenDevice::addSensor(PUSH1, Device::DIGITAL, 1); // ID:4
  OpenDevice::addSensor(PUSH2, Device::DIGITAL, 2); // ID:5
			
  OpenDevice::begin(deviceConnection);			
  OpenDevice::debug("Started!");

}

void loop(){
  
  OpenDevice::loop();
 
}


