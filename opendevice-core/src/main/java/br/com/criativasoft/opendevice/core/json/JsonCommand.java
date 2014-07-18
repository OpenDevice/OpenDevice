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

package br.com.criativasoft.opendevice.core.json;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Jackson MixIn Class to help serialize Command
 *
 * @autor Ricardo JL Rufino
 * @date 12/07/14.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="class")
public abstract class JsonCommand {

}
