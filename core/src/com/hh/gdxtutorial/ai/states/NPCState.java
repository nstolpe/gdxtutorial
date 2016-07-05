package com.hh.gdxtutorial.ai.states;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
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
import com.hh.gdxtutorial.entity.systems.TurnSystem;
import com.hh.gdxtutorial.libraries.Utility;
import com.hh.gdxtutorial.libraries.tweenengine.Tweens;
import com.hh.gdxtutorial.singletons.Manager;

/**
 * Created by nils on 6/26/16.
 */
public enum NPCState implements State<Entity> {
	// rest state, plays the floating animation
	REST() {
		@Override
		public void enter(Entity npc) {
			Mappers.MODEL_INSTANCE.get(npc).controller.animate("skeleton|rest", -1, null, 1);
		}
	},
	EVALUATE() {
		@Override
		public void enter(Entity npc) {
			Vector3 position = Mappers.POSITION.get(npc).position();
			Quaternion rotation = Mappers.ROTATION.get(npc).rotation();
			Vector3 destination = new Vector3(MathUtils.random(-20, 20), 0, MathUtils.random(-20, 20));
			Quaternion targetRotation = Utility.facingRotation(position, destination);
			float speed = Utility.magnitude(rotation, targetRotation);
			Tween rotate = Tweens.rotateToTween(rotation, targetRotation, speed / 4, Quad.INOUT, null);
			final Entity fnpc = npc;
			TweenCallback callback = new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					switch (type) {
						case COMPLETE:
							// @TODO go over this again. Bouncing back to the TurnSystem seems kind of weird
							// but kind of ok.
							Manager.getInstance().engine().getSystem(TurnSystem.class).getValidTargets(fnpc);
							break;
						default:
							break;
					}
				}
			};
			Tween translate = Tweens.translateToTween(position, destination, position.dst(destination) / 16, Quad.INOUT, null);
			Timeline.createSequence().push(rotate).push(translate).setCallback(callback).start(Manager.getInstance().tweenManager());
		}
	},
	TARGETING() {
		@Override
		public void enter(Entity npc) {
			final StateMachine<Entity, NPCState> stateMachine = Mappers.NPC.get(npc).stateMachine;
			TweenCallback callback = new TweenCallback() {
				@Override
				public void onEvent(int i, BaseTween<?> baseTween) {
					stateMachine.changeState(ATTACK_PRE);
				}
			};

			Entity target = Mappers.TARGET.get(npc).target;
			Vector3 position = Mappers.POSITION.get(npc).position;
			Quaternion rotation = Mappers.ROTATION.get(npc).rotation;
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
		public void enter(Entity npc) {
			final StateMachine<Entity, NPCState> stateMachine = Mappers.NPC.get(npc).stateMachine;
			Mappers.MODEL_INSTANCE.get(npc).controller.setAnimation(
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
		public void enter(final Entity npc) {
			final StateMachine<Entity, NPCState> stateMachine = Mappers.NPC.get(npc).stateMachine;
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(npc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(npc).getEffect("blast");

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
		public void update(Entity npc) {
			final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(npc);
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(npc).getEffect("blast");

			Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
			blast.position = attachmentMatrix.getTranslation(blast.position);
		}
	},
	/**
	 * Handles the last animation in the attack chain.
	 */
	ATTACK_POST() {
		@Override
		public void enter(final Entity npc) {
			final StateMachine<Entity, NPCState> stateMachine = Mappers.NPC.get(npc).stateMachine;
			final Vector3 position = Mappers.POSITION.get(npc).position;
			final Entity target = Mappers.TARGET.get(npc).target;
			final ModelInstanceComponent targetInstanceComponent = Mappers.MODEL_INSTANCE.get(target);
			final Vector3 targetPosition = targetInstanceComponent.instance.transform.cpy().mul(targetInstanceComponent.instance.getNode("impact.main").globalTransform).getTranslation(new Vector3());
			final EffectsComponent.Effect blast = Mappers.EFFECTS.get(npc).getEffect("blast");

			// set the animation to skeleton|attack.post
			Mappers.MODEL_INSTANCE.get(npc).controller.setAnimation(
				"skeleton|attack.post",
				new AnimationController.AnimationListener() {
					/**
					 * Shoot the blast at the target.
					 * @param animation
					 */
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						npc.remove(TargetComponent.class);
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
		public void update(Entity npc) {
			// projectile has been launched if there's no target, so the Tween will take over.
			if (Mappers.TARGET.has(npc)) {
				final ModelInstanceComponent modelInstanceComponent = Mappers.MODEL_INSTANCE.get(npc);
				final EffectsComponent.Effect blast = Mappers.EFFECTS.get(npc).getEffect("blast");

				Matrix4 attachmentMatrix = modelInstanceComponent.instance.transform.cpy().mul(modelInstanceComponent.instance.getNode("attach.projectile").globalTransform);
				blast.position = attachmentMatrix.getTranslation(blast.position);
			}
		}
	},
	GLOBAL() {
	};

	@Override
	public void enter(Entity npc) {
	}

	@Override
	public void update(Entity npc) {
	}

	@Override
	public void exit(Entity npc) {
	}

	@Override
	public boolean onMessage(Entity npc, Telegram telegram) {
		return false;
	}
}