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

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collection;

/**
 * TODO: PENDING DOC
 *
 * @author ricardo
 * @date 30/06/14.
 */
public class SimpleChart  extends ApplicationFrame implements ConnectionListener {

    private static final String TITLE = "Analog Test";
    private static final String START = "Start";
    private static final String STOP = "Stop";

    private static final float MINMAX = 1024;

    private static final int COUNT = 100;
    private static final int SERIES = 3;

    private StreamConnection connection;
    private DynamicTimeSeriesCollection dataset;


    public SimpleChart(final String title, final StreamConnection connection) {
        super(title);

        dataset = new DynamicTimeSeriesCollection(SERIES, COUNT, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 1, 1, Calendar.getInstance().get(Calendar.YEAR)));

        // Init.
//        for (int i = 0; i < SERIES; i++){
//            dataset.addSeries(new float[]{0}, i, "Serie " + (i+1));
//        }
        dataset.addSeries(new float[]{0,0,0}, 0, "Raw");
        dataset.addSeries(new float[]{0,0,0}, 1, "Software");
        dataset.addSeries(new float[]{0,0,0}, 2, "Hardware");

        this.connection = connection;

        // connection.setStreamReader(new FixedStreamReader(1));

        JFreeChart chart = createChart(dataset);

        final JButton run = new JButton(START);
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String cmd = e.getActionCommand();
                JButton source = (JButton) e.getSource();

                try {

                    if(cmd.equals(START)){
                        connection.addListener(SimpleChart.this);
                        if(!connection.isConnected())
                            connection.connect();

                        source.setText(STOP);
                        source.setActionCommand(STOP);
                    }else{
                        connection.removeListener(SimpleChart.this);
                        source.setText(START);
                        source.setActionCommand(START);
                    }

                } catch (ConnectionException e1) {
                    e1.printStackTrace();
                }

            }
        });

        final JComboBox combo = new JComboBox();

        if(connection instanceof UsbConnection){

            Collection<String> portNames = UsbConnection.listAvailablePortNames();
            for (String name : portNames){
                combo.addItem(name);
            }

        }

        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Object selectedItem = combo.getSelectedItem();

            }
        });

        this.add(new ChartPanel(chart), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(run);
        btnPanel.add(combo);
        this.add(btnPanel, BorderLayout.SOUTH);

    }



    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart( TITLE, "mm:ss", "Value", dataset, true, true, false);
        final XYPlot plot = result.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        ValueAxis domain = plot.getDomainAxis();
        domain.setAutoRange(true);
        ValueAxis range = plot.getRangeAxis();
        range.setRange(0, MINMAX);
        range.setAutoRangeMinimumSize(20);

        XYLineAndShapeRenderer renderer= (XYLineAndShapeRenderer) plot.getRenderer();

        for (int i = 0; i < SERIES; i++){
           renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }

        return result;
    }


    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        String msg = message.toString();
        String[] values = msg.split(",");

        if(values != null && values.length >= SERIES){

            float[] newData = new float[SERIES];

            newData[0] = Float.parseFloat(values[0]);
            if(SERIES >= 1) newData[1] = Float.parseFloat(values[1]);
            if(SERIES >= 2) newData[2] = Float.parseFloat(values[2]);

            dataset.advanceTime();
            dataset.appendData(newData);
        }

    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                StreamConnection usb = StreamConnectionFactory.createUsb("/dev/ttyACM0");
                SimpleChart demo = new SimpleChart(TITLE, usb);
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
            }
        });
    }


}