package com.hh.ghostengine.libraries.tweenengine;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.hh.ghostengine.libraries.tweenengine.accessors.QuaternionAccessor;
import com.hh.ghostengine.libraries.tweenengine.accessors.Vector3Accessor;

/**
 * Created by nils on 7/2/16.
 */
public class Tweens {
	/**
	 * Returns a tween between two quaternions.
	 * @TODO this doesn't belong here really. Make a tween library or something.
	 */
	public static Tween rotateTo(Quaternion origin, Quaternion target, float duration, TweenEquation easing, TweenCallback callback) {
		// move to Tween library. Tween.rotateTo(origin, target, duration, callback, easing)
		return SlerpTween.to(origin, QuaternionAccessor.ROTATION, duration)
			.target(target.x, target.y, target.z, target.w)
			.ease(easing)
			.setCallback(callback);
	}
	public static Tween translateTo(Vector3 origin, Vector3 target, float duration, TweenEquation easing, TweenCallback callback) {
		return Tween.to(origin, Vector3Accessor.XYZ, duration)
			.target(target.x, target.y, target.z)
			.ease(easing)
			.setCallback(callback);
	}
}
