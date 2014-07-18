#ifndef COMMAND_H_
#define COMMAND_H_

#include <Arduino.h>

namespace CommandType {
	enum CommandType {
		ON_OFF = 1,     // Indica que os valores são 0 ou 1 (HIGH ou LOW)
		ANALOG = 2, // Indica que os valores para esse comando pode ser de 0..1023
		ANALOG_REPORT = 3,   // ndica que os valores para esse comando pode ser de 0..1023
		GPIO_DIGITAL = 4,   //  Controle de baixo nivel (digitalWrite)
		GPIO_ANALOG = 5,   //  Controle de baixo nivel (analogWrite)
		DEVICE_COMMAND_RESPONSE = 10, // Response to commands like: ON_OFF, POWER_LEVEL, INFRA RED
		PING_REQUEST = 20, // Verificar se esta ativo
		PING_RESPONSE = 21, // Resposta para o Ping
		MEMORY_REPORT = 22, // Relatorio de quantidade de memória (repora o atual e o máximo).
		CPU_TEMPERATURE_REPORT = 23,
		CPU_USAGE_REPORT = 24,
		GET_DEVICES = 30,
		GET_DEVICES_RESPONSE = 31 // Enviado pelo modulo quando um comando for recebido.

	};
}

namespace ResponseStatus {
	enum ResponseStatus {
		SUCCESS = 200,
		NOT_FOUND = 404,
		BAD_REQUEST = 400,
		UNAUTHORIZED = 401,
		PERMISSION_DENIED = 550,
		FORBIDDEN = 403,
		INTERNAL_ERROR = 500,
		NOT_IMPLEMENTED = 501
	};
}

/*namespace SersorType {
	enum SersorType {
		SWITCH = 1, TOOGLE = 2, INFRA_RED = 3,
	};
}*/

struct Command {
	uint8_t type; // values of CommandType
	uint8_t id;
	uint8_t deviceID;
	uint16_t value;
	char *data; // raw data received (if not default command's)
	size_t length;
};

#endif /* HEADER */

