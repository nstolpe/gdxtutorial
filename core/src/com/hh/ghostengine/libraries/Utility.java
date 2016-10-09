package com.hh.ghostengine.libraries;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.hh.ghostengine.entity.components.Mappers;
import com.hh.ghostengine.entity.components.ModelInstanceComponent;
import com.hh.ghostengine.entity.systems.ModelBatchRenderer;
import com.hh.ghostengine.singletons.Manager;

/**
 * Created by nils on 6/24/16.
 */
public class Utility {
	/**
	 * Returns a Quaternion with the rotation from origin to target
	 * @param origin Vector of origin.
	 * @param target Vector to be faced.
	 * @return
	 */
	public static Quaternion facingRotation(Vector3 origin, Vector3 target) {
		Vector3 x = new Vector3(), y = new Vector3(), z = new Vector3();
		// set nz to direction from origin to target
		z.set(target).sub(origin).nor();
		// set nx perpendicular to nz and global Y
		x.set(Vector3.Y).crs(z).nor();
		// set ny perpendicular to nz -> nx
		y.set(z).crs(x).nor();
		// flipping vector name with field from the way
		// pass in via Matrix4.set() (x.y for y.x, z.x for x.z)
		// makes this work. need to figure out why.
		float values[] = new float[] {
			x.x, x.y, x.z, 0,
			y.x, y.y, y.z, 0,
			z.x, z.y, z.z, 0,
			  0,   0,   0, 1 };

		return new Matrix4(values).getRotation(new Quaternion());
	}

	public static float magnitude(Quaternion origin, Quaternion target) {
		Quaternion difference = origin.cpy().conjugate().mul(target);
		float angle = (float) (2 * Math.acos(difference.w));
		return angle > Math.PI ? (float) Math.abs(angle - 2 * Math.PI) : angle;
	}
	/**
	 * Tests if a ray from screen coordinates intersects any actors in an array of actors (Entities).
	 * @TODO This is ok here for now (better than in the state), but should move to its own lib eventually.
	 * @TODO Check bounding spheres first, then drill down to actual geometry.
	 * @TODO Only check targets on screen.
	 * @param screenX
	 * @param screenY
	 * @return
	 */
	public static int testTargets(Entity pc, Array<Entity> actors, float screenX, float screenY) {
		Ray ray = Manager.getInstance().engine().getSystem(ModelBatchRenderer.class).camera.getPickRay(screenX, screenY);
		int result = -1;
		float distance = -1;

		for (int i = 0; i < actors.size; i++) {
			if (actors.get(i).equals(pc)) continue;
			final ModelInstanceComponent mic = Mappers.MODEL_INSTANCE.get(actors.get(i));
			Vector3 position = mic.instance.transform.getTranslation(new Vector3());
			position.add(mic.center);

			final float len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);

			if (len < 0f) continue;

			float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);

			if (distance >= 0f && dist2 > distance) continue;

			// *2 seems more accurate than he squaring.
			// if (dist2 <= mic.radius * mic.radius) {
			if (dist2 <= mic.radius * 2) {
				result = i;
				distance = dist2;
			}
		}
		return result;
	}
	/**
	 * Original example code from Xoppa on irc. Doesn't achieve correct rotation.
	 * @param origin
	 * @param target
	 * @return
	 */
	public static Quaternion protoGetRotationTo(Vector3 origin, Vector3 target) {
		Vector3 nx = new Vector3(), ny = new Vector3(), nz = new Vector3();
		nz.set(target).sub(origin).nor();

		nx.set(Vector3.Y).crs(nz).nor();
		ny.set(nx).crs(nz).nor();

		return new Matrix4().set(nx, ny, nz, Vector3.Zero).getRotation(new Quaternion());
	}
	/**
	 * @deprecated
	 * Alternate way of rotating to face something. Here for posterity.
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
}
