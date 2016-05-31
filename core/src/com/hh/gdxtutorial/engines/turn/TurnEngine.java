package com.hh.gdxtutorial.engines.turn;

import com.badlogic.gdx.utils.Array;

/**
 * Created by nils on 5/31/16.
 */
public class TurnEngine {
    public Array<Actor> actors = new Array<Actor>();
	public Actor active;
	public int turnCount = 0;
	public boolean running = false;

	public TurnEngine() {}

	public TurnEngine(Array<Actor> actors) {
		this.actors = actors;
	}

	public void start(Actor first) {
		active = first == null ? actors.get(0) : first;
		running = true;
		active.startTurn();
	}

	public void update(float delta) {
		if (running && active.inTurn) active.update(delta);
	}
	public void advanceTurn() {

	}
	public void end() {

	}

	public class Turn {

	}


}
