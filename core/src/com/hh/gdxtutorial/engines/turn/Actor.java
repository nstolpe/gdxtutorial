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
	public Vector3 origin = new Vector3();
	public Vector3 destination = new Vector3();
	public boolean inTurn = false;
	public Vector3 direction;

	public Actor() {}
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

		origin.set(position);
		// get random new values for x and z
		float x = MathUtils.random(-20, 20);
		float z = MathUtils.random(-20, 20);
		// cache the generated destination as destination
		destination.set(x, 2, z);
		// get the normalized direction vector to translate with
		direction = origin.sub(destination).nor();
	}

	public void update(float delta) {
		instance.transform.setTranslation(position);
	}

	public void endTurn() {
		inTurn = false;
	}
}
