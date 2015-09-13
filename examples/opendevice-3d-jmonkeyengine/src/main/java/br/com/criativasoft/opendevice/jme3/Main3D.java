package br.com.criativasoft.opendevice.jme3;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import jme3.ext.JumpControl;
import jme3.ext.SimpleCameraState;
import jme3.ext.SimpleChaseCamera;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Ricardo JL Rufino
 * @date 03/07/2015
 */
public class Main3D extends SimpleApplication implements ConnectionListener, DeviceListener {

    Geometry geom;
    private Spatial androidModel;
    private float lastLocation = -1f;
    private float stepsLocation = 0.8f;

    private MaterialList materialList = new MaterialList();
    private Map<Device, Geometry> droids = new HashMap<Device, Geometry>();

    private LocalDeviceManager manager;


    public static void main(String[] args) {
        Main3D app = new Main3D();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
        aps.setResolution(800, 600);
        app.setSettings(aps);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        manager = new LocalDeviceManager();

        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setDownSamplingFactor(1.5f);
        bloom.setBloomIntensity(2f);
        bloom.setBlurScale(2f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);

        Box plane = new Box(5f, 0.05f, 5f);
        geom = new Geometry("plane", plane);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        geom.setMaterial(mat);
        geom.setLocalTranslation(Vector3f.ZERO);
        rootNode.attachChild(geom);

        androidModel = assetManager.loadModel("/Android.mesh.j3o");

        // Map colors to Device Ids
        createLightColorMaterial("1", ColorRGBA.Red);
        createLightColorMaterial("2", ColorRGBA.Yellow);
        createLightColorMaterial("3", ColorRGBA.Green);
        createLightColorMaterial("4", ColorRGBA.Orange);
        createLightColorMaterial("5", ColorRGBA.Blue);

//        NodeTreeViewer viewer = new NodeTreeViewer(rootNode);
//        viewer.setAlwaysOnTop(true);
//        viewer.setFocusable(true);
//        viewer.setAutoRequestFocus(true);
//        viewer.setSize(500,500);
//        viewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        viewer.setVisible(true);


        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -1f).normalizeLocal());
        dl.setColor(new ColorRGBA(1, 1, 1, 1));
        rootNode.addLight(dl);

        flyCam.setEnabled(false);
        viewPort.setBackgroundColor(ColorRGBA.Gray);

        SimpleCameraState simpleCamState = new SimpleCameraState(this);
        stateManager.attach(simpleCamState);
        SimpleChaseCamera chaseCam = simpleCamState.getChaseCamera();
        chaseCam.setRotateSpeed(3.0f);
        chaseCam.setTransformOffset(new Vector3f(0, 0.5f, 0));
        chaseCam.setSpatialToFollow(geom);
        chaseCam.updatePosition();
        chaseCam.constraintCamera();


        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, new String[]{"Click"});

        manager.addConnectionListener(this);
        manager.addListener(this);

        connect();
    }

    // ------------------------------  OpenDevice -------------------------------

    private void connect(){
        Thread thread = new Thread(){
            public void run() {
                try {
                    manager.connect(Connections.out.usb());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

        if(status == ConnectionStatus.CONNECTED){
            //
        }

    }

    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(message instanceof GetDevicesResponse){

            GetDevicesResponse response = (GetDevicesResponse) message;

            Collection<Device> devices = response.getDevices();
            for (final Device device : devices) {
                // Add LED(Digital/Output) devices
                if(device.getType() == DeviceType.DIGITAL && ! (device instanceof Sensor)) {
                    // Run in 3D UI Thread.
                    enqueue(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            addAndroidModel(device);
                            return null;
                        }
                    });

                }
            }

        }

    }

    @Override
    public void onDeviceChanged(Device device) {

        if(device.getType() == DeviceType.DIGITAL && ! (device instanceof Sensor)) {
            Geometry geometry = droids.get(device);
            assignMaterial(geometry);
        }

        if(device.getType() == DeviceType.DIGITAL && (device instanceof Sensor)) {
            if(device.isON()){
                Collection<Geometry> geometries = droids.values();
                for (Geometry target : geometries) {
                    Node parent = target.getParent();
                    parent.addControl(new JumpControl(20f, 1.5f));
                }
            }
        }
    }

    // ------------------------------  JMonkeyEngine -------------------------------


    private void addAndroidModel(Device device){
        Spatial clone = androidModel.clone();
        clone.setUserData("device", device.getId());

        // Model have 2 childs: Android, Eye
        Geometry geometry = (Geometry) ((Node)clone).getChild("Android");
        geometry.setUserData("device", device.getId());
        assignMaterial(geometry);

        clone.move(lastLocation, 0.45f, 0);
        lastLocation+=stepsLocation;
        rootNode.attachChild(clone);
        droids.put(device, geometry);
    }

    private void createLightColorMaterial(String name, ColorRGBA color){

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors",true);
        mat.setColor("Ambient", color);
        mat.setColor("Diffuse", color);
        materialList.put(name, mat);

        mat = mat.clone();
        mat.setColor("GlowColor", color);
        materialList.put(name + "Active", mat);

    }

    private void assignMaterial(Geometry geometry) {
        Integer deviceID = geometry.getUserData("device");
        Device device = manager.findDevice(deviceID);
        Material mat = materialList.get(device.getId() + (device.isON() ? "Active" : ""));
        System.out.println("Set material : " + mat + ", key " + device);
        geometry.setMaterial(mat);

    }

    @Override
    public void simpleUpdate(float tpf) {

    }

    private Geometry getClosestCollision() {
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, dir);
        rootNode.collideWith(ray, results);

        // Use the results -- we rotate the selected geometry.
        if (results.size() > 0) {
            // The closest result is the target that the player picked:
            return results.getClosestCollision().getGeometry();
        }

        return null;
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {

        if ("Click".equals(name) && !keyPressed) {

            Geometry target = getClosestCollision();
            System.out.println("getClosestCollision : " + target);

            if (target != null && target.getName().equals("Android")) {

                Integer deviceID = target.getUserData("device");
                Device device = manager.findDevice(deviceID);
                device.toggle(); // fire: onDeviceChanged

                Node parent = target.getParent();
                parent.addControl(new JumpControl(10f, 1f));
            }

        }
        }
    };



    ///--------------- DEBUG tools  -----------------------------------------

    private void attachCoordinateAxes(Vector3f pos){
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        return g;
    }
}
