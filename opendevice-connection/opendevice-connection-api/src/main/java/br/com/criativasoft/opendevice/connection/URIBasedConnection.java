package br.com.criativasoft.opendevice.connection;

/**
 * Interface that defines connections to support a connection address (usb, Bluettoth, tcp, etc. ..)
 * @autor Ricardo JL Rufino
 * @date 06/09/14.
 */
public interface URIBasedConnection {

    public void setConnectionURI(String uri);

    public String getConnectionURI();

}
