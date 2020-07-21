/*******************************************************************************
 * This file is part of RITDevX Controller project.
 * Copyright (c) 2020 Ricardo JL Rufino.
 *
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3 with Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. See the included LICENSE file for details.
 *******************************************************************************/
package no_unit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Painter;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;

/**
 * Logview with auto scrolll.
 * @author Ricardo JL Rufino - (ricardo.jl.rufino@gmail.com)
 * @date 14 de jun de 2020
 */
public class CommandLogViewer extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final DefaultStyledDocument document;
    private Timer timer;
    private boolean newLinePrinted;
    private SimpleAttributeSet stdOutStyle;
    private SimpleAttributeSet stdInStyle;
    private CommandStreamSerializer serializer;
    private DebugCommandStreamReader reader;

    private AtomicInteger sequence = new AtomicInteger();

    public CommandLogViewer() {

        this.setLayout(new BorderLayout());
        this.serializer = new DebugCommandSerializer();
        this.reader = new DebugCommandStreamReader();

        fixLaf();

        document = new DefaultStyledDocument();
        JTextPane consoleTextPane = new JTextPane(document);
        consoleTextPane.setEditable(false);
        consoleTextPane.setBackground(Color.BLACK);

        scroll = new JScrollPane(consoleTextPane);
        scroll.getVerticalScrollBar().setUnitIncrement(7);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Color.BLACK);

        final Font consoleFont = new Font("Courier New", Font.PLAIN, 13);
        stdOutStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stdOutStyle, Color.RED);
        StyleConstants.setBackground(stdOutStyle, Color.BLACK);
        StyleConstants.setFontSize(stdOutStyle, consoleFont.getSize());
        StyleConstants.setFontFamily(stdOutStyle, consoleFont.getFamily());
        StyleConstants.setBold(stdOutStyle, consoleFont.isBold());
        consoleTextPane.setParagraphAttributes(stdOutStyle, true);

        stdInStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stdInStyle, Color.GREEN);
        StyleConstants.setBackground(stdInStyle, Color.BLACK);
        StyleConstants.setFontSize(stdInStyle, consoleFont.getSize());
        StyleConstants.setFontFamily(stdInStyle, consoleFont.getFamily());
        StyleConstants.setBold(stdInStyle, consoleFont.isBold());

        DefaultCaret caret = (DefaultCaret) consoleTextPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        consoleTextPane.setFocusTraversalKeysEnabled(false);

        this.timer = new Timer(100, ( e ) -> {
            if (isShowing() && newLinePrinted) {
                newLinePrinted = false;
                scrollDown();
            }
        });
        timer.setRepeats(false);

        this.add(scroll, BorderLayout.CENTER);
    }

    /** FIX for Ninbus LAF (https://bugs.openjdk.java.net/browse/JDK-8058704) */
    private static void fixLaf() {
        UIManager.put("TextPane[Enabled].backgroundPainter", new Painter<JComponent>() {
            @Override
            public void paint( Graphics2D g , JComponent comp , int width , int height ) {
                g.setColor(comp.getBackground());
                g.fillRect(0, 0, width, height);
            }
        });
    }

    public void append( String message , SimpleAttributeSet style ) {

        try {
            document.insertString(document.getLength(), sequence.getAndIncrement() + "]: " + message + "\n", style);
            newLinePrinted = true;

            if (!timer.isRunning()) {
                timer.restart();
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    public void clear() {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void scrollDown() {
        scroll.getHorizontalScrollBar().setValue(0);
        scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
    }

    public CommandStreamSerializer getSerializer() {
        return serializer;
    }

    public DebugCommandStreamReader getReader() {
        return reader;
    }

    public class DebugCommandStreamReader extends CommandStreamReader {

        @Override
        public void processPacketRead( byte[] read , int length ) {
            append(new String(read), stdInStyle);
            super.processPacketRead(read, length);
        }

    }

    public class DebugCommandSerializer extends CommandStreamSerializer {

        @Override
        public Message parse( byte[] pkg ) {

            return super.parse(pkg);
        }

        @Override
        public byte[] serialize( Message message ) {
            byte[] serialize = super.serialize(message);
            append(new String(serialize), stdOutStyle);
            return serialize;
        }
    }

    /**
     * Display logs in a new frame
     * 
     * @param logViewer
     * @return {@link JFrame} with {@link CommandLogViewer} in contentPane
     */
    public static JFrame display( CommandLogViewer logViewer ) {
        JFrame frame = new JFrame();
        frame.setTitle("Logs");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(logViewer);
        logViewer.setPreferredSize(new Dimension(500, 500));
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public void monitor( AbstractStreamConnection usb ) {
        usb.setSerializer(getSerializer());
        usb.setStreamReader(getReader());
        display(this);
    }

}
