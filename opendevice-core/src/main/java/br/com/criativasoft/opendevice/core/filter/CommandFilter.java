/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core.filter;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;

/**
 * Filter used to intercept commands received by the connections.
 * Subclasses may use to limit the amount of data received or to receive just one type of command.
 * Filters should be associated with a  {@link br.com.criativasoft.opendevice.core.DeviceManager}
 *
 * @author Ricardo JL Rufino on 12/10/14.
 */
public interface CommandFilter {

    boolean filter(Command command, DeviceConnection connection);

}
