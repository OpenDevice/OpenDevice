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

package br.com.criativasoft.opendevice.wsrest.io;

import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Performs transparent updates on JPA entities with Jackson's
 * The entity is loaded from the database using the ID from the request, and the data is injected into it,
 * returning a managed entity to the resource. <br/>
 *
 * If you do not have the ID, only deserialization is done.
 *
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
public class EntityJacksonReader implements MessageBodyReader{

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(Entity.class);
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

        Injector injector = GuiceInjectProvider.getInjector();

        EntityManager em = injector.getInstance(EntityManager.class);

        ObjectMapper mapper = injector.getInstance(ObjectMapper.class);

        JsonNode node = mapper.readTree(entityStream);

        JsonNode id = node.get("id");

        if(id != null && em != null){

            Object entity = em.find(type, Long.parseLong(id.asText()));

            mapper.readerForUpdating(entity).readValue(node);

            return entity;

        }else{

            return mapper.treeToValue(node, type);

        }

    }

}
