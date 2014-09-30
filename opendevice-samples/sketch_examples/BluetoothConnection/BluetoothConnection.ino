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
#include <SoftwareSerial.h>

SoftwareSerial serialBT(8, 7); // RX  TX
DeviceConnection deviceConnection(serialBT);


void setup(){
    OpenDevice::debugMode = true;
    OpenDevice::debugTarget = 0; // 0:Serial, 1:DeviceConnection

    serialBT.begin(9600);
    Serial.begin(9600);
    while (!Serial){delay(1);} // uncomment if using Leonardo

    OpenDevice::addDevice(13, Device::DIGITAL); // ID:1
    
    OpenDevice::begin(deviceConnection);
    OpenDevice::debug("Started!");
         
}

void loop(){
	
	OpenDevice::loop();

}

