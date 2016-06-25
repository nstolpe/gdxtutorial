package com.hh.gdxtutorial.entity.components;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.helpers.Utility;
import com.hh.gdxtutorial.singletons.Manager;
import com.hh.gdxtutorial.tweenengine.accessors.QuaternionAccessor;
import com.hh.gdxtutorial.tweenengine.accessors.SlerpTween;

/**
 * Created by nils on 6/5/16.
 */
public class MobComponent implements Component {
	public StateMachine<MobComponent, MobState> stateMachine;

	public MobComponent() {
		stateMachine = new DefaultStateMachine<MobComponent, MobState>(this, MobState.REST);
	}

	public static enum MobState implements State<MobComponent> {
		REST() {
			@Override
			public void update(MobComponent ai) {

			}
		},
		TARGETING() {
			@Override
			public void enter(MobComponent ai) {
				// start rotate tween send message to this on callback
			}
			@Override
			public void exit(MobComponent ai) {

			}
		},
		ATTACK() {
			@Override
			public void update(MobComponent ai) {

			}
		},
		ATTACK_PRE() {
			@Override
			public void update(MobComponent ai) {

			}
		},
		ATTACK_POST() {
			@Override
			public void update(MobComponent ai) {

			}
		};

		@Override
		public void enter(MobComponent ai) {

		}

		@Override
		public void update(MobComponent ai) {

		}

		@Override
		public void exit(MobComponent ai) {

		}

		@Override
		public boolean onMessage(MobComponent ai, Telegram telegram) {
			System.out.println("in here now");
			switch (telegram.message) {
				case Messages.TARGET_ACQUIRED:
					Messages.TargetMessageData messageData = new Messages.TargetMessageData();
					try {
						messageData = (Messages.TargetMessageData) telegram.extraInfo;
					} catch (ClassCastException e) {
						Gdx.app.log("ClassCastException", e.getMessage());
						for (StackTraceElement ste : e.getStackTrace()) Gdx.app.log("Stack Trace", ste.getFileName() +  ", " + ste.getLineNumber() + " | " + ste.getClassName() + "." + ste.getMethodName());
						System.exit(-1);
					}
					faceTarget(messageData.actor, messageData.target);
					break;
				default:
					break;
			}
			return false;
		}

		/**
		 * Sets up data to face a target and executes a Tween
		 * @param actor
		 * @param target
		 */
		public void faceTarget(Entity actor, Entity target) {
			Vector3 position = Mappers.POSITION.get(actor).position;
			Quaternion rotation = Mappers.ROTATION.get(actor).rotation;
			Vector3 targetPosition = Mappers.POSITION.get(target).position;

			Quaternion rotationTo = Utility.getRotationTo(position, targetPosition, rotation);
			Quaternion qd = rotation.cpy().conjugate().mul(rotationTo);

			float angle = 2 * (float) Math.atan2(new Vector3(qd.x, qd.y, qd.z).len(), qd.w);

			// @TODO make duration configurable instead of just angle / 4
			SlerpTween.to(rotation, QuaternionAccessor.ROTATION, angle / 4)
					.target(rotationTo.x, rotationTo.y, rotationTo.z, rotationTo.w)
					.ease(Linear.INOUT)
					.setCallback(new TweenCallback() {
						@Override
						public void onEvent(int i, BaseTween<?> baseTween) {

						}
					}).start(Manager.getInstance().tweenManager);
		}
	}
}
