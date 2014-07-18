/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.connection;

import java.util.HashMap;


public class ConnectionProperties extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;
	
	public static String CONNECTION_URL = "connection.url";
	public static String CONNECTION_PORT = "connection.port";
	
	public ConnectionProperties() {
	}
	
	public ConnectionProperties(String url) {
		put(CONNECTION_URL, url);
	}
	
	public ConnectionProperties(int port) {
		put(CONNECTION_PORT, port);
	}

	public ConnectionProperties(String url, int port) {
		put(CONNECTION_URL, url);
		put(CONNECTION_PORT, port);
	}
	
	public String getConnectionURL(){
		return (String) this.get(CONNECTION_URL);
	}
	
	public int getConnectionPort(){
		Object object = this.get(CONNECTION_URL);
		if(object != null){
			return (Integer) object;
		}
		return 0;
	}

}
