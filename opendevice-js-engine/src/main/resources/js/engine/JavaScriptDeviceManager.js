/**
 * This script contains the settings and imports to run user scripts
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */

load("nashorn:mozilla_compat.js");

importPackage(
    Packages.br.com.criativasoft.opendevice.core.model,
    Packages.br.com.criativasoft.opendevice.core.listener,
    Packages.br.com.criativasoft.opendevice.engine.js,
    Packages.br.com.criativasoft.opendevice.core.connection
);

if(!manager) manager = new JavaScriptDeviceManager();

var proxy = { };

Object.bindProperties(proxy, manager);