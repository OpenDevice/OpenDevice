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

import br.com.criativasoft.opendevice.core.metamodel.EnumCode;

public enum CommandStatus implements EnumCode {
	
	CREATED(1),
    DELIVERED(2),
    RECEIVED(3),
    FAIL(4),
    EMPTY_DATABASE(5),
    // Response...
    SUCCESS(200),
    NOT_FOUND(404),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    PERMISSION_DENIED(550),
    INTERNAL_ERROR(500),
    NOT_IMPLEMENTED(501);

    private int code;

    private CommandStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CommandStatus getByCode(int code){
        EnumCode[] values = CommandStatus.values();
        for (EnumCode enumCode : values) {
            if(enumCode.getCode() == code){
                return (CommandStatus) enumCode;
            }
        }

        return null;
    }

}
