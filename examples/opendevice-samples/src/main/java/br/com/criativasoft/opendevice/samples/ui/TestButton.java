/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.samples.ui;

import javax.swing.*;
import java.awt.*;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 29/08/14.
 */
public class TestButton extends FormController {

    @Override
    protected void initComponents() {
        super.initComponents();

        JButton buttonBrowse = new JButton();
        buttonBrowse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/power_circle_on.png")));
        buttonBrowse.setPreferredSize(new Dimension(128,128));
        buttonBrowse.setMinimumSize(new Dimension(128,128));
        buttonBrowse.setBorderPainted(false);
        buttonBrowse.setFocusPainted(false);
        buttonBrowse.setContentAreaFilled(false);

        addButton(buttonBrowse);
    }

    public static void main(String[] args) {
        TestButton formAction = new TestButton();
        formAction.setVisible(true);
    }

}
