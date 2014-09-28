package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;

/**
 * @autor Ricardo JL Rufino
 * @date 13/09/14.
 */
public interface ServerConnection extends DeviceConnection {

    public void setPort(int port);

    Message notifyAndWait(Request message);
}
