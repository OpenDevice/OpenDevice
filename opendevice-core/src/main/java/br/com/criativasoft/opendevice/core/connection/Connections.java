package br.com.criativasoft.opendevice.core.connection;

/**
 * Factory for Connections
 * @autor Ricardo JL Rufino
 * @date 06/09/14.
 */
public class Connections {

    public static final OutputConnections out = new OutputConnections();

    /** Don't let anyone instantiate this class */
    private Connections() {
    }

}
