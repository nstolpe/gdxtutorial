package com.hh.gdxtutorial.engines.turn;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Actor {
	public static final int PLAYER = 0;
	public static final int MOB = 1;

	public ModelInstance instance;
	public int type;
	public Vector3 position = new Vector3();
	public boolean inTurn = false;

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
