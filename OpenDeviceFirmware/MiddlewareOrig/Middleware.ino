#include "Middleware.h"
#include <MeetAndroid.h>
#include <Command.h>
#include <DeviceManager.h>
 
// Include SoftSerial for ARDUINO UNO or similar.
#if !defined(UBRR1H) && !defined(ENERGIA)
#include <SoftwareSerial.h>
#endif

// Include IRremote for ARDUINO UNO or similar.
// Essa if IGNORA O Stellaris Laucpad
#ifndef ENERGIA
// #include <IRremote.h> // Necessários, pois o ARDUINO não enxerga include de include..
#include <IRSensor.h>
#endif

// ========================================================================
// Hardware Adaptions end Definitions
// ========================================================================

// Necessario para O Eclipse C++ reconhecer as variáveis da ENGERGIA IDE
// TODO: remover assim que terminar os testes, basicos...
/*
#ifndef __LM4F120H5QR__
	static const uint8_t PUSH1 = 31;
	static const uint8_t PUSH2 = 17;

	static const uint8_t RED_LED = 30;
	static const uint8_t GREEN_LED = 39;
	static const uint8_t BLUE_LED = 40;

	HardwareSerial BSerial(1);
#endif
*/

// Include SoftSerial for ARDUINO UNO.
#if !defined(UBRR1H) && !defined(ENERGIA)
   #define BSerial SERIAL_1

   // It uses analog pins (0=RX, 1=TX), equivalent to 14, 15 in UNO
   SoftwareSerial BSerial(14, 15); //(RX, TX)
   
// Arduino MEGA  
#elif defined(UBRR1H)  
  #define BSerial Serial1

// Energia (Stellaris Lauchpad)
#elif defined(ENERGIA)  
  #define BSerial Serial1
  
#endif


// Apenas para incluir algumas variaveis da Energia no ARDUINO.
#ifndef ENERGIA
	static const uint8_t PUSH1 = 0;
	static const uint8_t PUSH2 = 1;

	static const uint8_t RED_LED = 13;
	static const uint8_t GREEN_LED = 13;
	static const uint8_t BLUE_LED = 13;    
#endif
// TODO: Codigo para rodar no ARDUINO ---------
void sensorChanged(uint8_t id, long value);
void debugChange(int id, long value);
void setStatusLed(int deviceID, long value);
void sendCommand(long cmd[]);
void notifyReceived(int status);
void sendCommandCached();
// --------------------------------------------



// ========================================================================
// Variables
// ========================================================================

MeetAndroid meetAndroid(&BSerial);
DeviceManager deviceManager;

Command cmd; // Command received.
long cmdBuffer[4]; // Buffer to send commands


/* Indica que o proprio controlador ao detectar uma alteracao nos Sensores, deve LIGAR/DESLIGAR os dispositivos */
bool AUTO_CONTROL_DEVICE = true;
bool USE_STATUS_LED = false;
bool DEBUG_USB = true;


//The setup function is called once at startup of the sketch
void setup()
{
       BSerial.begin(9600);
       if(DEBUG_USB){
          Serial.begin(9600);
          debug("DB:Starting..");
        }

        //============================================= 
        // Arduino
        //=============================================
        #ifndef ENERGIA

	deviceManager.addDevice(1, 16, Device::DIGITAL);
	deviceManager.addDevice(2, 17, Device::DIGITAL);
	deviceManager.addDevice(3, 18, Device::DIGITAL);
	deviceManager.addDevice(4, 19, Device::DIGITAL);
        
        deviceManager.addSensor(new IRSensor(51, 11, &Serial)); // PIN 11
        // TODO: add IRDevice (Porta de Saida IR)
        
        #endif
 
        //============================================= 
        // Stellaris
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
    
	// Callbaks, para eventos que forem lançados no Bluetooth.
	meetAndroid.registerFunction(dataReceived, 'A');

	// Callbaks, para eventos nos Interruptores/Switches.
	deviceManager.setCallBack(&sensorChanged);

	deviceManager.init(); // Init Sensors and Devices.

	debug("DB:Started!");
        
}

// The loop function is called in an endless loop
void loop()
{
	meetAndroid.receive(); // Verificar se algum dado foi recebido.

	deviceManager.checkStatus(); // Verificar se algum evento ocorreu.

}

/** Evento chamado quando ha alguma alteração nos Interruptores(switch) ou Sensores */
void sensorChanged(uint8_t id, long value) {
  
	Sensor* sensor = deviceManager.getSensor(id);
	Device* device = deviceManager.getDevice(sensor->getTargetID());

//    if(id < 100) // FIXME: detectar se é o IR.
//    	value = !value;        // NOTA: Os valores do Swicth sao invertidos

   debugChange(id, value);

	if(AUTO_CONTROL_DEVICE){
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
                  
                  // Change LED
                  setStatusLed(sensor->getTargetID(), device->getValue());

		}
	}

	// Report event's
	// ==========================

	if(device != NULL){
		cmdBuffer[0] = (device->type == Device::DIGITAL ? CommandType::ON_OFF : CommandType::POWER_LEVEL);
		cmdBuffer[2] = device->id;
	}else{ // Provavelmente de um Sensor
		cmdBuffer[0] = CommandType::INFRA_RED; // TODO: Codigo fixo...
		cmdBuffer[2] = sensor->id;
	}
	cmdBuffer[1] = -1;
	cmdBuffer[3] = value;
	sendCommand(cmdBuffer);

}

/** Quando algum dado for recebido pelo bluetooth */
void dataReceived(byte flag, uint8_t numOfValues){
  int data[numOfValues];
  meetAndroid.getIntValues(data);
  

  cmd.type = data[0];
  cmd.id = data[1];
  cmd.deviceID = data[2];
  cmd.value = data[3]; // TODO: Observar conversão de INT -> LONG
  processCommand();
}


void processCommand(){
	Device* device = deviceManager.getDevice(cmd.deviceID);
        
	debugChange(cmd.deviceID, cmd.value);
        
	if(device != NULL){
		device->setValue(cmd.value);
		setStatusLed(cmd.deviceID, cmd.value);
                notifyReceived(ResponseStatus::SUCCESS);
	}else{
                notifyReceived(ResponseStatus::NOT_FOUND);
        }
}

/** Notifica que o comando foi recebido com successo */
void notifyReceived(int status){
  debug("DB:notifyReceived !");
  cmd.type = CommandType::DEVICE_COMMAND_RESPONSE;
  //  cmd.id; same ID.
  cmd.deviceID = -1;
  cmd.value = status; 
  sendCommandCached();
}

// FIXME: HARDCODED - Coodigo fixo apenas para testes, deve
// ser removido...
void setStatusLed(int deviceID, long value){
   
  if(!USE_STATUS_LED) return;
  
  if(deviceID == 1) digitalWrite(RED_LED, value);
  if(deviceID == 2) digitalWrite(GREEN_LED, value);
  if(deviceID == 3) digitalWrite(BLUE_LED, value);
  if(deviceID == 4){
	digitalWrite(RED_LED, value);
	digitalWrite(GREEN_LED, value);
  } 

}

// FIXME: User a variavel command, e internalmente converter para o array.
void sendCommand(long cmd[]){
	meetAndroid.send(cmd, 4); // TODO: Obervar a conversão interna desse método.
}

void sendCommandCached(){
  cmdBuffer[0] = cmd.type;
  cmdBuffer[1] = cmd.id;
  cmdBuffer[2] = cmd.deviceID;
  cmdBuffer[3] = cmd.value;
  sendCommand(cmdBuffer);
};

void debug(char c ){
	meetAndroid.send(c);
	if(DEBUG_USB) Serial.print(c);
}

void debug(const char str[]){
	meetAndroid.send(str);
	if(DEBUG_USB) Serial.println(str);
}

void debug(String str){
//	char dgbarray[str.length() + 1];
//	str.toCharArray(dgbarray, str.length() + 1, 0);
//	meetAndroid.send(dgbarray);
	if(DEBUG_USB) Serial.println(str);
}

void debugChange(int id, long value){
//  	String dbgstr =  "DBG:CHANGE:";
//	dbgstr.concat(id);
////	dbgstr.concat(value);
//	debug(dbgstr);

	if(DEBUG_USB){
		Serial.print("DBG:CHANGE:");
		Serial.print(id);
		Serial.print("=");
		Serial.print(value, DEC);
		Serial.print("->");
		Serial.print(value, HEX);
		Serial.println();
	}
}

