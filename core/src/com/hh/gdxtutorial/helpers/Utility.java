package com.hh.gdxtutorial.helpers;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nils on 6/24/16.
 */
public class Utility {
	/**
	 * Gets the rotation from one Vector3 to another.
	 * @param origin
	 * @param target
	 * @return
	 * @TODO Make sure those epsilon values are working. Troubleshoot the instances where
	 * things move backwards. Use different axis is z won't work.
	 */
	public static Quaternion getRotationTo(Vector3 origin, Vector3 target, Quaternion fallback) {
		// Get the difference between the two points.
		Vector3 difference = target.cpy().sub(origin);
		Vector3 direction = difference.cpy().nor();

		if (difference.len() < 0.000000001f) return new Quaternion(fallback);

		// z is forward
		Vector3 zAxis = new Vector3(0,0,1);

		float dot = zAxis.dot(direction);
		float angle = (float) Math.acos(dot);

		if (angle < 0.000000001f) return new Quaternion(fallback);

		return new Quaternion().setFromCross(zAxis, direction);
	}
}
