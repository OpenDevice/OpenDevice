
// Includes
#include "Arduino.h"
#include "HardwareSerial.h"
#include "DeviceConnection.h"

#ifdef __LM4F120H5QR__
	#include "itoa.h" // For Stellaris Lauchpad
#endif


extern "C" {
	#include <stdlib.h>
}



// public methods
DeviceConnection::DeviceConnection(){
	init();
}

DeviceConnection::DeviceConnection(Stream &stream) {
	com = &stream;
	init();
}


void DeviceConnection::init(){

	numberOfValues = 0;
	waitTime = 30;

	defaultListener = NULL;
	for(int a = 0;a < MAX_LISTENERS;a++){
		listeners[a] = NULL;
		listeners_key[a] = NULL;
	}
}


void DeviceConnection::addListener(uint8_t commandType, CommandListener listener ){
	if (commandType >= 0 && commandType < MAX_LISTENERS){
		listeners[listenersLength] = listener;
		listeners_key[listenersLength] = commandType;
		listenersLength++;
	}
}

void DeviceConnection::setDefaultListener(CommandListener listener ){
	defaultListener = listener;
}

void DeviceConnection::removeListener(uint8_t command){

	// Only remove pointers
	for (uint8_t i = 0; i < listenersLength; ++i) {
		if(listeners_key[i] == command){
			listeners_key[i] = NULL;
			listeners[i] = NULL;
			break;
		}
	}

}

bool DeviceConnection::checkDataAvalible(){
	uint8_t lastByte;
	boolean timeout = false;
	while(!timeout)
	{
		while(com->available() > 0)
		{
			lastByte = com->read();
			if(lastByte == STOP_BIT){
				flush();
			}
			else if(lastByte == ACK_BIT){
				parseCommand();
				flush();
			}
			else if(bufferCount < DATA_BUFFER){
				buffer[bufferCount] = lastByte;
				bufferCount++;
			}
			else return false;
		}
		
		if(com->available() <= 0 && !timeout){
			if(waitTime > 0) delayMicroseconds(waitTime);
			if(com->available() <= 0) timeout = true;
		}
	}
	return timeout;
}


// Private methods
void DeviceConnection::notifyListeners(Command cmd){

	Serial.println(F("ETAPA:2:notifyListeners"));

	if(defaultListener) (*defaultListener)(cmd);
	// Notify specific listener
	for (uint8_t i = 0; i < listenersLength; ++i) {
		if(listeners_key[i] == cmd.type){ // confirm type
			(*listeners[i])(cmd);
			break;
		}
	}
	Serial.println(F("ETAPA:3:notifyListeners"));

}

void DeviceConnection::parseCommand(){
	Command cmd;
	int data[4];
	readIntValues(data);
	cmd.type = (uint8_t) data[0];
	cmd.id = (uint8_t) data[1];
	cmd.deviceID = (uint8_t) data[2];
	cmd.value = (uint16_t) data[3]; // TODO: Observar conversão de INT -> LONG
	// cmd.data = buffer; // TODO: adicionar o restante dos dados.

	// TODO: verificar se é necessario , já que é uma função normal,
	memset(data, 0, sizeof data);
	memset(buffer, 0, sizeof buffer);
	// memset(data, 0, nMembers * (sizeof data[0]) );
	notifyListeners(cmd);
}

void DeviceConnection::getBuffer(uint8_t buf[]){

	for(int a = 0;a < bufferCount;a++){
		buf[a] = buffer[a];
	}
}

void DeviceConnection::readString(char string[]){

	for(int a = 1;a < bufferCount;a++){
		string[a-1] = buffer[a];
	}
	string[bufferCount-1] = '\0';
}

int DeviceConnection::readInt()
{
	uint8_t b[bufferCount];
	for(int a = 1;a < bufferCount;a++){ // FIXME: não ira começar mais do INDEX 1 (que era que o amarino usava... !!!)
		b[a-1] = buffer[a];
	}

	b[bufferCount-1] = '\0';
	return atoi((char*)b);
}

long DeviceConnection::readLong()
{
	uint8_t b[bufferCount];
	for(int a = 1;a < bufferCount;a++){
		b[a-1] = buffer[a];
	}

	b[bufferCount-1] = '\0';
	return atol((char*)b);
}

float DeviceConnection::readFloat()
{
	return (float)readDouble();
}

int DeviceConnection::getArrayLength()
{
	if (bufferCount == 1) return 0; // only a flag and ACK_BIT was sent, not data attached
	numberOfValues = 1;
	// find the amount of values we got
	for (int a=1; a<bufferCount;a++){
		if (buffer[a]==TOKEN_BIT) numberOfValues++;
	}
	return numberOfValues;
}

void DeviceConnection::readFloatValues(float values[])
{
	int t = 0; // counter for each char based array
	int pos = 0;

	int start = 1; // start of first value
	for (int end=1; end<bufferCount;end++){
		// find end of value
		if (buffer[end]==TOKEN_BIT) {
			// now we know start and end of a value
			char b[(end-start)+1]; // create container for one value plus '\0'
			t = 0;
			for(int i = start;i < end;i++){
				b[t++] = (char)buffer[i];
			}
			b[t] = '\0';
			values[pos++] = atof(b);
			start = end+1;
		}
	}
	// get the last value
	char b[(bufferCount-start)+1]; // create container for one value plus '\0'
	t = 0;
	for(int i = start;i < bufferCount;i++){
		b[t++] = (char)buffer[i];
	}
	b[t] = '\0';
	values[pos] = atof(b);
}

// not tested yet
void DeviceConnection::readDoubleValues(float values[])
{
	readFloatValues(values);
}

// not tested yet
void DeviceConnection::readIntValues(int values[])
{
	int t = 0; // counter for each char based array
	int pos = 0;

	int start = 1; // start of first value
	for (int end=1; end<bufferCount;end++){
		// find end of value
		if (buffer[end]==TOKEN_BIT) {
			// now we know start and end of a value
			char b[(end-start)+1]; // create container for one value plus '\0'
			t = 0;
			for(int i = start;i < end;i++){
				b[t++] = (char)buffer[i];
			}
			b[t] = '\0';
			values[pos++] = atoi(b);
			start = end+1;
		}
	}
	// get the last value
	char b[(bufferCount-start)+1]; // create container for one value plus '\0'
	t = 0;
	for(int i = start;i < bufferCount;i++){
		b[t++] = (char)buffer[i];
	}
	b[t] = '\0';
	values[pos] = atoi(b);
}


double DeviceConnection::readDouble()
{
	char b[bufferCount];
	for(int a = 1;a < bufferCount;a++){
		b[a-1] = (char)buffer[a];
	}

	b[bufferCount-1] = '\0';
	return atof(b);
	
}


#if defined(ARDUINO) && ARDUINO >= 100
size_t DeviceConnection::write(uint8_t b){
	return com->write(b);
}
#else
void DeviceConnection::write(uint8_t b){
	com->write(b);
}
#endif
	


void DeviceConnection::doStart(){
	com->write(START_BIT);
}

void DeviceConnection::doToken(){
	com->write(TOKEN_BIT);
}

void DeviceConnection::doEnd(){
	com->write(ACK_BIT);
}


void DeviceConnection::send(char c ){
	com->write(START_BIT);
	com->write(c);
	com->write(ACK_BIT);
}

void DeviceConnection::send(const char str[]){
	com->write(START_BIT);
	com->write(str);
	com->write(ACK_BIT);
}

void DeviceConnection::send(long values[], int size){
	com->write(START_BIT);
	char vbuffer[3];
	for (int i = 0; i < size; ++i) {
		ltoa(values[i], vbuffer, 10);
		com->write(vbuffer);
		com->write(TOKEN_BIT);
	}
	com->write(ACK_BIT);
}


void DeviceConnection::send(int values[], int size){
	com->write(START_BIT);
	char vbuffer[3];
	for (int i = 0; i < size; ++i) {
		itoa(values[i], vbuffer, 10);
		com->write(vbuffer);
		com->write(TOKEN_BIT);
	}
	com->write(ACK_BIT);
}

void DeviceConnection::send(uint8_t n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::send(int n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::send(unsigned int n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::send(long n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::send(unsigned long n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::send(long n, int base){
	com->write(START_BIT);
	com->print(n, base);
	com->write(ACK_BIT);
}
void DeviceConnection::send(double n){
	com->write(START_BIT);
	com->write(n);
	com->write(ACK_BIT);
}
void DeviceConnection::sendln(void){
	com->write(START_BIT);
	com->println();
	com->write(ACK_BIT);
}

void DeviceConnection::send(Command cmd){
	com->write(START_BIT);
	com->print(cmd.type);
	com->write(TOKEN_BIT);
	com->print(cmd.id);
	com->write(TOKEN_BIT);
	com->print(cmd.deviceID);
	com->write(TOKEN_BIT);
	com->print(cmd.value);
	com->write(ACK_BIT);
}


void DeviceConnection::flush(){
	for(uint8_t a=0; a < DATA_BUFFER; a++){
		buffer[a] = 0;
	}
	bufferCount = 0;
	numberOfValues = 0;
}
