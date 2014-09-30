/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

// See tutorial:
// https://opendevice.atlassian.net/wiki/display/DOC/Arduino+Blink+Demo

#include <DeviceConnection.h>
#include <OpenDevice.h>

DeviceConnection deviceConnection(Serial);
 
void setup(){
    OpenDevice::debugMode = true;
    Serial.begin(9600);  
    while (!Serial){delay(1);} // uncomment if using Leonardo
   
    OpenDevice::addDevice(13, Device::DIGITAL); // ID:1
    
    OpenDevice::begin(deviceConnection);			
    OpenDevice::debug("Started!");
         
}

void loop(){
	
	OpenDevice::loop();

//	  // if we get a valid byte, read analog ins:
//      if (Serial.available() > 0) {
//        // get incoming byte:
//        int inByte = Serial.read();
//        Serial.print("READ:");
//        Serial.println(inByte);
//     }

}

