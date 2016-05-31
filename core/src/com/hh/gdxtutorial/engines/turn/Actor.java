package com.hh.gdxtutorial.engines.turn;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Actor {
	public static final int PLAYER = 0;
	public static final int MOB = 1;

	public ModelInstance instance;
	public int type;
	public float zPos = 2.0f;
	public Vector3 position = new Vector3();
	public Vector3 start = new Vector3();
	public Vector3 end = new Vector3();
	public boolean inTurn = false;
	public Vector3 direction;

	public Actor(ModelInstance instance, int type) {
		this.instance = instance;
		this.type = type;
	}

	public void act() {
//		if (inTurn) update();
//		else startTurn();
	}
	public void startTurn() {
		inTurn = true;
		instance.transform.getTranslation(start);
		float x = MathUtils.random(-20, 20);
		float z = MathUtils.random(-20, 20);
		end.set(x, 2, z);
		direction = start.sub(end).nor();
	}
	public void update(float delta) {
		instance.transform.getTranslation(position);
		if (start.dst(position) < start.dst(end)) {
			instance.transform.translate(direction.x * delta * 20, 0, direction.z * delta * 20);
		} else {
			instance.transform.setTranslation(end);
			endTurn();
		}
	}

	public void endTurn() {

	}
}
