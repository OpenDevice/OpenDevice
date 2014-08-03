package br.com.criativasoft.opendevice.nanohttp.server;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.nanohttp.server.NanoHTTPD.Response.Status;

import java.util.*;

// TODO Documentar.
// TODO: mover os recursos de Handlers para o NanoHTTPD
public class ControlServer extends NanoHTTPD {
	
	private Set<ConnectionListener> listeners = new HashSet<ConnectionListener>(); 
	
	private Map<String, RequestHandler> handlers = new LinkedHashMap<String, RequestHandler>();
	
	
	public ControlServer(int port) {
		super(port);
	}
	
	@Override
	public Response serve(String uri, Method method,Map<String, String> header, Map<String, String> parms,Map<String, String> files) {
		StringBuffer msg = new StringBuffer(200);
		
		RequestHandler handler = findHandler(uri);
		if(handler != null){
			return handler.processRequest(uri, parms, this);
		}
	
		return new NanoHTTPD.Response( Status.INTERNAL_ERROR, MIME_HTML, "Command NOT FOUND" );
	}



	
	/**
	 * Notify All Listeners about received command.
	 */
	public void notifyListeners(Command command, long deviceID){
		for (ConnectionListener listener : listeners) {
			listener.onMessageReceived(command, null);
		}
	}
	

	// =======================================================
	// Set's / Get's
	// =======================================================

	/**
	 * @see #findHandler(String) 
	 */
	public void addRequestHandler(String uriPattern, RequestHandler requestHandler){
		handlers.put(uriPattern, requestHandler);
	}
	
	protected RequestHandler findHandler(String uri){
		Set<String> keySet = handlers.keySet();
		
		for (String key : keySet) {
			
			if(uri.startsWith(key) || "*".equals(key)){ // TODO; Usar expressão regular.
				return handlers.get(key);
			}
			
		}
		
		return null;
	}

	public boolean addListener(ConnectionListener e) {
		return listeners.add(e);
	}

	public boolean addAll(Collection<? extends ConnectionListener> c) {
		return listeners.addAll(c);
	}


	public Set<ConnectionListener> getListeners() {
		return listeners;
	}

	
	// -- TEST -- //
	
	public static void main(String[] args) {
		ControlServer server = new ControlServer(8080);
		System.out.println( "Listening on port 8080. Hit Enter to stop.\n" );
		try { System.in.read(); } catch( Throwable t ) {};		
	}

}
