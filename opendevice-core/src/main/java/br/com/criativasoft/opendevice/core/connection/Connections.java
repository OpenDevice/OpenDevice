package br.com.criativasoft.opendevice.core.connection;

/**
 * Factory for Connections
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 */
public class Connections {

    public static final OutputConnections out = new OutputConnections();

    public static final InputContections in = new InputContections();

    /** Don't let anyone instantiate this class */
    private Connections() {
    }

}
