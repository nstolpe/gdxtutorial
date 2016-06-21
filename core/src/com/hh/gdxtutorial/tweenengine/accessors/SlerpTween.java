package com.hh.gdxtutorial.tweenengine.accessors;

import aurelienribon.tweenengine.Tween;

/**
 * Created by nils on 6/20/16.
 */
public class SlerpTween extends Tween {
	private SlerpTween() {
		super();
	}

	@Override
	protected void setup(Object target, int tweenType, float duration) {
		// target is the origin. cache here? then cache destination (actual target) in update?
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
			for (int i=0; i<combinedAttrsCnt; i++) {
				accessorBuffer[i] = startValues[i] + t * (targetValues[i] - startValues[i]);
			}

		} else {
			for (int i=0; i<combinedAttrsCnt; i++) {
				pathBuffer[0] = startValues[i];
				pathBuffer[1+waypointsCnt] = targetValues[i];
				for (int ii=0; ii<waypointsCnt; ii++) {
					pathBuffer[ii+1] = waypoints[ii*combinedAttrsCnt+i];
				}

				accessorBuffer[i] = path.compute(t, pathBuffer, waypointsCnt+2);
			}
		}

		accessor.setValues(target, type, accessorBuffer);
	}
}
