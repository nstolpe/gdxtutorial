package com.hh.ghostengine.libraries.tweenengine;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.hh.ghostengine.entity.components.Mappers;
import com.hh.ghostengine.libraries.Utility;
import com.hh.ghostengine.libraries.tweenengine.accessors.QuaternionAccessor;
import com.hh.ghostengine.libraries.tweenengine.accessors.Vector3Accessor;
import com.hh.ghostengine.singletons.Manager;

/**
 * Created by nils on 7/2/16.
 */
public class Tweens {
	/**
	 * Returns a tween between two quaternions.
	 * @TODO this doesn't belong here really. Make a tween library or something.
	 */
	public static Tween rotateTo(Quaternion origin, Quaternion target, float speed, TweenEquation easing, TweenCallback callback) {
		// move to Tween library. Tween.rotateTo(origin, target, duration, callback, easing)
		return SlerpTween.to(origin, QuaternionAccessor.ROTATION, speed)
			.target(target.x, target.y, target.z, target.w)
			.ease(easing)
			.setCallback(callback);
	}
	public static Tween translateTo(Vector3 origin, Vector3 target, float speed, TweenEquation easing, TweenCallback callback) {
		return Tween.to(origin, Vector3Accessor.XYZ, speed)
			.target(target.x, target.y, target.z)
			.ease(easing)
			.setCallback(callback);
	}

	/**
	 * Sets up and starts a tween from Vector3 position to Vector3 destination for float duration.
	 * @param destination  Ending Vector3 for tween
	 * @TODO Move this to a Tween Library. Tweens.Vector3.Position(start, end, duration)
	 */
	public static void startTurnAction(Entity actor, Vector3 destination, TweenCallback callback) {
		Vector3 position = Mappers.POSITION.get(actor).position();
		Quaternion rotation = Mappers.ROTATION.get(actor).rotation();
		Quaternion targetRotation = Utility.facingRotation(position, destination);
		float speed = Utility.magnitude(rotation, targetRotation);
		// @TODO make speed divisors come from component.
		Tween rotate = rotateTo(rotation, targetRotation, speed / 4, Quad.INOUT, null);
		Tween translate = translateTo(position, destination, position.dst(destination) / 16, Quad.INOUT, null);
		Timeline.createSequence().push(rotate).push(translate).setCallback(callback).start(Manager.getInstance().tweenManager());
	}
}
