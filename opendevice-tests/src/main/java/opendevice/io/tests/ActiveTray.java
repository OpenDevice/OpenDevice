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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActiveTray {

    private SystemTray tray;
    private TrayIcon trayIcon;
    private Icon icon, icon1;
    private Image image, image1;
    private Timer timer;

    public ActiveTray() {
        if (SystemTray.isSupported() == false) {
            System.err.println("No system tray available");
            return;
        }
        tray = SystemTray.getSystemTray();
        PropertyChangeListener propListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                TrayIcon oldTray[] = (TrayIcon[]) evt.getOldValue();
                TrayIcon newTray[] = (TrayIcon[]) evt.getNewValue();
                System.out.println(oldTray.length + " / " + newTray.length);
            }
        };
        tray.addPropertyChangeListener("trayIcons", propListener);
        icon = new BevelArrowIcon(BevelArrowIcon.UP, false, false);
        image = iconToImage(icon);
        icon1 = new BevelArrowIcon(BevelArrowIcon.DOWN, false, false);
        image1 = iconToImage(icon1);
        PopupMenu popup = new PopupMenu();
        MenuItem item = new MenuItem("Hello, World");
        trayIcon = new TrayIcon(image, "Tip Text", popup);
        ActionListener menuActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                trayIcon.displayMessage("Good-bye", "Cruel World",
                        TrayIcon.MessageType.WARNING);
            }
        };
        item.addActionListener(menuActionListener);
        popup.add(item);
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
            }
        };
        trayIcon.addActionListener(actionListener);
        try {
            tray.add(trayIcon);
            start();
        } catch (AWTException ex) {
            Logger.getLogger(ActiveTray.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void start() {
        timer = new javax.swing.Timer(125, updateCol());
        timer.start();
        trayIcon.displayMessage(null, "  Aplication Loaded  ", TrayIcon.MessageType.NONE);
    }

    private Action updateCol() {
        return new AbstractAction("Icon load action") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                Runnable doRun = new Runnable() {

                    @Override
                    public void run() {
                        Image img = trayIcon.getImage();
                        if (img == image) {
                            trayIcon.setImage(image1);
                        } else {
                            trayIcon.setImage(image);
                        }
                    }
                };
                SwingUtilities.invokeLater(doRun);
            }
        };
    }

    public static void main(String args[]) {
        ActiveTray activeTray = new ActiveTray();
    }

    static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

    static class BevelArrowIcon implements Icon {

        public static final int UP = 0;         // direction
        public static final int DOWN = 1;
        private static final int DEFAULT_SIZE = 16;
        private Color edge1;
        private Color edge2;
        private Color fill;
        private int size;
        private int direction;

        public BevelArrowIcon(int direction, boolean isRaisedView,
                boolean isPressedView) {
            if (isRaisedView) {
                if (isPressedView) {
                    init(UIManager.getColor("controlLtHighlight"),
                            UIManager.getColor("controlDkShadow"),
                            UIManager.getColor("controlShadow"),
                            DEFAULT_SIZE, direction);
                } else {
                    init(UIManager.getColor("controlHighlight"),
                            UIManager.getColor("controlShadow"),
                            UIManager.getColor("control"),
                            DEFAULT_SIZE, direction);
                }
            } else {
                if (isPressedView) {
                    init(UIManager.getColor("controlDkShadow"),
                            UIManager.getColor("controlLtHighlight"),
                            UIManager.getColor("controlShadow"),
                            DEFAULT_SIZE, direction);
                } else {
                    init(UIManager.getColor("controlShadow"),
                            UIManager.getColor("controlHighlight"),
                            UIManager.getColor("control"),
                            DEFAULT_SIZE, direction);
                }
            }
        }

        public BevelArrowIcon(Color edge1, Color edge2, Color fill,
                int size, int direction) {
            init(edge1, edge2, fill, size, direction);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            switch (direction) {
                case DOWN:
                    drawDownArrow(g, x, y);
                    break;
                case UP:
                    drawUpArrow(g, x, y);
                    break;
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        private void init(Color edge1, Color edge2, Color fill,
                int size, int direction) {
            edge1 = Color.red;
            edge2 = Color.blue;
            this.edge1 = edge1;
            this.edge2 = edge2;
            this.fill = fill;
            this.size = size;
            this.direction = direction;
        }

        private void drawDownArrow(Graphics g, int xo, int yo) {
            g.setColor(edge1);
            g.drawLine(xo, yo, xo + size - 1, yo);
            g.drawLine(xo, yo + 1, xo + size - 3, yo + 1);
            g.setColor(edge2);
            g.drawLine(xo + size - 2, yo + 1, xo + size - 1, yo + 1);
            int x = xo + 1;
            int y = yo + 2;
            int dx = size - 6;
            while (y + 1 < yo + size) {
                g.setColor(edge1);
                g.drawLine(x, y, x + 1, y);
                g.drawLine(x, y + 1, x + 1, y + 1);
                if (0 < dx) {
                    g.setColor(fill);
                    g.drawLine(x + 2, y, x + 1 + dx, y);
                    g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
                }
                g.setColor(edge2);
                g.drawLine(x + dx + 2, y, x + dx + 3, y);
                g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
                x += 1;
                y += 2;
                dx -= 2;
            }
            g.setColor(edge1);
            g.drawLine(xo + (size / 2), yo + size - 1, xo
                    + (size / 2), yo + size - 1);
        }

        private void drawUpArrow(Graphics g, int xo, int yo) {
            g.setColor(edge1);
            int x = xo + (size / 2);
            g.drawLine(x, yo, x, yo);
            x--;
            int y = yo + 1;
            int dx = 0;
            while (y + 3 < yo + size) {
                g.setColor(edge1);
                g.drawLine(x, y, x + 1, y);
                g.drawLine(x, y + 1, x + 1, y + 1);
                if (0 < dx) {
                    g.setColor(fill);
                    g.drawLine(x + 2, y, x + 1 + dx, y);
                    g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
                }
                g.setColor(edge2);
                g.drawLine(x + dx + 2, y, x + dx + 3, y);
                g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
                x -= 1;
                y += 2;
                dx += 2;
            }
            g.setColor(edge1);
            g.drawLine(xo, yo + size - 3, xo + 1, yo + size - 3);
            g.setColor(edge2);
            g.drawLine(xo + 2, yo + size - 2, xo + size - 1, yo + size - 2);
            g.drawLine(xo, yo + size - 1, xo + size, yo + size - 1);
        }
    }
}