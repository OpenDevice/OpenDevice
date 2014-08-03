package br.com.criativasoft.opendevice.nanohttp.server;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Esta conexão cria um servidor web interno para recebimento de comandos.
 * TODO: Descrever o protocolo de comandos...
 * @author Ricardo JL Rufino
 */
public class WebServerConnection extends AbstractConnection implements ConnectionListener {
	
	private static Logger log = LoggerFactory.getLogger(WebServerConnection.class);
	
	public static final int DEFAULT_PORT = 8080;
	
	private int port = DEFAULT_PORT;
	private ControlServer server;
	private String cacheDir;
	
	public WebServerConnection(int port, final File cacheDir) {
		super();
		this.port = port;
		this.cacheDir = (cacheDir != null ? cacheDir.getAbsolutePath() : null);
		initServer();
		
	}
	
	protected void initServer(){
		server = new ControlServer(port);
		server.setTempFileManagerFactory(new NanoHTTPD.TempFileManagerFactory() {
			public NanoHTTPD.TempFileManager create() {
				return new NanoHTTPD.DefaultTempFileManager(cacheDir);
			}
		});		
	}
	
	public void updatePort(int port) throws IOException{
		if(server != null){
			log.debug("Updating WebServer port. (" + this.port + "->" + port + ")");
			this.port = port;
			
			boolean connected = isConnected();
			if(connected) this.server.stop();
			initServer();
			if(connected) connect();
			log.debug("Port updated , connected = " + isConnected());
			
		}
	}

	public void connect() throws ConnectionException {
		log.debug("Connecting ... isConnected = " +isConnected());
		setStatus(ConnectionStatus.CONNECTING);
		
		if(!isConnected()){
			server.addListener(this); // Handle in commandReceived
			try {
				server.start();
				setStatus(ConnectionStatus.CONNECTED);
			} catch (IOException e) {
				throw new ConnectionException(e.getMessage(), e);
			}
		}
	}

	public void disconnect() {
		log.debug("Disconnecting ... isConnected = " +isConnected());
		setStatus(ConnectionStatus.DISCONNECTING);
		
		if(isConnected()){
			server.stop();
			setStatus(ConnectionStatus.DISCONNECTED);
		}
	}

	public boolean isConnected() {
		return server != null && super.isConnected();
	}



	
	public ControlServer getServer() {
		return server;
	}
	
	@Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
		
	}

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

    }

    @Override
	public void send(Message message) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
