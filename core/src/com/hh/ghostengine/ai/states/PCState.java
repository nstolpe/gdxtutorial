package com.hh.ghostengine.ai.states;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.hh.ghostengine.ai.Messages;
import com.hh.ghostengine.entity.components.EffectsComponent;
import com.hh.ghostengine.entity.components.Mappers;
import com.hh.ghostengine.entity.components.ModelInstanceComponent;
import com.hh.ghostengine.entity.components.TargetComponent;
import com.hh.ghostengine.entity.systems.ModelBatchRenderer;
import com.hh.ghostengine.entity.systems.TurnSystem;
import com.hh.ghostengine.libraries.Utility;
import com.hh.ghostengine.libraries.tweenengine.Tweens;
import com.hh.ghostengine.singletons.Manager;

/**
 * Created by nils on 6/26/16.
 */
public enum PCState implements State<Entity> {
	// rest state, plays the floating animation
	REST() {
		@Override
		public void enter(Entity pc) {
			Mappers.MODEL_INSTANCE.get(pc).controller.animate("skeleton|rest", -1, null, 1);
		}
	},
	WAIT() {
		/**
		 * Register's the player character stateMachine as a listener to mouse/touch input.
		 * @param pc
		 * @TODO make the message something more specific to what is happening, TOUCH_CLICK_INPUT should trigger the sending of that message.
		 */
		@Override
		public void enter(Entity pc) {
			MessageManager.getInstance().addListener(Mappers.PC.get(pc).stateMachine, Messages.TOUCH_CLICK_INPUT);
		}
		/**
		 * Handles incoming messages related to touch/click input. Ignores the rest.
		 * @param pc
		 * @param telegram
		 * @return
		 */
		@Override
		public boolean onMessage(Entity pc, Telegram telegram) {
			switch (telegram.message) {
				case Messages.TOUCH_CLICK_INPUT:
					Vector3 screenCoordinates = (Vector3) telegram.extraInfo;
					ImmutableArray<Entity> actors = Manager.getInstance().engine().getSystem(TurnSystem.class).actors();
					int targetIndex = Utility.testTargets(pc, new Array<Entity>(actors.toArray()), screenCoordinates.x, screenCoordinates.z);

					if (targetIndex < 0) {
						Ray ray = Manager.getInstance().engine().getSystem(ModelBatchRenderer.class).camera.getPickRay(screenCoordinates.x, screenCoordinates.z);
						Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
						Vector3 intersection = new Vector3();
						Intersector.intersectRayPlane(ray, xzPlane, intersection);
						Vector3 position = Mappers.POSITION.get(pc).position;
						Quaternion rotation = Mappers.ROTATION.get(pc).rotation;
						Quaternion targetRotation = Utility.facingRotation(position, intersection);
						float speed = Utility.magnitude(rotation, targetRotation);
						Tween rotate = Tweens.rotateTo(rotation, targetRotation, speed / 4, Linear.INOUT, null);
						final Entity fpc = pc;
						TweenCallback callback = new TweenCallback() {
							@Override
							public void onEvent(int type, BaseTween<?> source) {
								switch (type) {
									case COMPLETE:
										// Move the else below to it's own state that requires target selection,
										// then enter it through this callback (as well as in the else).
										break;
									default:
										break;
								}
							}
						};
						Tween translate = Tweens.translateTo(position, intersection, position.dst(intersection) / 16, Linear.INOUT, null);
						Timeline.createSequence().push(rotate).push(translate).setCallback(callback).start(Manager.getInstance().tweenManager());
					} else {
						pc.add(new TargetComponent(actors.get(targetIndex)));
						Mappers.PC.get(pc).stateMachine.changeState(TARGETING);
					}
					break;
				default:
					break;
			}
			return false;
		}
	},
	/**
	 * A target has been selected, the PC Entity will perform a rotation tween to face the target.
	 */
	TARGETING() {
		@Override
		public void enter(Entity pc) {
			final StateMachine<Entity, PCState> stateMachine = Mappers.PC.get(pc).stateMachine;
			TweenCallback callback = new TweenCallback() {
				@Override
				public void onEvent(int i, BaseTween<?> baseTween) {
					stateMachine.changeState(ATTACK_PRE);
				}
			};

			Entity target = Mappers.TARGET.get(pc).target;
			Vector3 position = Mappers.POSITION.get(pc).position;
			Quaternion rotation = Mappers.ROTATION.get(pc).rotation;
			Vector3 destination = Mappers.POSITION.get(target).position;
			Quaternion targetRotation = Utility.facingRotation(position, destination);
			float speed = Utility.magnitude(rotation, targetRotation);
			// @TODO have speed divisor depend on some entity attribute
			Tweens.rotateTo(
				rotation,
				targetRotation,
				speed / 4,
				Linear.INOUT,
				callback
			).start(Manager.getInstance().tweenManager());
		}
	},
	ATTACK_PRE() {
		@Override
		public void enter(Entity pc) {
			final StateMachine<Entity, PCState> stateMachine = Mappers.PC.get(pc).stateMachine;
			Mappers.MODEL_INSTANCE.get(pc).controller.setAnimation(
				"skeleton|attack.pre",
				new AnimationController.AnimationListener() {
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						stateMachine.changeState(ATTACK);
					}

					@Override
					public void onLoop(AnimationController.AnimationDesc animation) {
					}
				});
		}
	},
	ATTACK() {
		/**
		 * @TODO move the blast effect handling to its own state machine.
		 * @param pc
		 */
		@Override
		public void enter(final Entity pc) {
			final StateMachine<Entity, PCState> stateMachine = Mappers.PC.get(pc).stateMachine;
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(pc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");

			// set the 'blast' effect's position to the model's attach.projectile node
			Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
			blast.position = attachmentMatrix.getTranslation(blast.position);
			// turn on the blast emitter
			blast.emitter.setEmissionMode(RegularEmitter.EmissionMode.Enabled);

			modelInstanceComponent.controller.setAnimation(
				"skeleton|attack",
				new AnimationController.AnimationListener() {
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						stateMachine.changeState(ATTACK_POST);
					}
					@Override
					public void onLoop(AnimationController.AnimationDesc animation) {}
				});
		}

		/**
		 * On update, the blast's position needs to be updated.
		 * @param pc
		 * @TODO move the blast effect's states tp their own machine.
		 */
		@Override
		public void update(Entity pc) {
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(pc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");
			// create a copy of the character's modelInstance.matrix and multiply it by the attachment node's globalTransform matrix.
			Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
			blast.position = attachmentMatrix.getTranslation(blast.position);
		}
	},
	/**
	 * Handles the last animation in the attack chain.
	 */
	ATTACK_POST() {
		@Override
		public void enter(final Entity pc) {
			final StateMachine<Entity, PCState> stateMachine = Mappers.PC.get(pc).stateMachine;
			final Vector3 position = Mappers.POSITION.get(pc).position;
			final Entity target = Mappers.TARGET.get(pc).target;
			final ModelInstanceComponent targetInstanceComponent = Mappers.MODEL_INSTANCE.get(target);
			final Vector3 targetPosition = targetInstanceComponent.instance.transform.cpy().mul(targetInstanceComponent.instance.getNode("impact.main").globalTransform).getTranslation(new Vector3());
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");

			// set the animation to skeleton|attack.post
			Mappers.MODEL_INSTANCE.get(pc).controller.setAnimation(
				"skeleton|attack.post",
				new AnimationController.AnimationListener() {
					/**
					 * Shoot the blast at the target.
					 * @param animation
					 * @TODO make onEnd trigger an update to the blast state machine so that the tween is built and started there.
					 */
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						pc.remove(TargetComponent.class);
						Tweens.translateTo(
							blast.position,
							targetPosition,
							position.dst(targetPosition.x, blast.position.y, targetPosition.z) / 24,
							Quad.OUT,
							// the callback sets the stateMachine back to REST, turns off the emitter,
							// and dispatches the ADVANCE_TURN_CONTROL message.
							new TweenCallback() {
								@Override
								public void onEvent(int i, BaseTween<?> baseTween) {
									stateMachine.changeState(REST);
									blast.emitter.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
									MessageManager.getInstance().dispatchMessage(0, Messages.ADVANCE_TURN_CONTROL);
								}
							}
						).start(Manager.getInstance().tweenManager());
					}

					@Override
					public void onLoop(AnimationController.AnimationDesc animation) {
					}
				});
		}
		@Override
		public void update(Entity pc) {
			// projectile has been launched if there's no target, so the Tween will take over.
			if (Mappers.TARGET.has(pc)) {
				final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(pc);
				final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");

				Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
				blast.position = attachmentMatrix.getTranslation(blast.position);
			}
		}
	},
	GLOBAL() {
	};

	@Override
	public void enter(Entity pc) {
	}

	@Override
	public void update(Entity pc) {
	}

	@Override
	public void exit(Entity pc) {
	}

	@Override
	public boolean onMessage(Entity pc, Telegram telegram) {
		return false;
	}
}
