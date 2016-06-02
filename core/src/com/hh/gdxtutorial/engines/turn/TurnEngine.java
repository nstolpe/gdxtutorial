package com.hh.gdxtutorial.engines.turn;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by nils on 5/31/16.
 */
public class TurnEngine {
    public Array<Actor> actors;
	public Actor active;
	public int turnCount = 0;
	public boolean running = false;

	public TweenManager tweenManager = new TweenManager();

	/*
	 * Empty constructor
	 */
	public TurnEngine() {
		this(new Array<Actor>());
	}

	public TurnEngine(Array<Actor> actors) {
		this.actors = actors;
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
	}
	public void start() {
		start(actors.get(0));
	}
	public void start(Actor first) {
		active = first;
		running = true;

		// put actors in their starting positions (and @TODO other transforms)
		for (Actor a : actors) {
			a.instance.transform.setTranslation(a.position);
		}
		active.startTurn();

		float x = MathUtils.random(-20, 20);
		float z = MathUtils.random(-20, 20);
		Tween.to(active.position, Vector3Accessor.XYZ, active.position.dst(x, 2, z) / 4).target(x, 2, z).setCallback(new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {

			}
		}).start(tweenManager);
	}

	public void update(float delta) {
		if (running) {
			tweenManager.update(delta);

//			if (!active.inTurn) {
//				active = actors.get((actors.indexOf(active, true) + 1) % actors.size);
//				active.startTurn();
//			}
			for (Actor actor : actors)
				actor.update(delta);
		}
	}
	public void advanceTurn() {

	}
	public void end() {

	}

	public class Turn {

	}

	public class Vector3Accessor implements TweenAccessor<Vector3> {
		public static final int X   = 0;
		public static final int Y   = 1;
		public static final int Z   = 2;
		public static final int XY  = 3;
		public static final int YZ  = 4;
		public static final int ZX  = 5;
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


}
