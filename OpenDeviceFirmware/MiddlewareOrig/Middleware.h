// Only modify this file to include
// - function definitions (prototypes)
// - include files
// - extern variable definitions
// In the appropriate section

#ifndef Middleware_H_
#define Middleware_H_
#include "Arduino.h"
//add your includes for the project OpenhouseMiddleware here


//end of add your includes here
#ifdef __cplusplus
extern "C" {
#endif
void loop();
void setup();
#ifdef __cplusplus
} // extern "C"
#endif

//add your function definitions for the project OpenhouseMiddleware here
void sensorChanged(uint8_t id, long value);
void dataReceived(byte flag, uint8_t numOfValues);
void processCommand();
void debug(char c );
void debug(const char str[]);

// Necessario para O Eclipse C++ reconhecer as variáveis da ENGERGIA IDE.
//extern HardwareSerial Serial;
//extern HardwareSerial Serial1;


//Do not add code below this line
#endif /* Middleware_H_ */
