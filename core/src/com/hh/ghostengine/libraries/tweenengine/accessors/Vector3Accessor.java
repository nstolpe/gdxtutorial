package com.hh.ghostengine.libraries.tweenengine.accessors;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nils on 6/2/16.
 */
public class Vector3Accessor implements TweenAccessor<Vector3> {
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int XY = 3;
	public static final int YZ = 4;
	public static final int ZX = 5;
	public static final int XYZ = 6;

	@Override
	public int getValues(Vector3 vec, int tweenType, float[] returnValues) {
		switch (tweenType) {
			case X:
				returnValues[0] = vec.x;
				return 1;
			case Y:
				returnValues[0] = vec.y;
				return 1;
			case Z:
				returnValues[0] = vec.z;
				return 1;
			case XY:
				returnValues[0] = vec.x;
				returnValues[1] = vec.y;
				return 2;
			case YZ:
				returnValues[0] = vec.y;
				returnValues[1] = vec.z;
				return 2;
			case ZX:
				returnValues[0] = vec.z;
				returnValues[1] = vec.x;
				return 2;
			case XYZ:
				returnValues[0] = vec.x;
				returnValues[1] = vec.y;
				returnValues[2] = vec.z;
				return 3;
			default:
				assert false;
				return -1;
		}
	}

	@Override
	public void setValues(Vector3 vec, int tweenType, float[] newValues) {
		switch (tweenType) {
			case X:
				vec.x = newValues[0];
				break;
			case Y:
				vec.y = newValues[0];
				break;
			case Z:
				vec.z = newValues[0];
				break;
			case XY:
				vec.x = newValues[0];
				vec.y = newValues[1];
				break;
			case YZ:
				vec.y = newValues[0];
				vec.z = newValues[1];
				break;
			case ZX:
				vec.z = newValues[0];
				vec.x = newValues[1];
				break;
			case XYZ:
				vec.set(newValues[0], newValues[1], newValues[2]);
				break;
			default:
				assert false;
				break;
		}
	}
}
