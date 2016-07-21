package com.hh.gdxtutorial.libraries.tweenengine;

import aurelienribon.tweenengine.Pool;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenPaths;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by nils on 6/20/16.
 */
public class SlerpTween extends Tween {
	public SlerpTween() {
		super();
		isRelative = true;
	}
	protected static final Pool.Callback<SlerpTween> slerpPoolCallback = new Pool.Callback<SlerpTween>() {
		@Override public void onPool(SlerpTween obj) {
			obj.reset();
		}
		@Override public void onUnPool(SlerpTween obj) {
			obj.reset();
		}
	};
	protected static final Pool<SlerpTween> slerpPool = new Pool<SlerpTween>(20, slerpPoolCallback) {
		@Override
		public SlerpTween create() {return new SlerpTween();}
	};
	@Override
	protected void setup(Object target, int tweenType, float duration) {
		super.setup(target, tweenType, duration);
	}

	@Override
	protected void initializeOverride() {
		if (target == null) return;

		accessor.getValues(target, type, startValues);

		for (int i=0; i<combinedAttrsCnt; i++) {
			targetValues[i] += isRelative ? startValues[i] : 0;

			for (int ii=0; ii<waypointsCnt; ii++) {
				waypoints[ii*combinedAttrsCnt+i] += isRelative ? startValues[i] : 0;
			}

			if (isFrom) {
				float tmp = startValues[i];
				startValues[i] = targetValues[i];
				targetValues[i] = tmp;
			}
		}
	}

	@Override
	protected void updateOverride(int step, int lastStep, boolean isIterationStep, float delta) {
		if (target == null || equation == null) return;

		// Case iteration end has been reached

		if (!isIterationStep && step > lastStep) {
			accessor.setValues(target, type, isReverse(lastStep) ? startValues : targetValues);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			accessor.setValues(target, type, isReverse(lastStep) ? targetValues : startValues);
			return;
		}

		// Validation

		assert isIterationStep;
		assert getCurrentTime() >= 0;
		assert getCurrentTime() <= duration;

		// Case duration equals zero

		if (duration < 0.00000000001f && delta > -0.00000000001f) {
			accessor.setValues(target, type, isReverse(step) ? targetValues : startValues);
			return;
		}

		if (duration < 0.00000000001f && delta < 0.00000000001f) {
			accessor.setValues(target, type, isReverse(step) ? startValues : targetValues);
			return;
		}

		// Normal behavior

		float time = isReverse(step) ? duration - getCurrentTime() : getCurrentTime();
		float t = equation.compute(time/duration);

		if (waypointsCnt == 0 || path == null) {
//			for (int i=0; i<combinedAttrsCnt; i++) {
//				accessorBuffer[i] = startValues[i] + t * (targetValues[i] - startValues[i]);
//			}
			accessorBuffer = slerp(startValues, targetValues, t);
		} else {
			//@TODO fix this or drop it, maybe a quaternion doesn't need waypoints?
//			for (int i=0; i<combinedAttrsCnt; i++) {
//				pathBuffer[0] = startValues[i];
//				pathBuffer[1+waypointsCnt] = targetValues[i];
//				for (int ii=0; ii<waypointsCnt; ii++) {
//					pathBuffer[ii+1] = waypoints[ii*combinedAttrsCnt+i];
//				}
//
//				accessorBuffer[i] = path.compute(t, pathBuffer, waypointsCnt+2);
//			}
			accessorBuffer = slerp(startValues, targetValues, t);
		}

		accessor.setValues(target, type, accessorBuffer);
	}

	public static float[] slerp(float[] startValues, float[] targetValues, float alpha) {
		Quaternion start = new Quaternion(startValues[0], startValues[1], startValues[2], startValues[3]);
		Quaternion end = new Quaternion(targetValues[0], targetValues[1], targetValues[2], targetValues[3]);
		final float d = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
		float absDot = d < 0.f ? -d : d;

		// Set the first and second scale for the interpolation
		float scale0 = 1f - alpha;
		float scale1 = alpha;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			final float angle = (float)Math.acos(absDot);
			final float invSinTheta = 1f / (float)Math.sin(angle);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = ((float)Math.sin((1f - alpha) * angle) * invSinTheta);
			scale1 = ((float)Math.sin((alpha * angle)) * invSinTheta);
		}

		if (d < 0.f) scale1 = -scale1;

		// Calculate the x, y, z and w values for the quaternion by using a
		// special form of linear interpolation for quaternions.
		start.x = (scale0 * start.x) + (scale1 * end.x);
		start.y = (scale0 * start.y) + (scale1 * end.y);
		start.z = (scale0 * start.z) + (scale1 * end.z);
		start.w = (scale0 * start.w) + (scale1 * end.w);
		return new float[] { start.x, start.y, start.z, start.w };
	}
	public static Tween call(TweenCallback callback) {
		SlerpTween tween = slerpPool.get();
		tween.setup(null, -1, 0);
		tween.setCallback(callback);
		tween.setCallbackTriggers(TweenCallback.START);
		return tween;
	}

	public static void ensurePoolCapacity(int minCapacity) {
		slerpPool.ensureCapacity(minCapacity);
	}

	@Override
	public void free() {
		slerpPool.free(this);
	}

	public static SlerpTween from(Object target, int tweenType, float duration) {
		SlerpTween tween = slerpPool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Quad.INOUT);
		tween.path(TweenPaths.catmullRom);
		tween.isFrom = true;
		return tween;
	}

	public static int getPoolSize() {
		return slerpPool.size();
	}

	public static SlerpTween mark() {
		SlerpTween tween = slerpPool.get();
		tween.setup(null, -1, 0);
		return tween;
	}

	public static SlerpTween set(Object target, int tweenType) {
		SlerpTween tween = slerpPool.get();
		tween.setup(target, tweenType, 0);
		tween.ease(Quad.INOUT);
		return tween;
	}

	public static SlerpTween to(Object target, int tweenType, float duration) {
		SlerpTween tween = slerpPool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Quad.INOUT);
		tween.path(TweenPaths.catmullRom);
		return tween;
	}
}
