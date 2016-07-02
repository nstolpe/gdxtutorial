package com.hh.gdxtutorial.ai.states;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.EffectsComponent;
import com.hh.gdxtutorial.entity.components.Mappers;
import com.hh.gdxtutorial.entity.components.ModelInstanceComponent;
import com.hh.gdxtutorial.entity.components.TargetComponent;
import com.hh.gdxtutorial.libraries.Utility;
import com.hh.gdxtutorial.libraries.tweenengine.Tweens;
import com.hh.gdxtutorial.singletons.Manager;

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
	ACTIVE() {
		@Override
		public void enter(Entity pc) {
			Vector3 targetPosition = new Vector3(MathUtils.random(-20, 20), 0, MathUtils.random(-20, 20));
		}
	},
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
			Tweens.rotateToTween(
				rotation,
				targetRotation,
				speed / 4,
				Quad.INOUT,
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
		@Override
		public void enter(final Entity pc) {
			final StateMachine<Entity, PCState> stateMachine = Mappers.PC.get(pc).stateMachine;
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(pc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");

			// set the 'blast' effect's position to the model's attach.projectile node
			Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
			blast.position = attachmentMatrix.getTranslation(blast.position);
			// turn on the blast emmitter
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

		@Override
		public void update(Entity pc) {
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(pc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(pc).getEffect("blast");

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
					 */
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						pc.remove(TargetComponent.class);
						Tweens.translateToTween(
							blast.position,
							targetPosition,
							position.dst(targetPosition.x, blast.position.y, targetPosition.z) / 24,
							Quad.OUT,
							new TweenCallback() {
								@Override
								public void onEvent(int i, BaseTween<?> baseTween) {
									stateMachine.changeState(REST);
									MessageManager.getInstance().dispatchMessage(0, Messages.ADVANCE_TURN_CONTROL);
									blast.emitter.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
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
