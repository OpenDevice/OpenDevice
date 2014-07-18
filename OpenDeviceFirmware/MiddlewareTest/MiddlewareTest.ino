
#include <Command.h>
#include <DeviceConnection.h>
#include <DeviceManager.h>
#include <OpenDevice.h>

#include <UIPEthernet.h>
#include <ConnectionENC28J60.h>

// Include SoftSerial for ARDUINO UNO or similar.
#if defined (__AVR_ATmega328P__) || defined (__AVR_ATmega328__)
#include <SoftwareSerial.h>
#endif

// Include IRremote for ARDUINO UNO or similar.
// Essa if IGNORA O Stellaris Laucpad
#ifndef ENERGIA
// #include <IRremote.h> // Necessários, pois o ARDUINO não enxerga include de include..
#endif


// ========================================================================
// Hardware Adaptions end Definitions
// ========================================================================


// ARDUINO UNO (Use SoftwareSerial)
#if defined (__AVR_ATmega328P__) || defined (__AVR_ATmega328__)

   // It uses analog pins (0=RX, 1=TX), equivalent to numbers 14,15 in UNO
   SoftwareSerial BSerial(14, 15); //(RX, TX)
   
// Arduino MEGA 1280 / MEGA 2560 /  Leonardo (Use Hardware Serial RX, TX Pins )
#elif defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__) || defined(__AVR_ATmega32U4__)
  #define BSerial Serial1

// Energia (Stellaris Lauchpad)
#elif defined(ENERGIA)  
  #define BSerial Serial1 // Pins (PC4=RX, PC5=TX)
  
#endif



// ========================================================================
// Variables
// ========================================================================

// OpenDeviceConnection openDevice (&BSerial);

ConnectionENC28J60 deviceConnection(8081);

DeviceManager deviceManager;

Command lastCMD; // Command received / send.

bool DEBUG_USB = true;

//The setup function is called once at startup of the sketch
void setup()
{
       BSerial.begin(9600);
       Serial.begin(9600);
       while (!Serial){delay(1);} // wait to open
       
       if(DEBUG_USB) OpenDevice::debug(F("Starting..."));

        //============================================= 
        // Arduino
        //=============================================
        #ifndef ENERGIA

			deviceManager.addDevice(10, Device::DIGITAL); // ID:1
			deviceManager.addDevice(11, Device::DIGITAL); // ID:2
			deviceManager.addDevice(12, Device::DIGITAL); // ID:3
			deviceManager.addDevice(13, Device::DIGITAL); // ID:4
			
			deviceManager.addSensor(0, Device::ANALOG, 0); //  ID:5, Pin A0, target:0(none)
			
//			pinMode(A1, INPUT);
//			pinMode(A2, INPUT);
//			pinMode(A3, INPUT);
//			pinMode(A4, INPUT);
//			pinMode(A5, INPUT);
//			
//			digitalWrite(A1, HIGH);
//			digitalWrite(A2, HIGH);
//			digitalWrite(A3, HIGH);
//			digitalWrite(A4, HIGH);
//			digitalWrite(A5, HIGH);
        
        // deviceManager.addSensor(new IRSensor(51, 11, &Serial)); // PIN 11
        // TODO: add IRDevice (Porta de Saida IR)
        
        #endif
 
        //============================================= 
        // Stellaris  (Use Internal LEDS and PUSH)
        //=============================================       
        #ifdef ENERGIA

			deviceManager.addDevice(1, RED_LED, Device::DIGITAL);
			deviceManager.addDevice(2, GREEN_LED, Device::DIGITAL);
			deviceManager.addDevice(3, BLUE_LED, Device::DIGITAL);

			deviceManager.addSensor(1, PUSH1, 1);
			deviceManager.addSensor(2, PUSH2, 3);

			//deviceManager.addDevice(1, PE_1, Device::DIGITAL);
			//deviceManager.addDevice(2, PE_2, Device::DIGITAL);
			//deviceManager.addDevice(3, PE_3, Device::DIGITAL);
			//deviceManager.addDevice(4, PF_1, Device::DIGITAL);

        #endif

    // TODO: falta configurar o IP..			
    OpenDevice::begin(&deviceManager, &deviceConnection);			
	OpenDevice::debug(F("Started!"));
        
}

// The loop function is called in an endless loop
void loop(){
	
	OpenDevice::loop();

}

