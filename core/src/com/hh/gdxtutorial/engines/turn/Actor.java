package com.hh.gdxtutorial.engines.turn;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.tween.accessors.Vector3Accessor;

public class Actor {
	public static final int PLAYER = 0;
	public static final int MOB = 1;
	public static TweenManager tweenManager = new TweenManager();

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

	public void startTurn() {
		float x = MathUtils.random(-20, 20);
		float z = MathUtils.random(-20, 20);

		Tween.to(this.position, Vector3Accessor.XYZ, this.position.dst(x, 2, z) / 8)
			.target(x, 2, z)
			.setCallback(new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					switch (type) {
						case COMPLETE:
							MessageManager.getInstance().dispatchMessage(0, Messages.ADVANCE_ACTOR);
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
	public void endTurn() {
		inTurn = false;
	}
}
