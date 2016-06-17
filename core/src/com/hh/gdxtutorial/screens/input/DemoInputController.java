package com.hh.gdxtutorial.screens.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.hh.gdxtutorial.ai.Messages;

/**
 * Created by nils on 6/4/16.
 */
public class DemoInputController extends CameraInputController {
	// buttons and keys. see full list and whatthe constructor
	// overrides in CameraInputController
	public int interactButton = Input.Buttons.LEFT;
	public int modifyKey = Input.Keys.CONTROL_LEFT;
	protected boolean modifyPressed;
	public int leftKey = Input.Keys.A;
	protected boolean leftPressed;
	public int rightKey = Input.Keys.D;
	protected boolean rightPressed;
	public int zoomInKey = Input.Keys.Z;
	protected boolean zoomInPressed;
	public int zoomOutKey = Input.Keys.X;
	protected boolean zoomOutPressed;

	// touch/click intersection handling.
	final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
	final Vector3 intersection = new Vector3();
	// for process(). They're in parent but are private.
	protected final Vector3 tmpV1 = new Vector3();
	protected final Vector3 tmpV2 = new Vector3();

	public DemoInputController(Camera camera) {
		super(camera);
		rotateButton = Input.Buttons.RIGHT;
		translateButton = Input.Buttons.MIDDLE;
		forwardButton = 0;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		boolean ret = super.touchDown(screenX, screenY, pointer, button);
		// only send the INTERACT_TOUCH message if the interactButton is pressed
		if (button == interactButton && !modifyPressed) {
			Ray pickRay = camera.getPickRay(screenX, screenY);
			Intersector.intersectRayPlane(pickRay, xzPlane, intersection);
			MessageManager.getInstance().dispatchMessage(0, Messages.INTERACT_TOUCH, new Vector3(intersection.x, 0, intersection.z));
		}
		return ret;
	}

	@Override
	protected boolean process (float deltaX, float deltaY, int button) {
		if (button == rotateButton && !modifyPressed) {
			tmpV1.set(camera.direction).crs(camera.up).y = 0f;
			camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
			camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
		} else if (button == translateButton || (button == rotateButton && modifyPressed)) {
			camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
			camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
			if (translateTarget) target.add(tmpV1).add(tmpV2);
		}
		if (autoUpdate) camera.update();
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == forwardKey)              forwardPressed = true;
		else if (keycode == backwardKey)       backwardPressed = true;
		else if (keycode == leftKey)               leftPressed = true;
		else if (keycode == rightKey)             rightPressed = true;
		else if (keycode == rotateRightKey) rotateRightPressed = true;
		else if (keycode == rotateLeftKey)   rotateLeftPressed = true;
		else if (keycode == zoomInKey)           zoomInPressed = true;
		else if (keycode == zoomOutKey)         zoomOutPressed = true;
		else if (keycode == modifyKey)           modifyPressed = true;

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == forwardKey)              forwardPressed = false;
		else if (keycode == backwardKey)       backwardPressed = false;
		else if (keycode == leftKey)               leftPressed = false;
		else if (keycode == rightKey)             rightPressed = false;
		else if (keycode == rotateRightKey) rotateRightPressed = false;
		else if (keycode == rotateLeftKey)   rotateLeftPressed = false;
		else if (keycode == zoomInKey)           zoomInPressed = false;
		else if (keycode == zoomOutKey)         zoomOutPressed = false;
		else if (keycode == modifyKey)           modifyPressed = false;

		return false;
	}

	protected static class GestureListener extends CameraGestureListener {

	}
}
