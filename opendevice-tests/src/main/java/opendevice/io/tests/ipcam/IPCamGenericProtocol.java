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

package opendevice.io.tests.ipcam;

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.command.ActionCommand;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.command.SetPropertyCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.test.ActionDef;
import br.com.criativasoft.opendevice.core.model.test.CategoryIPCam;
import br.com.criativasoft.opendevice.core.model.test.Property;
import br.com.criativasoft.opendevice.core.model.test.PropertyDef;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class IPCamGenericProtocol implements MessageSerializer {

    private BasicAuth auth;

    private IPCamConnection connection;

    private static Map<String, PropertyDef> propertyMap;

    public IPCamGenericProtocol(BasicAuth auth, IPCamConnection connection) {
        this.auth = auth;
        this.connection = connection;

        // Map to final properties..
        if(propertyMap == null){
            propertyMap = new HashMap<String, PropertyDef>();
            propertyMap.put("alias", CategoryIPCam.alias);
            propertyMap.put("deviceid", CategoryIPCam.cameraID);
            propertyMap.put("alarm_status", CategoryIPCam.alarmStatus);
            propertyMap.put("syswifi_mode", CategoryIPCam.syswifiMode);
            propertyMap.put("mac", CategoryIPCam.mac);
            propertyMap.put("wifimac", CategoryIPCam.wifimac);
            propertyMap.put("authuser", CategoryIPCam.authuser);
            propertyMap.put("externwifi", CategoryIPCam.externWifi);
            propertyMap.put("sdtotal", CategoryIPCam.sdSize);
            propertyMap.put("sdfree", CategoryIPCam.sdFree);
            propertyMap.put("dnsenable", CategoryIPCam.dnsStatus);

            // Image
            propertyMap.put("resolution", CategoryIPCam.resolution);
            propertyMap.put("vbright", CategoryIPCam.brightness);
            propertyMap.put("vcontrast", CategoryIPCam.contrast);
            propertyMap.put("flip", CategoryIPCam.flip);
            propertyMap.put("speed", CategoryIPCam.speed);
            propertyMap.put("ircut", CategoryIPCam.sdFree);
            propertyMap.put("sdfree", CategoryIPCam.sdFree);
            propertyMap.put("enc_bitrate", CategoryIPCam.bitrate);
            propertyMap.put("enc_framerate", CategoryIPCam.framerate);
            propertyMap.put("ircut", CategoryIPCam.infrared);
            propertyMap.put("mode", CategoryIPCam.mode);
        }
    }

    protected String getAuthParams(){
        return "loginuse="+auth.getUser()+"&loginpas="+auth.getPassword();
    }

    @Override
    public Message parse(byte[] data) {
        return null;
    }

    @Override
    public byte[] serialize(Message message) {

        // mapear para as URLs..

        //
        if(message instanceof GetDevicesRequest) {

            getCameraSettings();

        }

        //============================================================
        // Actions
        //============================================================

        if(message instanceof ActionCommand){

            ActionCommand command = (ActionCommand) message;

            String action = ((ActionCommand) message).getAction();

            if(isAction(action, CategoryIPCam.setPosition)){

                // TODO: fazer validação dos parametros (na categoria isso é explicado.)
                Integer position = command.getParam(0, Integer.class);

                return getPositionControlURL(position).getBytes();
            } else if(isAction(action, CategoryIPCam.gpio)){
                Boolean state = command.getParam(0, Boolean.class);
                int value = (state ? 94 : 95);
                connection.doGet("decoder_control.cgi?command="+value + "&onestep=0&"+getAuthParams(), null);
            }

            if(isAction(action, CategoryIPCam.snapshot)){

                String url = "snapshot.cgi";
                String requestURL = connection.getConnectionURI() + "/"+url+"?" + getAuthParams();

                try{
                    InputStream in = new BufferedInputStream( new URL(requestURL).openStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    // byte [] chunk = new byte[1024];
                    for ( int i; (i = in.read()) != -1; ) {
                        out.write(i);
                    }
                    in.close();
                    out.close();
                    return out.toByteArray();
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }

        }

        //============================================================
        // Properties
        //============================================================

        if(message instanceof SetPropertyCommand){

            SetPropertyCommand command = (SetPropertyCommand) message;

            String property = ((SetPropertyCommand) message).getProperty();

            if(isProperty(property, CategoryIPCam.alias)){
                String value = (command).getValue(String.class);
                connection.doGet("set_alias.cgi?alias="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.brightness)){
                Integer value = (command).getValue(Integer.class); // ROLES: 0..255
                return setBrightnessURL(value).getBytes();
            }else if(isProperty(property, CategoryIPCam.contrast)){ // ROLES: 0..255
                Integer value = (command).getValue(Integer.class);
                connection.doGet("camera_control.cgi?param=2&value="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.resolution)){ // ROLES: 0..1
                Integer value = (command).getValue(Integer.class);
                connection.doGet("camera_control.cgi?param=0&value="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.framerate)){ // ROLES: 0..30
                Integer value = (command).getValue(Integer.class);
                connection.doGet("camera_control.cgi?param=6&value="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.flip)){
                Integer value = (command).getValue(Integer.class); // ROLES: 0,1,2,3
                connection.doGet("camera_control.cgi?param=5&value="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.infrared)){
                Integer value = (command).getValue(Integer.class);
                connection.doGet("camera_control.cgi?param=14&value="+value + "&"+getAuthParams(), null);
            }else if(isProperty(property, CategoryIPCam.speed)){
                Integer value = (command).getValue(Integer.class); // ROLES: 0..10
                connection.doGet("camera_control.cgi?param=100&value="+value + "&"+getAuthParams(), null);
            }

        }

        return null;
    }

    public boolean isProperty(String name, PropertyDef def){
        return name.equals(def.getName());
    }

    public boolean isAction(String name, ActionDef def){
        return name.equals(def.getName());
    }

    public String getPositionControlURL(int position){
        return "decoder_control.cgi?command=" + position + "&onestep=0&" + getAuthParams();
    }

    public String setBrightnessURL(int value){
        return "camera_control.cgi?c&param=1&value="+value+"&" + getAuthParams();
    }


    protected void getCameraSettings(){
        connection.doGet("get_status.cgi?"+getAuthParams(), new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {

                if(response.getStatusCode() == 200){ // sucess.

                    String content = response.getResponseBody();

                    getImageSettings(content);


                }else if(response.getStatusCode() == 401){ // Unauthorized

                    connection.disconnect();

                }

                return response;
            }

        });
    }

    protected void getImageSettings(final String cameraSettings){

        connection.doGet("get_camera_params.cgi?" + getAuthParams(), new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {

                if (response.getStatusCode() == 200) { // sucess.

                    String content = response.getResponseBody();

                    IPCamDevice device = new IPCamDevice(11); // FIXME:  id fixo...
                    device.setCategory(BaseDeviceManager.getInstance().getCategory(CategoryIPCam.class));
                    device.setConnection(connection);

                    parseProperties(device, cameraSettings);
                    parseProperties(device, content);

                    String videoURL = connection.getConnectionURI() + "/videostream.cgi?" + getAuthParams();
                    Property property = new Property(CategoryIPCam.videoStreamURL, videoURL);
                    device.addProperty(property);

                    List<Device> devices = new ArrayList<Device>();
                    devices.add(device);
                    GetDevicesResponse getDevicesResponse = new GetDevicesResponse(devices, connection.getConnectionURI());
                    connection.notifyListeners(getDevicesResponse);


                } else if (response.getStatusCode() == 401) { // Unauthorized

                }

                return response;
            }

        });


    }

    protected void parseProperties(IPCamDevice device, String response){
        String[] lines = response.split("\r\n");

        for (String line : lines) {

            // var alias="value";
            String[] vardec = line.split("=");
            String name = vardec[0].substring(4);
            String value = vardec[1];
            value = value.replace(";", "");
            if(value.contains("\"")){ // String
                value = value.replaceAll("\"", "");
            }

            if("alias".equals(name)){
                device.setName(value);
            }

            PropertyDef propertyDef = propertyMap.get(name);
            if(propertyDef != null){
                Property property = new Property(propertyDef, value);
                device.addProperty(property);
            }
        }

    }


    // TODO: remove this

    public static void main(String[] args) throws ConnectionException {
        new CategoryIPCam().loadProperties(); // FIXME: isso tem que ser automatico
        BasicAuth auth = new BasicAuth("admin", "soft2011");
        final IPCamConnection ipCamConnection = new IPCamConnection("http://192.168.3.104:81", auth);
        ipCamConnection.connect();
        new IPCamGenericProtocol(auth, ipCamConnection).serialize(new GetDevicesRequest());

    }

    //    protected void snapshot(){
//
//        String url = "snapshot.cgi";
//        String requestURL = connectionURI + "/"+url+"?" + getAuthParams();
//
//        try{
//            InputStream in = new BufferedInputStream( new URL(requestURL).openStream());
//            OutputStream out = new BufferedOutputStream(new FileOutputStream("/home/ricardo/Pictures/img1.jpg"));
//
//            for ( int i; (i = in.read()) != -1; ) {
//                out.write(i);
//            }
//            in.close();
//            out.close();
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//    }
}


