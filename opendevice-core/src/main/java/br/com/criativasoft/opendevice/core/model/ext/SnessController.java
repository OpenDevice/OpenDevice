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

package br.com.criativasoft.opendevice.core.model.ext;


import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 26/10/14.
 */
public class SnessController extends Sensor {

    public static interface Button{

        int getButtonIndex();
        int getButtonCode();

    }

    public static enum Pad1 implements Button{

//        B(0, PadKey.KEYCODE_BUTTON_B),
//        Y(1, PadKey.KEYCODE_BUTTON_Y),
//        SELECT(2, PadKey.KEYCODE_BUTTON_SELECT),
//        START(3, PadKey.KEYCODE_BUTTON_START),
//        UP(4, PadKey.KEYCODE_DPAD_UP),
//        DOWN(5, PadKey.KEYCODE_DPAD_DOWN),
//        LEFT(6, PadKey.KEYCODE_DPAD_LEFT),
//        RIGHT(7, PadKey.KEYCODE_DPAD_RIGHT),
//        A(8, PadKey.KEYCODE_BUTTON_A),
//        X(9, PadKey.KEYCODE_BUTTON_X),
//        L(10, PadKey.KEYCODE_BUTTON_L1),
//        R(11, PadKey.KEYCODE_BUTTON_R1),

        B(0, 'B'),
        Y(1, 'Y'),
        SELECT(2, KeyEvent.VK_ENTER),
        START(3, KeyEvent.VK_BACK_SPACE),
        UP(4, KeyEvent.VK_UP),
        DOWN(5, KeyEvent.VK_DOWN),
        LEFT(6, KeyEvent.VK_LEFT),
        RIGHT(7, KeyEvent.VK_RIGHT),
        A(8, 'A'),
        X(9, 'X'),
        L(10, 'L'),
        R(11, 'R'),
        ;

        Pad1(int index, int code) {
            this.index = index;
            this.code = code;
        }

        int index;
        int code;

        @Override
        public int getButtonIndex() {
            return index;
        }

        @Override
        public int getButtonCode() {
            return code;
        }
    }

    public SnessController(int uid) {
        super(uid, DeviceType.CHARACTER);
    }

    public boolean isPressed(Button button){
        return checkByte(button.getButtonIndex());
    }

    public Button getPressedPad1(){

        List<Button> buttons = getPressed(Pad1.values());

        if(buttons.size() > 0) return buttons.get(0);

        return null;

    }

    public List<Button> getPressed(Button[] buttons){
        List<Button> pressed = new LinkedList<Button>();
        for (Button button : buttons) {
            if (isPressed(button)) {
                pressed.add(button);
            }
        }

        return pressed;
    }

    private boolean checkByte(int byteIndex){
        long ulong = getValue() & 0xffffffff;
        return (( ulong >> byteIndex) & 1) != 0;
    }

}
