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

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.Pause;

import java.io.IOException;

public class MediaCenterDevice implements Runnable {

    public static void main(String[] args) throws IOException {
        // Start a user thread that runs the UPnP stack
        Thread clientThread = new Thread(new MediaCenterDevice());
        clientThread.setDaemon(false);
        clientThread.start();

    }

    private static Service avTransportService;
    private static UpnpService upnpService;


    public void run() {
        try {

            upnpService = new UpnpServiceImpl();

            // Add a listener for device registration events
            upnpService.getRegistry().addListener(createRegistryListener);

            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

    DefaultRegistryListener createRegistryListener = new DefaultRegistryListener() {

            ServiceId serviceId = new UDAServiceId("AVTransport");

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                if ((avTransportService = device.findService(serviceId)) != null) {
                    System.out.println(device.getDetails().getFriendlyName() + " - Service discovered: " + avTransportService);

                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Service switchPower;
                if (! device.getDetails().getFriendlyName().contains("TV")) return;
                if ((switchPower = device.findService(serviceId)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                }
            }

        };


    public void executeAction() {

//        ActionCallback setAVTransportURIAction =
//                new SetAVTransportURI(avTransportService, "http://192.168.3.104:81/videostream.cgi?loginuse=criativa&loginpas=criativa22269", "") {
//                    @Override
//                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//                        // Something was wrong
//                    }
//
//                    @Override
//                    public void success(ActionInvocation invocation) {
//                        ActionCallback playAction =
//                                new Play(avTransportService) {
//                                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//                                        // Something was wrong
//                                        System.out.println("playAction ok ");
//                                    }
//                                };
//                        upnpService.getControlPoint().execute(playAction);
//                    }
//                };
//
//        upnpService.getControlPoint().execute(setAVTransportURIAction);

                ActionCallback playAction =
                new Pause(avTransportService) {
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        // Something was wrong
                        System.out.println("playAction ok ");
                    }
                };
                        upnpService.getControlPoint().execute(playAction);

    }


}