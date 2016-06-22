package com.hh.gdxtutorial.tweenengine.accessors;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by nils on 6/17/16.
 */
public class QuaternionAccessor implements TweenAccessor<Quaternion> {
	public static final int ROTATION = 0;

//	public QuaternionAccessor() {
//		Tween.setCombinedAttributesLimit(4);
//	}
	@Override
	public int getValues(Quaternion quat, int tweenType, float[] returnValues) {
		switch (tweenType) {
			case ROTATION:
				returnValues[0] = quat.x;
				returnValues[1] = quat.y;
				returnValues[2] = quat.z;
				returnValues[3] = quat.w;
				return 4;
			default:
				return -1;
		}
	}

	@Override
	public void setValues(Quaternion quat, int tweenType, float[] newValues) {
		switch (tweenType) {
			case ROTATION:
				quat.set(newValues[0], newValues[1], newValues[2], newValues[3]).nor();
				break;
			default:
				break;
		}
	}
}
