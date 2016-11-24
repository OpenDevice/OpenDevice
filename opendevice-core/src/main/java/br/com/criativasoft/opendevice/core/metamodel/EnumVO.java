/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.metamodel;

/**
 * @date 02/11/16
 */
public class EnumVO {

    private int code;
    private String description;

    public EnumVO(EnumCode enumCode) {
        this.code = enumCode.getCode();
        this.description = enumCode.getDescription();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
