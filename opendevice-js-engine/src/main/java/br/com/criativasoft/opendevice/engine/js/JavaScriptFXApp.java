///*
// * *****************************************************************************
// * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// *  Contributors:
// *  Ricardo JL Rufino - Initial API and Implementation
// * *****************************************************************************
// */
//
//package br.com.criativasoft.opendevice.engine.js;
//
//import javafx.application.Application;
//import javafx.stage.Stage;
//
//import javax.script.ScriptException;
//import javax.script.SimpleBindings;
//import java.io.File;
//import java.io.FileNotFoundException;
//
///*
// * Need install dependencies on ubuntu:
// * sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2
// */
//
///**
// * @author Ricardo JL Rufino
// * @date 23/08/15.
// */
//public class JavaScriptFXApp extends Application {
//
//    @Override
//    public void start(Stage stage){
//
//        String script = getParameters().getRaw().get(0);
//
//        SimpleBindings bindings = new SimpleBindings();
//        bindings.put("stage", stage);
//
//        try {
//            OpenDeviceJSEngine.run(new File(script), bindings);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (ScriptException e) {
//            e.printStackTrace();
//        }
//    }
//}
