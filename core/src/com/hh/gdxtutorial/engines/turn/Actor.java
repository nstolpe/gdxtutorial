package com.hh.gdxtutorial.engines.turn;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.libraries.tweenengine.accessors.Vector3Accessor;

public class Actor implements Telegraph {
	public static final int PLAYER = 0;
	public static final int MOB = 1;
	public static TweenManager tweenManager = new TweenManager();

	public ModelInstance instance;
	public int type;
	public Vector3 position = new Vector3();
	public boolean inTurn = false;

	public Actor(ModelInstance instance, int type) {
		this.instance = instance;
		this.type = type;
	}

	public void update() {
		instance.transform.setTranslation(position);
	}

	public void startTurn() {
		switch (type) {
			case MOB:
				runAi();
				break;
			case PLAYER:
				queuePlayerInput();
				break;
			default:
				break;
		}
	}

	private void queuePlayerInput() {
		MessageManager.getInstance().addListener(this, Messages.TOUCH_CLICK_INPUT);
	}

	private void runAi() {
		moveTo(new Vector3(MathUtils.random(-20, 20), 2, MathUtils.random(-20, 20)));
	}

	private void moveTo(Vector3 destination) {
		Tween.to(this.position, Vector3Accessor.XYZ, this.position.dst(destination) / 8)
			.target(destination.x, destination.y, destination.z)
			.setCallback(new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					switch (type) {
						case COMPLETE:
							MessageManager.getInstance().dispatchMessage(0, Messages.ADVANCE_TURN_CONTROL);
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

	@Override
	public boolean handleMessage(Telegram msg) {
		MessageManager.getInstance().removeListener(this, Messages.TOUCH_CLICK_INPUT);
		Vector3 xz = (Vector3) msg.extraInfo;
		moveTo(new Vector3(xz.x, 2, xz.z));
		return false;
	}
}
