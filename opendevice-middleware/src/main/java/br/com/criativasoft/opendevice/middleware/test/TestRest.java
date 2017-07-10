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

package br.com.criativasoft.opendevice.middleware.test;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.FirmwareUpdateCommand;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.middleware.tools.SimulationService;
import br.com.criativasoft.opendevice.restapi.DeviceRest;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 13/10/16
 */
@Path("/tests")
public class TestRest {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceManager manager;

    @Inject
    private DeviceDao dao;

    @Inject
    private SimulationService simulationService;

    @PersistenceContext
    private EntityManager em;


    @GET
    @Path("/simulation/start/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startSimulation(@PathParam("uid") int uid, @QueryParam("interval") int interval) {
        Device device = manager.findDeviceByUID(uid);
        TenantContext context = TenantProvider.getCurerntContext();

        boolean started = simulationService.start(context, device, interval);

        if(started){
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

    };

    @GET
    @Path("/simulation/stop/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopSimulation(@PathParam("uid") int uid) {
        Device device = manager.findDeviceByUID(uid);
        simulationService.stop(device);
        return Response.ok().build();
    }

    @GET
    @Path("/simulation/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceVO> listSimulation() {

        TenantContext context = TenantProvider.getCurerntContext();

        List<DeviceVO> devices = new LinkedList<DeviceVO>();

        Collection<Device> deviceList = simulationService.list(context);

        if(deviceList != null){
            for (Device device : deviceList) {
                devices.add(new DeviceVO(device));
            }
        }

        return devices;
    }

    @GET
    @Path("/teste1")
    @Produces(MediaType.APPLICATION_JSON)
    public String teste1() {

        Set<Device> devices = new HashSet();

//        TypedQuery<PhysicalDevice> query = em.createQuery("select x from PhysicalDevice x JOIN x.board where  x.applicationID = :TENANT", PhysicalDevice.class);
//
//        query.setParameter("TENANT", TenantProvider.getCurrentID());
//
//        devices.addAll(query.getResultList());
//
//
//        TypedQuery<Board> boardQR = em.createQuery("select  x from Board x JOIN FETCH x.devices WHERE x.applicationID = :TENANT", Board.class);
//
//        boardQR.setParameter("TENANT", TenantProvider.getCurrentID());
//
//        devices.addAll(boardQR.getResultList());


        TypedQuery<Device> anotherQR = em.createQuery("select x from Device x where  x.applicationID = ?1", Device.class);

        anotherQR.setParameter(1, TenantProvider.getCurrentID());

        devices.addAll(anotherQR.getResultList());


        System.out.println("Devices : " + devices.size());

        for (Device device : devices) {

            if(device instanceof PhysicalDevice){
                System.out.println("Device: " + device);
                System.out.println(":: is Phy.ParantID:" + ((PhysicalDevice) device).getBoard());
            }

            if(device instanceof Board){
                System.out.println("Board : " + device.getName() + ", devices: " + ((Board) device).getDevices());

//                TypedQuery<PhysicalDevice> anotherQR = em.createQuery("from PhysicalDevice where board.uid = " + device.getUid(), PhysicalDevice.class);
//                List<PhysicalDevice> resultList = anotherQR.getResultList();
//                System.out.println(" >> " + resultList);
            }
        }

        return "OK";
    }


//    TypedQuery<Device> query = em.createQuery("select x from Device x where x.applicationID = :TENANT", Device.class);
//
//        query.setParameter("TENANT", TenantProvider.getCurrentID());
//
//    List<Device> list = query.getResultList();
//
//        System.out.println("Devices : " + list.size());
//
//        for (Device device : list) {
//
//        if(device instanceof PhysicalDevice){
//            System.out.println("Phy:" + ((PhysicalDevice) device).getBoard());
//        }
//
//        if(device instanceof Board){
//            System.out.println("Board : " + device.getName() + ", devices: " + ((Board) device).getDevices());
//
//            TypedQuery<PhysicalDevice> query2 = em.createQuery("from PhysicalDevice where board.uid = " + device.getUid(), PhysicalDevice.class);
//
//            List<PhysicalDevice> resultList = query2.getResultList();
//            System.out.println(" >> " + resultList);
//
//
//        }
//    }


    @GET
    @Path("/updateFiemware")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFiemware() {

        // Files path: {root}/data/files/uploads/firmwares
        try {

            ((BaseDeviceManager)manager).send(new FirmwareUpdateCommand("firmware.bin"), true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/firmwares/download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public ByteArrayOutputStream downloadFiemware(@Context AtmosphereResource request, @PathParam("id") int id) {

        request.getRequest().header("Transfer-Encoding", "xxxxxxxx");
        System.out.println("Downloading....");

        File file = new File("/media/ricardo/Dados/TEMP/ArduinoTestes/OpenDeviceEEPROM/.pioenvs/esp/firmware.bin");

        try{

            ByteArrayOutputStream ous = null;
            InputStream ios = null;
            try {
                byte[] buffer = new byte[4096];
                ous = new ByteArrayOutputStream();
                ios = new FileInputStream(file);
                int read = 0;
                while ((read = ios.read(buffer)) != -1) {
                    ous.write(buffer, 0, read);
                }
            }finally {
                try {
                    if (ous != null)
                        ous.close();
                } catch (IOException e) {
                }

                try {
                    if (ios != null)
                        ios.close();
                } catch (IOException e) {
                }
            }

            return ous;

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

}
