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

package br.com.criativasoft.opendevice.middleware.resources;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.command.FirmwareUpdateCommand;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.engine.js.utils.IOUtils;
import br.com.criativasoft.opendevice.middleware.MainTenantProvider;
import br.com.criativasoft.opendevice.middleware.model.Firmware;
import br.com.criativasoft.opendevice.middleware.persistence.dao.FirmwareDao;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Ricardo JL Rufino
 *         Date: 08/07/18
 */
@Path("/middleware/firmwares")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class FirmwareRest {

    private static final Logger log = LoggerFactory.getLogger(FirmwareRest.class);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private FirmwareDao dao;

    @GET
    @Path("/{id}")
    public Firmware get(@PathParam("id") long id) throws IOException {
        return dao.getById(id);
    }

    @GET
    public List<Firmware> list() throws IOException {
        return dao.listAll();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(@FormDataParam("file") FormDataBodyPart bodyPart,
                         @FormDataParam("title") String title,
                         @FormDataParam("description") String description) {

        InputStream file = bodyPart.getValueAs(InputStream.class);

        String filePath = null;
        String uuid = UUID.randomUUID().toString();

        // Save File
        if (file != null) {
            try {
                String baseDirectory = OpenDeviceConfig.getHomeDirectory()  + "/data";
                filePath = "uploads/firmwares/" + uuid + ".bin";
                String path = baseDirectory + "/" + filePath;
                new File(path).getParentFile().mkdirs();
                IOUtils.write(path, file);
                log.debug("Saving file in: " + path);
            } catch (IOException ex) {
                return ErrorResponse.ERROR(ex.getMessage());
            }
        }

        Firmware firmware = new Firmware();
        firmware.setTitle(title);
        firmware.setDescription(description);
        firmware.setCreatedAt(new Date());
        firmware.setFilePath(filePath);
        dao.persist(firmware);

        return Response.ok(firmware).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) throws IOException {

        Firmware entity = dao.getById(id);

        if(MainTenantProvider.validadeEntity(entity)){ // check null ou invalid account
            return ErrorResponse.UNAUTHORIZED("UNAUTHORIZED - Invalid Account !");
        }

        // Remove file
        String baseDirectory = OpenDeviceConfig.getHomeDirectory()  + "/data";
        String path = baseDirectory + "/" + entity.getFilePath();
        new File(path).delete();

        dao.delete(entity);

        return Response.ok().build();

    }

    @GET
    @Path("{id}/sendUpdate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendUpdate(@PathParam("id") long id, @QueryParam("connection") String connectionUID) {

        BaseDeviceManager manager = ODev.getDeviceManager();

        DeviceConnection connection = manager.findConnection(connectionUID);

        Firmware firmware = dao.getById(id);

        log.info("Send firmware to : " + connection + ", firmware: " + firmware.getUuid());

        if(connection != null){
            try {
                connection.send(new FirmwareUpdateCommand(firmware.getUuid())); // FirmwareDownloadRest
            } catch (IOException e) {
                return ErrorResponse.BAD_REQUEST(e.getMessage());
            }

        }else{
            return ErrorResponse.UNAUTHORIZED("Invalid Device !");
        }

        return Response.ok().build();
    }
}
