package br.com.criativasoft.opendevice.connection;

import java.util.List;

/**
 * WebSocket Server Connection
 *
 * @author Ricardo JL Rufino
 * @date 13/09/14.
 */
public interface IWSServerConnection extends ServerConnection {

    void addWebResource(String path);

    void addResource(Class<?> resource);

    List<String> getWebresources();
}
