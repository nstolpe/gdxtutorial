package com.hh.gdxtutorial.engines.turn;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.tween.accessors.Vector3Accessor;

/**
 * Created by nils on 5/31/16.
 */
public class TurnEngine implements Telegraph {
    public Array<Actor> actors;
	public Actor active;
	public int turnCount = 0;
	public boolean running = false;

	public TweenManager tweenManager = new TweenManager();

	/*
	 * Empty constructor makes an empty actor array.
	 */
	public TurnEngine() {
		this(new Array<Actor>());
	}

	public TurnEngine(Array<Actor> actors) {
		this.actors = actors;
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
		MessageManager.getInstance().addListener(this, 0x01);
	}
	public void start() {
		start(actors.get(0));
	}
	public void start(Actor first) {
		running = true;

		// put actors in their starting positions (and @TODO other transforms)
		for (Actor a : actors) {
			a.instance.transform.setTranslation(a.position);
		}

		startTurn(first);
	}

	public void update(float delta) {
		if (running) {
			tweenManager.update(delta);

			for (Actor actor : actors) actor.update();
		}
	}
	public void advanceTurn() {
		startTurn(actors.get((actors.indexOf(active, true) + 1) % actors.size));
	}
	public void end() {

	}

	@Override
	public boolean handleMessage(Telegram msg) {
		advanceTurn();
		return true;
	}

	private void startTurn(Actor a) {
		active = a;
		float x = MathUtils.random(-20, 20);
		float z = MathUtils.random(-20, 20);

		Tween.to(a.position, Vector3Accessor.XYZ, a.position.dst(x, 2, z) / 8)
			.target(x, 2, z)
			.setCallback(new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					switch (type) {
						case COMPLETE:
							MessageManager.getInstance().dispatchMessage(0, 0x01);
							break;
						default:
							assert false;
							break;
					}
				}
			})
			.ease(Linear.INOUT)
			.start(tweenManager);
	}


}
