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

import br.com.criativasoft.opendevice.core.metamodel.EnumCode;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.test.CategoryIPCam;
import br.com.criativasoft.opendevice.core.model.test.GenericDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/01/16
 */
public class IPCamDevice extends GenericDevice {

    // TODO: falta definição do tipo do device
    // cada camera pode ter um protocolo diferente. (vai ser de 1...1 com connection ?)

    // FIXME: protocol dependent
    public enum MoveAction implements EnumCode {

        UP(0),
        DOWN(2),
        LEFT(4),
        RIGHT(6),
        LEFT_UP(90),
        RIGHT_UP(91),
        LEFT_DOWN(92),
        RIGHT_DOWN(93),
        STOP(1),
        CENTER(25);

        MoveAction(int code){
            this.code = code;
        }

        private final int code;

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getDescription() {
            return name();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(IPCamDevice.class);


    public IPCamDevice(int uid) { // FIXME worng
        super(uid, null, DeviceType.DIGITAL, DeviceCategory.GENERIC);
    }


    public void screenshot(){

        execute(CategoryIPCam.snapshot, null);
    }


    public void move(MoveAction position){
        execute(CategoryIPCam.setPosition, position.getCode());
    }

}
