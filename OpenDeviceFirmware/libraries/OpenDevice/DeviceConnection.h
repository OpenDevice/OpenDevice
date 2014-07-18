
#ifndef DeviceConnection_h
#define DeviceConnection_h

#include <inttypes.h>
#include "Print.h"
#include "Stream.h"
#include "Command.h"


extern "C"
{
  // Definition of the listener function
  typedef void (*CommandListener) (Command);
}

/******************************************************************************
* Definitions
******************************************************************************/

/**
 * Class que implements the application level protocol of the OpenDevice.
 */
class DeviceConnection{


#define API_VERSION   1 // software version of this library
#define DATA_BUFFER   64
#define MAX_LISTENERS 10


protected:
	Stream * com;
	virtual void init();
private:
	// per object data
	uint8_t bufferCount;
	uint8_t buffer[DATA_BUFFER];
	
	static const uint8_t START_BIT = 18;
	static const uint8_t ACK_BIT = 19;
	static const uint8_t STOP_BIT = 27;
	static const uint8_t TOKEN_BIT = 59;

	int numberOfValues;

	CommandListener defaultListener;  			// default listener
	CommandListener listeners[MAX_LISTENERS];   // user listeners
	uint8_t listeners_key[MAX_LISTENERS];       // listeners keys(CommandType).
	int listenersLength;

	// private methods
	void parseCommand(void);
	void notifyListeners(Command);

	int getArrayLength();

public: 
	// public methods
	DeviceConnection();
	DeviceConnection(Stream &serial);
	
	void flush(void);
	virtual bool checkDataAvalible(void);

	void setDefaultListener(CommandListener);
	void addListener(uint8_t,CommandListener);
	void removeListener(uint8_t);
	int bufferLength(){return bufferCount;} // buffer withouth ACK
	int stringLength(){return bufferCount;} // string without flag but '/0' at the end
	void getBuffer(uint8_t[]);
	
	void readString(char[]);
	int readInt();
	long readLong();
	float readFloat();
	double readDouble();
	void readIntValues(int[]);
	void readFloatValues(float[]);
	void readDoubleValues(float[]); // in Arduino double and float are the same
	
	#if defined(ARDUINO) && ARDUINO >= 100
	size_t write(uint8_t);
	#else
	void write(uint8_t);
	#endif
	

	void doStart();
	void doToken();
	void doEnd();

	void send(char);
    void send(const char[]);
    void send(int[], int);
    void send(long values[], int size);
    void send(uint8_t);
    void send(int);
    void send(unsigned int);
    void send(long);
    void send(unsigned long);
    void send(long, int);
    void send(double);
    void sendln(void);
    void send(Command);

    template < class T > void sendCmdArg (T arg){
    	com->write(START_BIT);
    	com->print(arg);
    	com->write(ACK_BIT);
    }

    // Umanaged send data, must be used with doStart/doToken/doEnd
    template < class T > void print (T arg){
    	com->print(arg);
    }

	uint16_t waitTime;
	
	static int api_version() { return API_VERSION;}
};


#endif
