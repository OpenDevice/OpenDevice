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

package br.com.criativasoft.opendevice.restapi.model;

/**
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
public enum AccountType {
    ACCOUNT_MANAGER,
    USER;

    // For Rest
    public static class ROLES {
        public static final String ACCOUNT_MANAGER = "ACCOUNT_MANAGER";
        public static final String USER = "USER";
    }
}
