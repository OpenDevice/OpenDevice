/*
 * ConnectionENC28J60.h
 *
 *  Created on: 22/06/2014
 *  Author: Ricardo JL Rufino
 */

#ifndef SOCKETCONNECTIONENC28J60_H_
#define SOCKETCONNECTIONENC28J60_H_

// NOTA: DESABILITADO UDP: ./UIPEthernet/utility/uipethernet-conf.h:9:#define UIP_CONF_UDP

#include "DeviceConnection.h"
#include <UIPEthernet.h>
#include <Stream.h>

class ConnectionENC28J60 : public DeviceConnection {
private:
	uint16_t _port;
	virtual void init();
	bool _connected;
	void connect();
public:
	ConnectionENC28J60(uint16_t);
	virtual ~ConnectionENC28J60();
	virtual bool checkDataAvalible(void);

	EthernetServer *server; // TODO : remover de publico...
	EthernetClient *client;
};

#endif /* SOCKETCONNECTIONENC28J60_H_ */
