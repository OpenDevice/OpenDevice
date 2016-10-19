/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.json;

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Configuration for Commands
 *
 * @author Ricardo JL Rufino
 * @date 11/07/14.
 */
public class CommandJacksonMapper  {

    private ObjectMapper mapper;

    public ObjectMapper getMapper() {
        if(mapper == null){
            mapper = new ObjectMapper();

            // Uses Enum.toString() for serialization of an Enum
            mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
            // Uses Enum.toString() for deserialization of an Enum
            mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SimpleModule module = new SimpleModule("OpenDeviceModule", new Version(0, 1, 0, "alpha"));

            module.addSerializer(CommandType.class, new EnumCodeSerializer());
            module.addDeserializer(CommandType.class, new EnumCodeDeserialize.CommandTypeDeserialize());
            module.addDeserializer(Command.class, new CommandJsonDeserialize());
            module.addDeserializer(DeviceCategory.class, new DeviceCategoryDeserialize());
//            module.addSerializer(ResponseCommand.class, new CommandJsonSerialize());

            //mapper.enableDefaultTyping();
            //mapper.setDefaultTyping(new ObjectMapper.DefaultTypeResolverBuilder());
            mapper.addMixInAnnotations(Command.class, CommandJsonSerialize.ResponseCommandIgnoreMixin.class);
//
//            List<Class<? extends  Command>> classList = new ArrayList<Class<? extends Command>>();
//            classList.add(Command.class);
//            classList.add(ResponseCommand.class);
//            classList.add(GetDevicesCommand.class);
//
//            for (Class<? extends Command> aClass : classList) {
//                mapper.addMixInAnnotations(aClass, JsonCommand.class);
//            }


            //mapper.getSubtypeResolver().registerSubtypes(new NamedType());
            mapper.registerModule(module);
        }
        return mapper;
    }


}
