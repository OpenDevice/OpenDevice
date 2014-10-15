package br.com.criativasoft.opendevice.samples.ui;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 14/09/14.
 */
public class SwitchButton extends JToggleButton implements ItemListener, DeviceListener{

    private static ImageIcon iconON = new javax.swing.ImageIcon(SwitchButton.class.getResource("/icons/power_circle_on.png"));
    private static ImageIcon iconOFF = new javax.swing.ImageIcon(SwitchButton.class.getResource("/icons/power_circle_off.png"));
    private static ImageIcon iconOFF_Hover = new javax.swing.ImageIcon(SwitchButton.class.getResource("/icons/power_circle_off-hover.png"));

    private Device device;

    public SwitchButton(Device device) {
        super(device.getName(), iconOFF, false);
        this.device = device;
        device.addListener(this);

        setPressedIcon(iconON);
        setSelectedIcon(iconON);

        setDisabledIcon(iconOFF);
        setDisabledSelectedIcon(iconON);

        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(false);

        // setForeground(Color.white);
        setFont(new Font("Verdana", Font.BOLD, 14));
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMinimumSize(new Dimension(128,128));


        if(device instanceof Sensor){
            setEnabled(false);
        }

        addItemListener(this);
    }


    @Override
    public void itemStateChanged(ItemEvent event) {

        int state = event.getStateChange();

        if(state == ItemEvent.SELECTED){
            device.setValue(1);
        }else{
            device.setValue(0);
        }

    }

    @Override
    public void onDeviceChanged(Device deviceChanged) {

        boolean selected = device.getValue() == 1;

        if(selected != this.isSelected()){
            System.out.println("BUTTON CHNAGE !!!");

            if(deviceChanged.getValue() == 0) this.setSelected(false);
            if(deviceChanged.getValue() == 1) this.setSelected(true);
        }

    }
}
