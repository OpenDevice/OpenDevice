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

package br.com.criativasoft.opendevice.middleware.resources.voice.google;

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sensor;
import br.com.criativasoft.opendevice.middleware.utils.DeviceNameMatcher;
import br.com.criativasoft.opendevice.middleware.utils.Messages;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Locale;

/**
 * Service to handle Google Actions (Not SmartHome Acions).
 * It has been implemented to match a Portuguese language
 *
 * @author Ricardo JL Rufino
 *         Date: 13/07/18
 */
@Path("/voice/google/static")
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class GoogleStaticVoiceRest {

    private static final Logger log = LoggerFactory.getLogger(GoogleStaticVoiceRest.class);

    private static final String ACTION_ON = "Action.TurnOn";
    private static final String ACTION_OFF = "Action.TurnOff";

    @POST
    public Response request(JsonNode request) throws Exception {

//        System.out.println("Request: ");
//        System.out.println("=================================");
//        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
//        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter();
//        printer.indentObjectsWith(indenter); // Indent JSON objects
//        printer.indentArraysWith(indenter);  // Indent JSON arrays
//        ObjectMapper mapper = new ObjectMapper();
//
//        StringWriter out = new StringWriter();
//        mapper.writer(printer).writeValue(out, request);
//        System.out.println(out.toString());
//        System.out.println("=================================");

        JsonNode queryResult = request.get("queryResult");

        log.debug("QueryText: " + queryResult.get("queryText") + ", confidence: " + queryResult.get("intentDetectionConfidence"));

        boolean inputHasOnlyDevices = false;
        String action = null;
        String intent = null;
        String inputText = queryResult.get("queryText").asText();

        String[] langSplit = queryResult.get("languageCode").asText().split("-");
        Locale lang = new Locale(langSplit[0], langSplit[1].toUpperCase());

        JsonNode intentNode = queryResult.get("intent");

        if(intentNode != null && intentNode.has("displayName")){
            JsonNode displayName = intentNode.get("displayName");
            intent = displayName.asText().replaceAll("\"", "");
            log.debug("Intent: " + intent);
        }

        if(queryResult != null && queryResult.has("parameters")){

            JsonNode params = queryResult.get("parameters");

            JsonNode device = params.get("Device");

            // Device match in Google Actions Device/Entity List
            if(device != null && device.asText().length() > 0){
                inputHasOnlyDevices = true;
                inputText = device.asText();
                log.debug("Match Device (from Google): " + inputText);
            }


            JsonNode actionNode = params.get("Action");

            // Device Action
            if(actionNode != null && actionNode.asText().length() > 0){
                inputHasOnlyDevices = true;
                action = actionNode.asText();
                log.debug("Action: " + action);
            }
        }

        JSONObject response = new JSONObject();
        String responseMessage = null;

        if(action != null && action.equals(ACTION_ON) || action.equals(ACTION_OFF) ){
            try {
                ODev.getDeviceManager().getDevices();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            Collection<Device> devices = ODev.getDeviceManager().getDevices();
            DeviceNameMatcher nameMatcher = new DeviceNameMatcher(devices, inputHasOnlyDevices);

            Device device = nameMatcher.process(inputText);
            log.debug("Device found : " + device);

            if(device != null){

                // Control Actions
                if(ACTION_ON.equals(action) || ACTION_OFF.equals(action)){

                    if(device.getType() == Device.DIGITAL && !(device instanceof Sensor)){

                        // Check action
                        if(ACTION_ON.equals(action)){
                            if(device.isON()){
                                responseMessage = Messages.translate(Messages.DEVICE_CURRENT_ON, lang);
                            }else{
                            }

                            device.on();
                        }

                        // Check action
                        if(ACTION_OFF.equals(action)){
                            if(device.isOFF()){
                                responseMessage = Messages.translate(Messages.DEVICE_CURRENT_OFF, lang);
                            }else{
                            }

                            device.off();
                        }

                    }else{
                        log.warn("Define can not be controlled !");
                        responseMessage = Messages.translate(Messages.DEVICE_NOT_FOUND, lang);
                    }
                }

            }else{
                responseMessage = Messages.translate(Messages.DEVICE_NOT_FOUND, lang);
            }
        }

        if(responseMessage != null){
            log.debug("Response Message : " + responseMessage);
            response.put("fulfillmentText", responseMessage);
            return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
        }

        return Response.noContent().header("Content-Type", "application/json;charset=UTF-8").build();

    }



//    @POST
//    public Response request(JsonNode request) throws Exception {
//
//        System.out.println("Request: ");
//        System.out.println("=================================");
//        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
//        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter();
//        printer.indentObjectsWith(indenter); // Indent JSON objects
//        printer.indentArraysWith(indenter);  // Indent JSON arrays
//        ObjectMapper mapper = new ObjectMapper();
//
//        StringWriter out = new StringWriter();
//        mapper.writer(printer).writeValue(out, request);
//        System.out.println(out.toString());
//        System.out.println("=================================");
//
//        JsonNode queryResult = request.get("queryResult");
//
//        System.out.println("intentDetectionConfidence: " + queryResult.get("intentDetectionConfidence"));
//        System.out.println("queryText: " + queryResult.get("queryText"));
//
//        JsonNode intent = queryResult.get("intent");
//
//        if(intent != null && intent.has("displayName")){
//
//            JsonNode displayName = intent.get("displayName");
//
//            System.out.println("Action: " + displayName);
//
//        }
//
//        if(queryResult != null && queryResult.has("parameters")){
//
//            JsonNode params = queryResult.get("parameters");
//
//            System.out.println("params: " + params);
//
//        }
//
//        // TODO: Se tiver ligado, avisar que já está ligado.
//
//        JSONObject reponse = new JSONObject();
//
////        reponse.put("payload", new JSONObject(""));
//
//
////        reponse.put("payload", new JSONObject("{\n" +
////                "  \"telegram\": {\n" +
////                "    \"text\": \"Pick a color\",\n" +
////                "    \"reply_markup\": {\n" +
////                "      \"inline_keyboard\": [\n" +
////                "        [\n" +
////                "          {\n" +
////                "            \"text\": \"Red\",\n" +
////                "            \"callback_data\": \"Red\"\n" +
////                "          }\n" +
////                "        ],\n" +
////                "        [\n" +
////                "          {\n" +
////                "            \"text\": \"Green\",\n" +
////                "            \"callback_data\": \"Green\"\n" +
////                "          }\n" +
////                "        ],\n" +
////                "        [\n" +
////                "          {\n" +
////                "            \"text\": \"Yellow\",\n" +
////                "            \"callback_data\": \"Yellow\"\n" +
////                "          }\n" +
////                "        ],\n" +
////                "        [\n" +
////                "          {\n" +
////                "            \"text\": \"Blue\",\n" +
////                "            \"callback_data\": \"Blue\"\n" +
////                "          }\n" +
////                "        ],\n" +
////                "        [\n" +
////                "          {\n" +
////                "            \"text\": \"Pink\",\n" +
////                "            \"callback_data\": \"Pink\"\n" +
////                "          }\n" +
////                "        ]\n" +
////                "      ]\n" +
////                "    }\n" +
////                "  }\n" +
////                "}"));
//
//
//
//        reponse.put("fulfillmentMessages", new JSONArray("[\n" +
//                "  {\n" +
//                "    \"card\": {\n" +
//                "      \"title\": \"Exemplo de Card\",\n" +
//                "      \"subtitle\": \"Aqui uma breve descricao\",\n" +
//                "      \"imageUri\": \"https://assistant.google.com/static/images/molecule/Molecule-Formation-stop.png\",\n" +
//                "      \"buttons\": [\n" +
//                "        {\n" +
//                "          \"text\": \"Callback do Servidor\",\n" +
//                "          \"postback\": \"Ligar a luz do banheiro\"\n" +
//                "        }\n" +
//                "      ]\n" +
//                "    }\n" +
//                "  }\n" +
//                "]"));
//
//        // admin/invitation/asdasdasdasd
//
//        return Response.ok(reponse.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
//    }

}
