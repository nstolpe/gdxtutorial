package com.hh.ghostengine.screens.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.hh.ghostengine.ai.Messages;

/**
 * Created by nils on 6/4/16.
 */
public class DemoInputController extends CameraInputController {
	// buttons and keys. see full list and what the constructor
	// overrides in CameraInputController
	public int interactButton = Input.Buttons.RIGHT;
	public int viewButton = Input.Buttons.LEFT;
	public int altLeft = Input.Keys.ALT_LEFT;
	protected boolean altLeftPressed;
	public int shiftLeft = Input.Keys.SHIFT_LEFT;
	protected boolean shiftLeftPressed;
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
		rotateRightKey = Input.Keys.E;
		rotateLeftKey = Input.Keys.Q;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		boolean ret = super.touchDown(screenX, screenY, pointer, button);
		// only send the TOUCH_CLICK_INPUT message if the interactButton is pressed
		if (button == interactButton && !altLeftPressed) {
			Ray pickRay = camera.getPickRay(screenX, screenY);
			Intersector.intersectRayPlane(pickRay, xzPlane, intersection);
			MessageManager.getInstance().dispatchMessage(0, Messages.TOUCH_CLICK_INPUT, new Vector3(screenX, 0, screenY));
//			MessageManager.getInstance().dispatchMessage(0, Messages.TOUCH_CLICK_INPUT, new Vector3(intersection.x, 0, intersection.z));
		}
		return ret;
	}

	@Override
	protected boolean process (float deltaX, float deltaY, int button) {
		if (button == viewButton && altLeftPressed) {
			if (!shiftLeftPressed) {
				tmpV1.set(camera.direction).crs(camera.up).y = 0f;
				camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
				camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
			} else {
				camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
				camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
				if (translateTarget) target.add(tmpV1).add(tmpV2);
			}
		} else if (button == translateButton) {
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
		else if (keycode == altLeft)            altLeftPressed = true;
		else if (keycode == shiftLeft)        shiftLeftPressed = true;

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
		else if (keycode == shiftLeft)        shiftLeftPressed = false;
		else if (keycode == altLeft)            altLeftPressed = false;

		return false;
	}

	@Override
	public void update (){
		if (rotateRightPressed || rotateLeftPressed || forwardPressed || backwardPressed || leftPressed || rightPressed || zoomInPressed || zoomOutPressed) {
			final float delta = Gdx.graphics.getDeltaTime();
			if (rotateRightPressed) camera.rotate(camera.up, -delta * rotateAngle);
			if (rotateLeftPressed) camera.rotate(camera.up, delta * rotateAngle);
			if (forwardPressed) {
				// check to correct for lock when looking straight down.
				if (camera.direction.equals(new Vector3(0,-1,0))) camera.rotate(new Vector3(-1, 0, 0), -1);

				camera.translate(tmpV1.set(camera.direction.x, 0, camera.direction.z).nor().scl(delta * translateUnits));

				if (forwardTarget) target.add(tmpV1);
			}
			if (backwardPressed) {
				// check to correct for lock when looking straight down.
				if (camera.direction.equals(new Vector3(0,-1,0))) camera.rotate(new Vector3(-1, 0, 0), 1);

				camera.translate(tmpV1.set(camera.direction.x, 0, camera.direction.z).nor().scl(-delta * translateUnits));
				if (forwardTarget) target.add(tmpV1);
			}
			if (leftPressed) {
				Vector3 left = new Vector3().set(camera.direction).crs(camera.up).nor();
				camera.translate(tmpV1.set(left).scl(-delta * translateUnits));
			}
			if (rightPressed) {
				Vector3 right = new Vector3().set(camera.direction).crs(camera.up).nor().scl(-1f);
				camera.translate(tmpV1.set(right).scl(-delta * translateUnits));
			}
			if (zoomInPressed) {
				zoom(delta * translateUnits);
			}
			if (zoomOutPressed) {
				zoom(-delta * translateUnits);
			}
			if (autoUpdate) camera.update();
		}
	}
	protected static class GestureListener extends CameraGestureListener {

	}
}
