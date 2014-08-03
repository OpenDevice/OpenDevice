package br.com.criativasoft.opendevice.nanohttp.server;

import java.util.Map;

import br.com.criativasoft.opendevice.nanohttp.server.NanoHTTPD.Response;

public interface RequestHandler {
	
	public Response processRequest(String uri, Map<String, String> parms, ControlServer server);
	
}
