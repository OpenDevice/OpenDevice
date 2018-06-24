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

package br.com.criativasoft.opendevice.middleware;

import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.LocalTenantProvider;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.extension.ViewExtension;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.core.util.StringUtils;
import br.com.criativasoft.opendevice.engine.js.OpenDeviceJSEngine;
import br.com.criativasoft.opendevice.middleware.config.DependencyConfig;
import br.com.criativasoft.opendevice.middleware.jobs.JobManager;
import br.com.criativasoft.opendevice.middleware.persistence.HibernateProvider;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import br.com.criativasoft.opendevice.middleware.resources.ConnectionsRest;
import br.com.criativasoft.opendevice.middleware.resources.DashboardRest;
import br.com.criativasoft.opendevice.middleware.resources.IndexRest;
import br.com.criativasoft.opendevice.middleware.rules.RuleManager;
import br.com.criativasoft.opendevice.middleware.test.PopulateInitialData;
import br.com.criativasoft.opendevice.middleware.test.TestRest;
import br.com.criativasoft.opendevice.mqtt.MQTTServerConnection;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.script.SimpleBindings;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// Run using Maven using: mvn compile exec:java -Dexec.mainClass=br.com.criativasoft.opendevice.middleware.Main

public class Main extends LocalDeviceManager {


	private static final Logger log = LoggerFactory.getLogger(Main.class);

    private SimpleBindings jscontext;
    private IWSServerConnection webscoket;

	public void start() throws IOException  {

        LocalTenantProvider.setAvoidChanges(true);
        setApiKey(OpenDeviceConfig.LOCAL_APP_ID); // Only for Startup

        OpenDeviceConfig config = ODev.getConfig();

        configLogging(config);

        MainDataManager manager = new MainDataManager();

        setDataManager(manager);

        // Server with suport for HTTP,Rest,WebSocket
        webscoket = Connections.in.websocket(config.getPort());

        jscontext = new SimpleBindings();
        jscontext.put("manager", this);
        jscontext.put("config", config);


        // Configuration Scripts (config.js)
        String userConfig = config.getStartupScript();
        if(!StringUtils.isEmpty(userConfig)){
            log.info("Using config file (JS): " + userConfig);
            loadScript(userConfig);
        }

        // Rest Resources
        // ================
        webscoket.addResource(IndexRest.class);
        webscoket.addResource(TestRest.class);
        webscoket.addResource(ConnectionsRest.class);

        if(config.isDatabaseEnabled()){
            webscoket.addResource(DashboardRest.class);
        }

        // Extract resources from JARS
        extractResources();

        // Static WebResources
        String rootWebApp = getWebAppDir();
        addWebResource(rootWebApp);
        log.debug("Current root-resource: " + rootWebApp);

        // External Resources
        List<String> externalResources = config.getExternalResources();
        for (String externalResource : externalResources) {
            log.info("Add external resource : " + externalResource + " exist: " + new File(externalResource).exists());
            addWebResource(externalResource);
        }

        this.addInput(webscoket);

        if(config.isMqttEnabled()) {
            MQTTServerConnection mqttServerConnection = new MQTTServerConnection();
            this.addInput(mqttServerConnection);
        }



        // new FakeSensorSimulator(50, this, 6, 7).start(); // generate fake data
        // addFilter(new FixedReadIntervalFilter(500, this));

        // Neo4j rasberry config: https://gist.github.com/widged/8329039
        //addOutput(Connections.out.tcp("192.168.0.204:8081"));
//        DeviceConnection conn = new RaspberryConnection() {
//            @Override
//            public void setup(GpioController gpio) {
//                attach(findDeviceByUID(1), RaspiPin.GPIO_01);
//                attach(findDeviceByUID(2), RaspiPin.GPIO_02);
//                attach(findDeviceByUID(3), RaspiPin.GPIO_03);
//                // attach(4, gpio.provisionDigitalInputPin(RaspiPin.GPIO_04));
//            }
//        };
//        addOutput(conn);

        EntityManager entityManager = null;

        if(config.isDatabaseEnabled()){
            log.info("Using database: " + config.getDatabasePath());
            entityManager = LocalEntityManagerFactory.getInstance().createEntityManager();
            HibernateProvider.setInstance(entityManager);
        }

        // Enable UDP discovery service.
        getDiscoveryService().listen();

        // Set IoC/DI Config
        Injector injector = Guice.createInjector(new DependencyConfig());
        GuiceInjectProvider.setInjector(injector);
        injector.injectMembers(manager);


        // Setup tenant provider (or use LocalTenantProvider)
        if(config.isTenantsEnabled()){
            MainTenantProvider provider = new MainTenantProvider(manager);
            config.setBindLocalVariables(false);
            TenantProvider.setProvider(provider);
        }


        // Init accounts
        if(entityManager != null){
            // FIXME: this can show startup
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();
            List<Account> accounts = manager.getAccountDao().listAll();

            if(accounts.isEmpty()) accounts = initDatabase(entityManager);

            TenantProvider provider = TenantProvider.getTenantProvider();

            for (Account account : accounts) {
                provider.addNewContext(account.getUuid());
            }
            tx.commit();
        }

        // TODO: Instead of looking for the entities manually it would be interesting to implement a
        // system of self-registration (features)
        RuleManager ruleManager = injector.getInstance(RuleManager.class);
        ruleManager.start();


        JobManager jobManager = injector.getInstance(JobManager.class);
        jobManager.start();


        this.connect();

//        // Create a JmDNS instance
//        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
//
//        // Register a service
//        ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "opendevice", config.getPort(), "path=index.html");
//        jmdns.registerService(serviceInfo);

    }

    /**
     * Init database for fist usage
     */
    private List<Account> initDatabase(EntityManager em) {
        PopulateInitialData populate = new PopulateInitialData(em);
        populate.initDatabase();
        return populate.getAccounts();
    }

    @Override
    public boolean transactionBegin() {

        EntityManager entityManager = HibernateProvider.getInstance();

        if(entityManager == null || !entityManager.isOpen()){
            entityManager = LocalEntityManagerFactory.getInstance().createEntityManager();
            HibernateProvider.setInstance(entityManager);
        }

        EntityTransaction tx = entityManager.getTransaction();

        if(tx.isActive()) return true;

        tx.begin();

        return false;

    }

    @Override
    public void transactionEnd() {

        EntityManager em = HibernateProvider.getInstance();

        HibernateProvider.setInstance(null);

        EntityTransaction local = em.getTransaction();

        try {
            if (local.isActive()) local.commit();
        }catch (RuntimeException e) {
            if ( local != null && local.isActive() ) local.rollback();
            throw e; // or display error message
        } finally {
        }

    }

    private String getWebAppDir(){

        String current = getClass().getClassLoader().getResource("").getPath();

        OpenDeviceConfig config = ODev.getConfig();

        String profile = config.getProfile();

        if(profile.equals(OpenDeviceConfig.PROFILE_DEV)){
            current = current.replaceAll("/target/classes", "/src/main/resources");
        }

        // Current directory (based in JAR)
        File webapp = new File(current + File.separator + "webapp" );
        if(webapp.exists()){
            return webapp.getPath();
        }

        // Current Directory
        current = System.getProperty("user.dir");
        webapp = new File(current + File.separator + "webapp" );
		if(webapp.exists()){
			return webapp.getPath();
		}

        // Using mvn exec
        webapp = new File(current + File.separator + "target"+ File.separator + "webapp" );
        if(webapp.exists()){
            return webapp.getPath();
        }

		// Find project in same directory...
		File currentDir = new File(current);
		String parent = currentDir.getParent();
		webapp = new File(parent + "/target/webapp" );
		if(webapp.exists()){
			return webapp.getPath();
		}
		
		return null;
	}

    protected boolean extractResources() {
        try {

            String destPath = getWebAppDir();

            // Load: User Interface Extensions
            // ======================================

            List<ViewExtension> extensions = getExtensions(ViewExtension.class);

            log.info("Additional user interface extension: ");

            for (ViewExtension extension : extensions) {
                log.info(" - " + extension.getClass());
                // Copy resources for webapp folder...
                extractResources(extension.getClass(), extension, destPath);
            }


        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    protected boolean extractResources(Class klass, ViewExtension extension, String destPath) throws IOException {

        String jarSource = klass.getProtectionDomain().getCodeSource().getLocation().getPath();
        log.debug("Extracting contents to: " + destPath);
        log.debug("Source: " + jarSource);

        if(!jarSource.endsWith(".jar") && !jarSource.endsWith(".war")) return false;

        // Check if has extracted
        File extensionMarker = new File(destPath, "webapp/ext/." + klass.getSimpleName()+".properties");
        if(extensionMarker.exists()){
            log.debug("Ignoring webapp content for extension: " + klass);
            return false;
        }else{
            extensionMarker.getParentFile().mkdirs();
            PrintWriter dynamic_plugins = new PrintWriter(extensionMarker);
            dynamic_plugins.println("install.date = " + new Date());
            dynamic_plugins.close();
        }

        JarFile jarFile = new JarFile(klass.getProtectionDomain().getCodeSource().getLocation().getPath());
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("webapp")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
                log.debug("Extracting: {}", entry.getName());
            }
        }
        return true;
    }
	

	public static void main(String[] args) throws Exception {
        launchApplication(Main.class, args);
    }

    private void configLogging(OpenDeviceConfig config){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset(); // Call context.reset() to clear any previous configuration, e.g. default
            String directory = OpenDeviceConfig.getConfigDirectory();
            configurator.doConfigure(directory + File.separator + config.getLogConfig());

        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private void loadScript(String file){

        try {
            OpenDeviceJSEngine.run(new File(file), jscontext);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addWebResource(String staticResourcePath){
        if(StringUtils.isEmpty(staticResourcePath)) return;
        webscoket.addWebResource(staticResourcePath);
    }

}
