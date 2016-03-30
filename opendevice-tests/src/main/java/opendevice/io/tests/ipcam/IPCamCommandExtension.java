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

package opendevice.io.tests.ipcam;


import br.com.criativasoft.opendevice.core.command.CommandRegistry;

/**
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class IPCamCommandExtension implements ICommandExtension {

    @Override
    public void install(CommandRegistry registry) {

        // talvez seja interessante mapear um deviceType, para aplicações
        // terem como buscar quais os tipos de comando que são aceitos (problema, o mesmo pode funcionar para outros tipos ?)

//        registry.addCommand(12, SetBrightnessCmd.class); // TODO, falta o TYPE do Value, falta um nome para facilitar o entendimento
    }
}
