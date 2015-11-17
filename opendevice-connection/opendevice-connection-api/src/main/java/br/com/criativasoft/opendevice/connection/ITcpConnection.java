package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.discovery.DiscoveryService;

/**
 * SPI Interface Marker for TCP.
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 */
public interface ITcpConnection extends StreamConnection {

    void setDiscoveryService(DiscoveryService discoveryService);

}
