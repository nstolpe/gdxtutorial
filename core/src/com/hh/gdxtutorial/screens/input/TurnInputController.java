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
public class TurnInputController extends CameraInputController {
	public int interactButton = Input.Buttons.LEFT;
	final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
	final Vector3 intersection = new Vector3();

	public TurnInputController(Camera camera) {
		super(camera);
		rotateButton = Input.Buttons.RIGHT;
		translateButton = Input.Buttons.MIDDLE;
		forwardButton = 0;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		boolean ret = super.touchDown(screenX, screenY, pointer, button);
		// only send the TOUCH_CLICK_INPUT message if the interactButton is pressed
		if (button == interactButton) {
			Ray pickRay = camera.getPickRay(screenX, screenY);
			Intersector.intersectRayPlane(pickRay, xzPlane, intersection);
			MessageManager.getInstance().dispatchMessage(0, Messages.TOUCH_CLICK_INPUT, new Vector3(intersection.x, 0, intersection.z));
		}
		return ret;
	}
	protected static class GestureListener extends CameraInputController.CameraGestureListener {

	}
}
