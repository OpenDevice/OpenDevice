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

package opendevice.io.tests;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
  static Image image = Toolkit.getDefaultToolkit().getImage("/media/ricardo/Dados/Programacao/arduino-1.6.5-r5/lib/arduino.png");

  static TrayIcon trayIcon;

  public static void main(String[] a) throws Exception {

      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    if (SystemTray.isSupported()) {
      SystemTray tray = SystemTray.getSystemTray();

      PopupMenu popup = new PopupMenu();
      popup.add("Opt 1");
      popup.add("Opt 2");

      trayIcon = new TrayIcon(image, "Tester2", popup);

      trayIcon.setImageAutoSize(false);
      trayIcon.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("In here");
          trayIcon.displayMessage("Tester!", "Some action performed", TrayIcon.MessageType.INFO);
        }
      });

      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        System.err.println("TrayIcon could not be added.");
      }
    }
  }
}