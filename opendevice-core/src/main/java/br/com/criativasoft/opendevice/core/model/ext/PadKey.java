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

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 26/10/14.
 */
public interface PadKey {

    public static final int KEYCODE_DPAD_UP         = 19;
    /** Key code constant: Directional Pad Down key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_DOWN       = 20;
    /** Key code constant: Directional Pad Left key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_LEFT       = 21;
    /** Key code constant: Directional Pad Right key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_RIGHT      = 22;


    /** Key code constant: A Button key.
     * On a game controller, the A button should be either the button labeled A
     * or the first button on the bottom row of controller buttons. */
    public static final int KEYCODE_BUTTON_A        = 96;
    /** Key code constant: B Button key.
     * On a game controller, the B button should be either the button labeled B
     * or the second button on the bottom row of controller buttons. */
    public static final int KEYCODE_BUTTON_B        = 97;
    /** Key code constant: C Button key.
     * On a game controller, the C button should be either the button labeled C
     * or the third button on the bottom row of controller buttons. */
    public static final int KEYCODE_BUTTON_C        = 98;
    /** Key code constant: X Button key.
     * On a game controller, the X button should be either the button labeled X
     * or the first button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_X        = 99;
    /** Key code constant: Y Button key.
     * On a game controller, the Y button should be either the button labeled Y
     * or the second button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_Y        = 100;
    /** Key code constant: Z Button key.
     * On a game controller, the Z button should be either the button labeled Z
     * or the third button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_Z        = 101;
    /** Key code constant: L1 Button key.
     * On a game controller, the L1 button should be either the button labeled L1 (or L)
     * or the top left trigger button. */
    public static final int KEYCODE_BUTTON_L1       = 102;
    /** Key code constant: R1 Button key.
     * On a game controller, the R1 button should be either the button labeled R1 (or R)
     * or the top right trigger button. */
    public static final int KEYCODE_BUTTON_R1       = 103;
    /** Key code constant: L2 Button key.
     * On a game controller, the L2 button should be either the button labeled L2
     * or the bottom left trigger button. */
    public static final int KEYCODE_BUTTON_L2       = 104;
    /** Key code constant: R2 Button key.
     * On a game controller, the R2 button should be either the button labeled R2
     * or the bottom right trigger button. */
    public static final int KEYCODE_BUTTON_R2       = 105;
    /** Key code constant: Left Thumb Button key.
     * On a game controller, the left thumb button indicates that the left (or only)
     * joystick is pressed. */
    public static final int KEYCODE_BUTTON_THUMBL   = 106;
    /** Key code constant: Right Thumb Button key.
     * On a game controller, the right thumb button indicates that the right
     * joystick is pressed. */
    public static final int KEYCODE_BUTTON_THUMBR   = 107;
    /** Key code constant: Start Button key.
     * On a game controller, the button labeled Start. */
    public static final int KEYCODE_BUTTON_START    = 108;
    /** Key code constant: Select Button key.
     * On a game controller, the button labeled Select. */
    public static final int KEYCODE_BUTTON_SELECT   = 109;

}
