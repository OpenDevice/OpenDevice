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

package br.com.criativasoft.opendevice.core.command;

	
public enum ResponseCommandStatus {
	
	SUCCESS(200), 
	NOT_FOUND(404),
	BAD_REQUEST(400),
	UNAUTHORIZED(401), 
	PERMISSION_DENIED(550), 
	FORBIDDEN(403), 
	INTERNAL_ERROR(500), 
	NOT_IMPLEMENTED(501), 
	
	;
	
	private int code;
		

	private ResponseCommandStatus(int code) {
		this.code = (byte) code;
	}

	public int getCode() {
		return code;
	}

	public static ResponseCommandStatus getByCode(int code){
		ResponseCommandStatus[] values = ResponseCommandStatus.values();
		for (ResponseCommandStatus status : values) {
			if(status.getCode() == code){
				return status;
			}
		}
		
		return null;
	}
	
}
