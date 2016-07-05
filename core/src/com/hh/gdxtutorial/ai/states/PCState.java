package com.hh.gdxtutorial.ai.states;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.EffectsComponent;
import com.hh.gdxtutorial.entity.components.Mappers;
import com.hh.gdxtutorial.entity.components.ModelInstanceComponent;
import com.hh.gdxtutorial.entity.components.TargetComponent;
import com.hh.gdxtutorial.entity.systems.ModelBatchRenderer;
import com.hh.gdxtutorial.entity.systems.TurnSystem;
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
	EVALUATE() {
		/**
		 * Register's the player character stateMachine as a listener to mouse/touch input.
		 * @param pc
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
					int targetIndex = testTargets(pc, new Array<Entity>(actors.toArray()), screenCoordinates.x, screenCoordinates.z);

					if (targetIndex < 0) {
						Ray ray = Manager.getInstance().engine().getSystem(ModelBatchRenderer.class).camera.getPickRay(screenCoordinates.x, screenCoordinates.z);
						Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
						Vector3 intersection = new Vector3();
						Intersector.intersectRayPlane(ray, xzPlane, intersection);
						Vector3 position = Mappers.POSITION.get(pc).position;
						Quaternion rotation = Mappers.ROTATION.get(pc).rotation;
						Quaternion targetRotation = Utility.facingRotation(position, intersection);
						float speed = Utility.magnitude(rotation, targetRotation);
						Tween rotate = Tweens.rotateToTween(rotation, targetRotation, speed / 4, Quad.INOUT, null);
						final Entity fpc = pc;
						TweenCallback callback = new TweenCallback() {
							@Override
							public void onEvent(int type, BaseTween<?> source) {
								switch (type) {
									case COMPLETE:
										// Maybe add system that manages entity position and rotation, move that code from the render system.
//										Manager.getInstance().engine().getSystem(TurnSystem.class).getValidTargets(fpc);
										break;
									default:
										break;
								}
							}
						};
						Tween translate = Tweens.translateToTween(position, intersection, position.dst(intersection) / 16, Quad.INOUT, null);
						Timeline.createSequence().push(rotate).push(translate).setCallback(callback).start(Manager.getInstance().tweenManager());
					}
					System.out.println(targetIndex);
					break;
				default:
					break;
			}
			return false;
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

	/**
	 * Tests if a ray from screen coordinates intersects any actors in an array of actors (Entities).
	 * @param screenX
	 * @param screenY
	 * @return
	 */
	private static int testTargets(Entity pc, Array<Entity> actors, float screenX, float screenY) {
		Ray ray = Manager.getInstance().engine().getSystem(ModelBatchRenderer.class).camera.getPickRay(screenX, screenY);
		int result = -1;
		float distance = -1;

		for (int i = 0; i < actors.size; i++) {
			if (actors.get(i).equals(pc)) continue;
			final ModelInstanceComponent mic = Mappers.MODEL_INSTANCE.get(actors.get(i));
			Vector3 position = mic.instance.transform.getTranslation(new Vector3());
			position.add(mic.center);

			final float len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);

			if (len < 0f) continue;

			float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);

			if (distance >= 0f && dist2 > distance) continue;

			// *2 seems more accurate than he squaring.
			// if (dist2 <= mic.radius * mic.radius) {
			if (dist2 <= mic.radius * 2) {
				result = i;
				distance = dist2;
			}
		}
		return result;
	}

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
