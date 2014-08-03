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
	
	CREATED(1), DELIVERED(2), RECEIVED(3), FAIL(4);

    private int code;

    /**
     * @param code - Device type code. MAX 127.
     */
    private CommandStatus(int code) {
        this.code = (byte) code;
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
