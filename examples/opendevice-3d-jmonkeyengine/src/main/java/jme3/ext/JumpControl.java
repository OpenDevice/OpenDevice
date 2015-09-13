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

package jme3.ext;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class JumpControl extends AbstractControl {

    private float size;
    private float speed;
    private float current = 0;
    private boolean _jumping = true;

    public JumpControl(float size, float speed) {
        this.size = size;
        this.speed = speed;
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (_jumping && current < size) {

            current += speed;

            spatial.move(0, tpf * speed, 0);

        } else {

            _jumping = false;

            current -= speed;

            if (current < 0) {
                spatial.removeControl(this);
            } else {
                spatial.move(0, -(tpf * speed), 0);
            }

        }

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
