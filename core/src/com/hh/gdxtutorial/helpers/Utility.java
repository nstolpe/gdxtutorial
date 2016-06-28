package com.hh.gdxtutorial.helpers;

import com.badlogic.gdx.math.Matrix4;
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
	 * @TODO Make sure those epsilon values are working.
	 */
	public static Quaternion getRotationTo(Vector3 origin, Vector3 target, Quaternion rotation) {
		// http://stackoverflow.com/questions/29686149/find-axis-angle-of-one-vector3-relative-to-another
		// Get the difference between the two points.
		Vector3 difference = target.cpy().sub(origin);

		// if difference is too small, return the current rotation quat
		// epsilon here might be way too small.
		if (difference.len() < 0.000000001f) return new Quaternion(rotation);

		Vector3 direction = difference.cpy().nor();

		// z is forward
		Vector3 zAxis = new Vector3(Vector3.Z);
		float dot = zAxis.dot(direction);

		// if the target point is positive Z from origin, return a new quat
		if(dot == 1) return new Quaternion();
		// if the target point is negative Z from the origin, rotate 180 on up axis (Y in this case).
		if (dot == -1) return new Quaternion(Vector3.Y, 180);

		// get angle between vectors
		float angle = (float) Math.acos(dot);

		// if the angle is too small, return current rotation quat
		// as above, epsilon is probably too small.
		if (angle < 0.000000001f) return new Quaternion(rotation);

		return new Quaternion().setFromCross(zAxis, direction);
	}
	public static Quaternion getRotTo(Vector3 origin, Vector3 target, Quaternion rotation) {
		Vector3 nx = new Vector3(), ny = new Vector3(), nz = new Vector3();
		nz.set(target).sub(origin).nor();
		nx.set(nz).crs(Vector3.Y.cpy()).nor();
		ny.set(nz).crs(nx).nor();
		return new Matrix4().set(nx, ny, nz, Vector3.Zero).getRotation(new Quaternion());
	}
}
