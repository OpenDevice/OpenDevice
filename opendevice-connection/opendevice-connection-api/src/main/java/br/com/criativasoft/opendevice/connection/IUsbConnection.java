package br.com.criativasoft.opendevice.connection;

/**
 * SPI Interface Marker for Usb.
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 */
public interface IUsbConnection extends StreamConnection {

    /**
     * Sets the time used to wait for device initialization, some arduinos reset when the initial connection is made.<br/>
     * Use this function to disable or increase the waiting time.<br/>
     * The default value must be set in the subclass
     * @param delay
     */
    void setDeviceBootTime(int delay);
}
