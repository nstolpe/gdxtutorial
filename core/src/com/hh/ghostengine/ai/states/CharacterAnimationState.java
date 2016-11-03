package com.hh.ghostengine.ai.states;

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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.hh.ghostengine.ai.Messages;
import com.hh.ghostengine.entity.components.EffectsComponent;
import com.hh.ghostengine.entity.components.Mappers;
import com.hh.ghostengine.entity.components.ModelInstanceComponent;
import com.hh.ghostengine.entity.components.TargetComponent;
import com.hh.ghostengine.libraries.tweenengine.Tweens;
import com.hh.ghostengine.globals.Manager;

/**
 * Created by nils on 10/8/16.
 */
public enum CharacterAnimationState implements State<Entity> {
	REST() {
		@Override
		public void enter(Entity c) {
			Mappers.MODEL_INSTANCE.get(c).controller.animate("skeleton|rest", -1, null, 1);
		}
	},
	ATTACK_PRE() {
		@Override
		public void enter(Entity c) {
			final StateMachine<Entity, CharacterAnimationState> stateMachine = Mappers.CHARACTER_ANIMATION_STATE.get(c).stateMachine;
			Mappers.MODEL_INSTANCE.get(c).controller.setAnimation(
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
		public void enter(final Entity c) {
			final StateMachine<Entity, CharacterAnimationState> stateMachine = Mappers.CHARACTER_ANIMATION_STATE.get(c).stateMachine;
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(c);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(c).getEffect("blast");

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
		 * @param c
		 */
		@Override
		public void update(Entity c) {
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(c);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(c).getEffect("blast");
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
		public void enter(final Entity c) {
			final StateMachine<Entity, CharacterAnimationState> stateMachine = Mappers.CHARACTER_ANIMATION_STATE.get(c).stateMachine;
			final Vector3 position = Mappers.POSITION.get(c).position;
			final Entity target = Mappers.TARGET.get(c).target;
			final ModelInstanceComponent targetInstanceComponent = Mappers.MODEL_INSTANCE.get(target);
			final Vector3 targetPosition = targetInstanceComponent.instance.transform.cpy().mul(targetInstanceComponent.instance.getNode("impact.main").globalTransform).getTranslation(new Vector3());
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(c).getEffect("blast");
			// set the animation to skeleton|attack.post
			Mappers.MODEL_INSTANCE.get(c).controller.setAnimation(
					"skeleton|attack.post",
					new AnimationController.AnimationListener() {
						/**
						 * Shoot the blast at the target.
						 * @param animation
						 */
						@Override
						public void onEnd(AnimationController.AnimationDesc animation) {
							c.remove(TargetComponent.class);
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
		public void update(Entity c) {
			// projectile has been launched if there's no target, so the Tween will take over.
			if (Mappers.TARGET.has(c)) {
				final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(c);
				final EffectsComponent.Effect blast = Mappers.EFFECTS.get(c).getEffect("blast");

				Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
				blast.position = attachmentMatrix.getTranslation(blast.position);
			}
		}
	},
	GLOBAL() {
	};
	@Override
	public void enter(Entity c) {
	}

	@Override
	public void update(Entity c) {
	}

	@Override
	public void exit(Entity c) {
	}

	@Override
	public boolean onMessage(Entity c, Telegram telegram) {
		return false;
	}
}
