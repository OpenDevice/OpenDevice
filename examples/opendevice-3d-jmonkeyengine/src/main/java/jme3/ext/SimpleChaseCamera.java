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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3.ext;

import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author mifth
 */
public class SimpleChaseCamera implements ActionListener, AnalogListener {

    private Node chaseGeneralNode, chaseCamNode, chaseRotateHelper;
    private Application app;
    private InputManager inputManager;
    private String ChaseCamDown = "ChaseCamDown";
    private String ChaseCamUp = "ChaseCamUp";
    private String ChaseCamZoomIn = "ChaseCamZoomIn";
    private String ChaseCamZoomOut = "ChaseCamZoomOut";
    private String ChaseCamMoveLeft = "ChaseCamMoveLeft";
    private String ChaseCamMoveRight = "ChaseCamMoveRight";
    private String ChaseCamToggleRotate = "ChaseCamToggleRotate";
    private boolean canRotate, doRotate, doVerticalConstraint, canZoom, doZoom, zoomIn, zoomRelativeToDistance;
    private float horizontRotate, verticalRotate, verticalUpLimit, verticalDownLimit;
    private float rotateSpeed, zoomStep, zoomMax, zoomMin;
    private String[] inputs;
    private boolean enabled;
    private Spatial spatialToFollow;
    private Vector3f transformOffset = null;

    public SimpleChaseCamera(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;

        enabled = true;

        canRotate = true;
        doRotate = false;
        horizontRotate = 0.0f;
        verticalRotate = 0.0f;
        verticalUpLimit = FastMath.DEG_TO_RAD * 30;
        verticalDownLimit = FastMath.DEG_TO_RAD * 95;
        doVerticalConstraint = true;
        rotateSpeed = 1.0f;

        canZoom = true;
        doZoom = false;
        zoomIn = false;
        zoomStep = 1.0f;
        zoomMin = 2f;
        zoomMax = 100f;
        zoomRelativeToDistance = true;

        chaseGeneralNode = new Node("chaseNode");
        chaseCamNode = new Node("chaseCamNode");
        chaseGeneralNode.attachChild(chaseCamNode);
        chaseRotateHelper = new Node("chaaseRotateHelper");
        chaseGeneralNode.attachChild(chaseRotateHelper);


        chaseCamNode.setLocalTranslation(0, 0, 5f);
        chaseCamNode.setLocalRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));

        registerWithInput(inputManager);

    }

    /**
     * Registers inputs with the input manager
     *
     * @param inputManager
     */
    public final void registerWithInput(InputManager inputManager) {

        inputs = new String[]{ChaseCamToggleRotate,
            ChaseCamDown,
            ChaseCamUp,
            ChaseCamMoveLeft,
            ChaseCamMoveRight,
            ChaseCamZoomIn,
            ChaseCamZoomOut};

//        this.inputM = inputManager;
//        if (!invertYaxis) {
        inputManager.addMapping(ChaseCamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(ChaseCamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
//        } else {
//            inputManager.addMapping(ChaseCamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
//            inputManager.addMapping(ChaseCamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
//        }
        inputManager.addMapping(ChaseCamZoomIn, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ChaseCamZoomOut, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
//        if (!invertXaxis) {
        inputManager.addMapping(ChaseCamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(ChaseCamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, false));
//        } else {
//            inputManager.addMapping(ChaseCamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, false));
//            inputManager.addMapping(ChaseCamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, true));
//        }
        inputManager.addMapping(ChaseCamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
//        inputManager.addMapping(ChaseCamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this, inputs);
    }

    private void rotateHorizontally(float value) {
        chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(new Quaternion().fromAngleAxis(value * 1f, Vector3f.UNIT_Y)));

    }

    private void rotateVertically(float value) {
        chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(new Quaternion().fromAngleAxis(value * 1f, Vector3f.UNIT_X)));
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (enabled) {
            if (canRotate) {
                if (name.equals(ChaseCamToggleRotate) && isPressed) {
                    doRotate = true;
                } else if (name.equals(ChaseCamToggleRotate) && !isPressed) {
                    doRotate = false;
                    horizontRotate = 0;
                    verticalRotate = 0;
                }
            }

            if (canZoom) {
                if (name.equals(ChaseCamZoomIn) && isPressed) {
                    doZoom = true;
                    zoomIn = true;
                } else if (name.equals(ChaseCamZoomOut) && isPressed) {
                    doZoom = true;
                    zoomIn = false;
                }
            }
        }
    }

    public void onAnalog(String name, float value, float tpf) {

        if (enabled) {
            if (canRotate && doRotate) {
                if (name.equals(ChaseCamMoveLeft)) {
                    horizontRotate = value;
                } else if (name.equals(ChaseCamMoveRight)) {
                    horizontRotate = -value;
                } else if (name.equals(ChaseCamUp)) {
                    verticalRotate = value;
                } else if (name.equals(ChaseCamDown)) {
                    verticalRotate = -value;
                }
            }
        }
    }

    public void constraintCamera() {
        // Over 180
        float angleVerticalNow_y = chaseGeneralNode.getLocalRotation().mult(Vector3f.UNIT_Y).normalize().
                angleBetween(Vector3f.UNIT_Y);

        if (angleVerticalNow_y > FastMath.HALF_PI) {
            Quaternion xRotAgain = new Quaternion().fromAngleAxis(angleVerticalNow_y - FastMath.HALF_PI, Vector3f.UNIT_X);
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRotAgain));
        }

        // LIMITS
        float angleVerticalNow_z_inverted = chaseGeneralNode.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().
                angleBetween(Vector3f.UNIT_Y);
//                    System.out.println(angleVerticalNow_z_inverted);
        if (angleVerticalNow_z_inverted < verticalUpLimit) {
            Quaternion xRotAgain2 = new Quaternion().fromAngleAxis((verticalUpLimit - angleVerticalNow_z_inverted), Vector3f.UNIT_X);
            xRotAgain2.negate();
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRotAgain2));
        } else if (angleVerticalNow_z_inverted > verticalDownLimit) {
            Quaternion xRotAgain3 = new Quaternion().fromAngleAxis((verticalDownLimit - angleVerticalNow_z_inverted), Vector3f.UNIT_X);
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRotAgain3));
        }
    }

    public void updatePosition() {
        if (spatialToFollow != null) {
            if (transformOffset != null) {
                chaseGeneralNode.setLocalTranslation(spatialToFollow.getLocalTranslation().add(transformOffset));
            } else {
                chaseGeneralNode.setLocalTranslation(spatialToFollow.getLocalTranslation());
            }

        }
    }

    public void update() {

        if (enabled) {
            // MOVE TO SPATIAL
            updatePosition();

            if (canRotate && doRotate) {

                // HORIZONTAL
                Quaternion chaseRot = chaseGeneralNode.getLocalRotation().clone();
                chaseGeneralNode.setLocalRotation(new Quaternion());
                chaseRotateHelper.setLocalRotation(chaseRot);

                Quaternion yRot = new Quaternion().fromAngleAxis(horizontRotate * rotateSpeed, Vector3f.UNIT_Y);
                chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(yRot));
                chaseGeneralNode.setLocalRotation(chaseRotateHelper.getWorldRotation());


                // VERTICAL
                Quaternion xRot = new Quaternion().fromAngleAxis(verticalRotate * rotateSpeed, Vector3f.UNIT_X);
                chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRot));


                // VERTICAL LIMITATION
                if (doVerticalConstraint) {
                    constraintCamera();
                }

                horizontRotate = 0f;
                verticalRotate = 0f;
            }

            if (canZoom && doZoom) {
                Vector3f zoomVec = Vector3f.UNIT_Z.clone().multLocal(zoomStep);
                if (zoomRelativeToDistance) {
                    zoomVec.multLocal(0.5f + (0.05f * chaseCamNode.getLocalTranslation().getZ()));
                }

                if (zoomIn) {
                    chaseCamNode.setLocalTranslation(chaseCamNode.getLocalTranslation().add(zoomVec.negateLocal()));
                } else {
                    chaseCamNode.setLocalTranslation(chaseCamNode.getLocalTranslation().add(zoomVec));
                }

                if (chaseCamNode.getLocalTranslation().z > zoomMax) {
                    chaseCamNode.setLocalTranslation(new Vector3f(0, 0, zoomMax));
                } else if (chaseCamNode.getLocalTranslation().z < zoomMin) {
                    chaseCamNode.setLocalTranslation(new Vector3f(0, 0, zoomMin));
                }

                doZoom = false;
            }

            app.getCamera().setLocation(chaseCamNode.getWorldTranslation());
            app.getCamera().setRotation(chaseCamNode.getWorldRotation());

        }
    }

    public void destroy() {
        transformOffset = null;
        inputManager.removeListener(this);
        app = null;
        inputManager = null;
    }

    public void setEnabled() {
        if (!enabled) {
            inputManager.addListener(this, inputs);
            enabled = true;
        }

    }

    public void setDisabled() {
        if (enabled) {
            inputManager.removeListener(this);
            enabled = false;
        }

    }

    public boolean isCanRotate() {
        return canRotate;
    }

    public void setCanRotate(boolean canRotate) {
        if (!canRotate) {
            doRotate = false;
            horizontRotate = 0f;
            verticalRotate = 0f;
        }

        this.canRotate = canRotate;
    }

    public boolean isCanZoom() {
        return canZoom;
    }

    public void setCanZoom(boolean canZoom) {
        if (!canZoom) {
            doZoom = false;
        }
        this.canZoom = canZoom;
    }

    public void setZoom(float zoomValue) {
        chaseCamNode.setLocalTranslation(new Vector3f(0f, 0f, zoomValue));
    }

    public float getZoom() {
        return chaseCamNode.getWorldTranslation().distance(chaseGeneralNode.getLocalTranslation());
    }

    public Vector3f getCameraNodeLocation() {
        return chaseGeneralNode.getLocalTranslation();
    }

    public void setCameraNodeLocation(Vector3f location) {
        chaseGeneralNode.setLocalTranslation(location);
    }

    public Quaternion getCameraNodeRotation() {
        return chaseGeneralNode.getLocalRotation();
    }

    public void setCameraNodeRotation(float xRot, float yRot) {
        chaseGeneralNode.setLocalRotation(new Quaternion());
        chaseGeneralNode.rotate(xRot, yRot, 0f);
    }

    public float getRotateSpeed() {
        return rotateSpeed;
    }

    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public float getVerticalUpLimit() {
        return verticalUpLimit;
    }

    public void setVerticalUpLimit(float verticalUpLimit) {
        this.verticalUpLimit = verticalUpLimit;
    }

    public float getVerticalDownLimit() {
        return verticalDownLimit;
    }

    public void setVerticalDownLimit(float verticalDownLimit) {
        this.verticalDownLimit = verticalDownLimit;
    }

    public float getZoomMax() {
        return zoomMax;
    }

    public void setZoomMax(float zoomMax) {
        this.zoomMax = zoomMax;
    }

    public float getZoomMin() {
        return zoomMin;
    }

    public void setZoomMin(float zoomMin) {
        this.zoomMin = zoomMin;
    }

    public float getZoomStep() {
        return zoomStep;
    }

    public void setZoomStep(float zoomStep) {
        this.zoomStep = zoomStep;
    }

    public boolean isDoVerticalConstraint() {
        return doVerticalConstraint;
    }

    public void setDoVerticalConstraint(boolean doVerticalConstraint) {
        this.doVerticalConstraint = doVerticalConstraint;
    }

    public boolean isZoomRelativeToDistance() {
        return zoomRelativeToDistance;
    }

    public void setZoomRelativeToDistance(boolean zoomRelativeToDistance) {
        this.zoomRelativeToDistance = zoomRelativeToDistance;
    }

    public Spatial getSpatialToFollow() {
        return spatialToFollow;
    }

    public void setSpatialToFollow(Spatial spatialToFollow) {
        this.spatialToFollow = spatialToFollow;
    }

    public Vector3f getTransformOffset() {
        return transformOffset;
    }

    public void setTransformOffset(Vector3f transformOffset) {
        this.transformOffset = transformOffset;
    }

    /**
     * Sets custom triggers for toggleing the rotation of the cam deafult are
     * new MouseButtonTrigger(MouseInput.BUTTON_LEFT) left mouse button new
     * MouseButtonTrigger(MouseInput.BUTTON_RIGHT) right mouse button
     *
     * @param triggers
     */
    public void setToggleRotationTrigger(Trigger... triggers) {
        inputManager.deleteMapping(ChaseCamToggleRotate);
        inputManager.addMapping(ChaseCamToggleRotate, triggers);
        inputManager.addListener(this, ChaseCamToggleRotate);
    }

    /**
     * Sets custom triggers for zomming in the cam default is new
     * MouseAxisTrigger(MouseInput.AXIS_WHEEL, true) mouse wheel up
     *
     * @param triggers
     */
    public void setZoomInTrigger(Trigger... triggers) {
        inputManager.deleteMapping(ChaseCamZoomIn);
        inputManager.addMapping(ChaseCamZoomIn, triggers);
        inputManager.addListener(this, ChaseCamZoomIn);
    }

    /**
     * Sets custom triggers for zomming out the cam default is new
     * MouseAxisTrigger(MouseInput.AXIS_WHEEL, false) mouse wheel down
     *
     * @param triggers
     */
    public void setZoomOutTrigger(Trigger... triggers) {
        inputManager.deleteMapping(ChaseCamZoomOut);
        inputManager.addMapping(ChaseCamZoomOut, triggers);
        inputManager.addListener(this, ChaseCamZoomOut);
    }
}
