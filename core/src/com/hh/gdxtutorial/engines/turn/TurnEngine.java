package com.hh.gdxtutorial.engines.turn;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.tween.accessors.Vector3Accessor;

/**
 * TurnEngine handles the running of turn actions for each Actor involved in a turn.
 */
public class TurnEngine implements Telegraph {
    public Array<Actor> actors;
	public Actor active;
	public int turnCount = 0;
	public boolean running = false;
	private int firstIndex;
	/**
	 * Empty constructor creates an empty actors array
	 */
	public TurnEngine() {
		this(new Array<Actor>());
	}
	/**
	 * Constructor. Takes an array of actors.
	 * @param actors
	 */
	public TurnEngine(Array<Actor> actors) {
		this.actors = actors;
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
		MessageManager.getInstance().addListener(this, Messages.ADVANCE_TURN_CONTROL);
	}
	/**
	 * Starts a turn with the first Actor in actors.
	 */
	public void start() {
		start(actors.get(0));
	}
	public void stop() {
		running = false;
		actors.clear();
		MessageManager.getInstance().removeListener(this, Messages.ADVANCE_TURN_CONTROL);
	}
	/**
	 * Starts a turn with the Actor identified by first. Actors will take their turns
	 * according to the ordering of actors.
	 * @param first
	 */
	public void start(Actor first) {
		running = true;
		active = first;

		// put actors in their starting positions (and @TODO other transforms)
		for (Actor a : actors) {
			a.instance.transform.setTranslation(a.position);
		}

		firstIndex = actors.indexOf(first, true);
		first.startTurn();
	}
	/**
	 * Updates the tweenManager and updates the actors.
	 * @TODO Get tweenManager out of here. Move to Actor.
	 * @param delta
	 */
	public void update(float delta) {
		if (running) {
			Actor.tweenManager.update(delta);

			for (Actor actor : actors) actor.update();
		}
	}
	/**
	 * Advance control to the next Actor.
	 */
	public void advanceActor() {
		int nextIndex = (actors.indexOf(active, true) + 1) % actors.size;
		if (nextIndex == firstIndex) endTurn();
//		startActor(actors.get(nextIndex));
		active = actors.get(nextIndex);
		actors.get(nextIndex).startTurn();
	}

	/**
	 * Ends a turn. Perform maintenance and stat gathering?
	 * @TODO Make something happen besides incrementing turnCount.
	 */
	public void endTurn() {
		turnCount++;
	}
	/**
	 * Overrides Telegraph.handleMessage.
	 * Calls advanceActor, should be triggered by an Actor at the end of its turn via Telegram.
	 * @param msg
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.ADVANCE_TURN_CONTROL:
				advanceActor();
				break;
			default:
				return false;
		}
		return true;
	}
}
