package com.hh.gdxtutorial.entity.systems;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.singletons.Manager;
import com.hh.gdxtutorial.tweenengine.accessors.QuaternionAccessor;
import com.hh.gdxtutorial.tweenengine.accessors.SlerpTween;
import com.hh.gdxtutorial.tweenengine.accessors.Vector3Accessor;

import java.util.Comparator;

/**
 * Entity system that manages a turn-based portion of a game. A turn consists of a set of actions
 * performed by all Actors in the turn.
 */
public class TurnSystem extends EntitySystem implements Telegraph {
	private ImmutableArray<Entity> actors;
	private Array<Entity> sortedActors = new Array<Entity>();
	private int activeIndex;
	private TweenManager tweenManager = new TweenManager();
	private boolean inTurn = false;
	public int turnCount = 0;
	public float attentionRadius = 20.0f;

	private TweenCallback advanceTurnCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
				case COMPLETE:
					Mappers.EFFECTS.get(sortedActors.get(activeIndex)).getEffect("blast").emitter.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
					MessageManager.getInstance().dispatchMessage(0, Messages.ADVANCE_TURN_CONTROL);
					break;
				default:
					assert false;
					break;
			}
		}
	};
	private TweenCallback scanForTargetsCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
				case COMPLETE:
					getValidTargets(sortedActors.get(activeIndex));
					break;
				default:
					assert false;
					break;
			}
		}
	};

	/**
	 * Getter for activeIndex.
	 * @return
	 */
	public int activeIndex() {
		return activeIndex;
	}

	public void getValidTargets(final Entity actor) {
		final Vector3 position = Mappers.POSITION.get(actor).position;
		Quaternion rotation = Mappers.ROTATION.get(actor).rotation;
		Vector3 tmp;

		// create a map of distances to entitiyes
		ArrayMap<Float, Entity> validTargets = new ArrayMap<Float, Entity>();
		for (Entity a : sortedActors) {
			if (a != actor) {
				tmp = Mappers.POSITION.get(a).position;
				float distance = position.dst(tmp);
				if (distance <= attentionRadius) {
					validTargets.put(distance, a);
				}
			}
		}
		// make an array of the distance keys and sort it
		Array<Float> keys = validTargets.keys().toArray();
		keys.sort(new Comparator<Float>() {
			@Override
			public int compare(Float o1, Float o2) {
				return o1 > o2 ? 1 : o1 == o2 ? 0 : -1;
			}
		});

		if (validTargets.size == 0) {
			advanceTurnControl();
		} else {
			Mappers.MOB.get(actor);

			Mappers.MOB.get(actor).stateMachine.changeState(MobComponent.MobState.TARGETING);
			MessageManager.getInstance().dispatchMessage(this, Mappers.MOB.get(actor).stateMachine, Messages.TARGET_ACQUIRED, new Messages.TargetMessageData(actor, validTargets.firstValue()));
			// Attacks closest, first in map sorted by distance
			final Vector3 targetPosition = Mappers.POSITION.get(validTargets.firstValue()).position;

			Quaternion targetRotation = getTargetRotation(position, targetPosition, rotation);
			Quaternion qd = rotation.cpy().conjugate().mul(targetRotation);
			float angle = 2 * (float) Math.atan2(new Vector3(qd.x, qd.y, qd.z).len(), qd.w);

			// rotate to face the target.
			SlerpTween.to(rotation, QuaternionAccessor.ROTATION, angle / 4)
				.target(targetRotation.x, targetRotation.y, targetRotation.z, targetRotation.w)
				.ease(Linear.INOUT)
				.setCallback(new TweenCallback() {
					@Override
					public void onEvent(int i, BaseTween<?> baseTween) {

						final ModelInstanceComponent mic = Mappers.MODEL_INSTANCE.get(actor);

						mic.controller.animate("skeleton|attack.pre", new AnimationController.AnimationListener() {
							@Override
							public void onEnd(AnimationController.AnimationDesc animation) {
								System.out.println("attack.pre");
								ModelInstance inst = Mappers.MODEL_INSTANCE.get(actor).instance();
								EffectsComponent.Effect blast = Mappers.EFFECTS.get(actor).getEffect("blast");
								Matrix4 transform = inst.transform.cpy().mul(inst.getNode("attach.projectile").globalTransform);
								final Vector3 p = blast.position;
								blast.position = transform.getTranslation(blast.position);
								blast.emitter.setEmissionMode(RegularEmitter.EmissionMode.Enabled);

								Tween.to(p, Vector3Accessor.XYZ, position.dst(targetPosition.x, p.y, targetPosition.z) / 16)
										.target(targetPosition.x, p.y, targetPosition.z)
										.ease(Linear.INOUT)
										.setCallback(advanceTurnCallback)
										.start(Manager.getInstance().tweenManager());

								mic.controller.animate("skeleton|attack", 1, 1.0f, new AnimationController.AnimationListener() {
									@Override
									public void onEnd(AnimationController.AnimationDesc animation) {
										mic.controller.animate("skeleton|rest", -1, null, 1f);

									}

									@Override
									public void onLoop(AnimationController.AnimationDesc animation) {

									}
								}, 1.0f);
							}

							@Override
							public void onLoop(AnimationController.AnimationDesc animation) {

							}
						}, 1.0f);

					}
				});//.start(Manager.getInstance().tweenManager());
		}
	}
	public void startPlayerTurn(Entity actor) {
		MessageManager.getInstance().addListener(this, Messages.INTERACT_TOUCH);
	}
	public void startMobTurn(Entity actor) {
		Vector3 actorPosition = Mappers.POSITION.get(actor).position();
		Quaternion actorRotation = Mappers.ROTATION.get(actor).rotation();
		Vector3 targetPosition = new Vector3(MathUtils.random(-20, 20), 0, MathUtils.random(-20, 20));
		startTurnAction(actorPosition, actorRotation, targetPosition, scanForTargetsCallback);
	}
	/**
	 * Gets the rotation from one Vector3 to another.
	 * @param origin
	 * @param target
	 * @return
	 * @TODO Make sure those epsilon values are working.
	 */
	public Quaternion getTargetRotation(Vector3 origin, Vector3 target, Quaternion fallback) {
		// Get the difference between the two points.
		Vector3 difference = target.cpy().sub(origin);
		Vector3 direction = difference.cpy().nor();

		if (difference.len() < 0.000000001f) return new Quaternion(fallback);

		// z is forward
		Vector3 zAxis = new Vector3(0,0,1);

		float dot = zAxis.dot(direction);
		float angle = (float) Math.acos(dot);

		if (angle < 0.000000001f) return new Quaternion(fallback);

		return new Quaternion().setFromCross(zAxis, direction);
	}
	/**
	 * Sets up and starts a tween from Vector3 position to Vector3 destination for float duration.
	 * @param position     Starting Vector3 for tween
	 * @param rotation     Starting Quaternion for tween
	 * @param destination  Ending Vector3 for tween
	 * @TODO Move this to a Tween Library. Tweens.Vector3.Position(start, end, duration)
	 */
	private void startTurnAction(Vector3 position, Quaternion rotation, Vector3 destination, TweenCallback callback) {
		Quaternion targetRotation = getTargetRotation(position, destination, rotation);
		Quaternion qd = rotation.cpy().conjugate().mul(targetRotation);

		float angle = 2 * (float) Math.atan2(new Vector3(qd.x, qd.y, qd.z).len(), qd.w);

		Tween rotate = SlerpTween.to(rotation, QuaternionAccessor.ROTATION, angle / 4)
			.target(targetRotation.x, targetRotation.y, targetRotation.z, targetRotation.w)
			.ease(Linear.INOUT);

		Tween translate = Tween.to(position, Vector3Accessor.XYZ, position.dst(destination) / 16)
			.target(destination.x, destination.y, destination.z)
			.ease(Linear.INOUT);

		Timeline.createSequence().push(rotate).push(translate).setCallback(callback).start(tweenManager);
	}
	/**
	 * Passes control of the turn to the next actor in sortedActors
	 * If the activeIndex is the last entity, generate random values
	 * for the InitiativeComponent and resort sorted actors.
	 */
	public void advanceTurnControl() {
		// if last actor is taking its turn action
		// reset sortedActors
		if (activeIndex + 1 == sortedActors.size) {
			sortedActors.clear();
			for(int i = 0; i < actors.size(); i++) {
				Entity e = actors.get(i);
				// this random generation should be better. random + speed/agility bonus? also move to another system.
				Mappers.INITIATIVE.get(e).initiative(MathUtils.random(10));
				sortedActors.add(e);
			}
			sortedActors.sort(new InitiativeComparator());
			activeIndex = 0;
			turnCount++;
		} else {
			activeIndex++;
		}

		inTurn = false;
	}

	/**
	 * Gets actors from Family and builds/sorts sortedActors by InitiativeComponent.initiative
	 * Starts listening to ADVANCE_TURN_CONTROL and registers the Vector3Accessor
	 * @param engine
	 * @TODO move tween accessor registration out to a movement or transform system.
	 */
	@Override
	public void addedToEngine(Engine engine) {
		actors = engine.getEntitiesFor(Family
				.all(InitiativeComponent.class)
				.one(MobComponent.class, PlayerComponent.class)
				.get());

		if (actors.size() > 0) {
			for (int i = 0; i < actors.size(); i++) sortedActors.add(actors.get(i));
			sortedActors.sort(new InitiativeComparator());
		}

		MessageManager.getInstance().addListener(this, Messages.ADVANCE_TURN_CONTROL);
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
		Tween.registerAccessor(Quaternion.class, new QuaternionAccessor());

		activeIndex = 0;
	}
	/**
	 * Checks if a entity is taking its turn (inTurn) and, if not, starts the turn
	 * of the next entity.
	 * @TODO Move MOB and player specific stuff out of here. Have a component handle it. MOB should be pulled in
	 * from somewhere.
	 * @param deltaTime
	 */
	@Override
	public void update (float deltaTime) {
		tweenManager.update(deltaTime);
		// @TODO move Manager update to screen, maybe abstract.
		Manager.getInstance().update(deltaTime);

		if (!inTurn) {
			inTurn = true;
			Entity active = sortedActors.get(activeIndex);

			if (Mappers.PLAYER.get(active) != null)
				startPlayerTurn(active);
			else if (Mappers.MOB.get(active) != null)
				startMobTurn(active);

		}
	}
	/**
	 * From Telegraph. Will listen to advance a turn and receive touch input for a user controlled character.
	 * @param msg   extraInfo is used with INTERACT_TOUCH. It expects a Vector3 and casts to it.
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.ADVANCE_TURN_CONTROL:
				advanceTurnControl();
				break;
			case Messages.INTERACT_TOUCH:
				Vector3 actorPosition = Mappers.POSITION.get(sortedActors.get(activeIndex)).position();
				Quaternion actorRotation = Mappers.ROTATION.get(sortedActors.get(activeIndex)).rotation();
				// msg.extraInfo holds the x and z coords.
				// y is set to 0 since there's no height yet.
				Vector3 targetPosition = (Vector3) msg.extraInfo;
				targetPosition.y = 0;
				// remove the listener for INTERACT_TOUCH
				MessageManager.getInstance().removeListener(this, Messages.INTERACT_TOUCH);
				startTurnAction(actorPosition, actorRotation, targetPosition, advanceTurnCallback);
				break;
			default:
				return false;
		}
		return true;

	}
	/**
	 * Comparator for =, >, <
	 * Used to determine sequence of turn actions.
	 */
	private static class InitiativeComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity a, Entity b) {
			return Mappers.INITIATIVE.get(a).initiative() > Mappers.INITIATIVE.get(b).initiative() ? 1 : Mappers.INITIATIVE.get(a).initiative() == Mappers.INITIATIVE.get(b).initiative()  ? 0 : -1;
		}
	}
}
