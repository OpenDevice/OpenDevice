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

package br.com.criativasoft.opendevice.core;

/**
 * OpenDevice Extension Point Interface.
 * The extension system (plugins) of OpenDevice is using SPI
 * @author Ricardo JL Rufino
 * @date 30/08/15.
 */
public interface OpenDeviceExtension {

    String getName();

    void init();

    void destroy();

}
