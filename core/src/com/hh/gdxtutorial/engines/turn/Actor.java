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

	public void update() {
		instance.transform.setTranslation(position);
	}

	public void endTurn() {
		inTurn = false;
	}
}
