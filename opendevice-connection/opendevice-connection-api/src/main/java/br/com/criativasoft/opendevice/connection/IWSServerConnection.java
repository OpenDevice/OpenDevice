package br.com.criativasoft.opendevice.connection;

/**
 * WebSocket Server Connection
 *
 * @autor Ricardo JL Rufino
 * @date 13/09/14.
 */
public interface IWSServerConnection extends ServerConnection {

    void addWebResource(String path);
}
