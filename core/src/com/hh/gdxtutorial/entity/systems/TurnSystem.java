package com.hh.gdxtutorial.entity.systems;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
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
	private boolean processingActive = false;
	public int turnCount = 0;
	/**
	 * Getter for activeIndex.
	 * @return
	 */
	public int activeIndex() {
		return activeIndex;
	}
	// this gets the right end quat, most of the time. sometimes it's reversed.
	public Quaternion setTargetRotation(Vector3 origin, Vector3 target) {
		Vector3 diff = target.cpy().sub(origin);
		diff.nor();
		// why is it z?
		Vector3 zaxis = new Vector3(0,0,1);
		float dot = zaxis.dot(diff);
		float angle = (float) Math.acos(dot);
		Vector3 axis = zaxis.cpy().crs(diff).nor();
//		rotation.setFromCross(Vector3.Z, diff);
		return new Quaternion().setFromAxisRad(axis, angle);
	}
	/**
	 * Sets up and starts a tween from Vector3 position to Vector3 destination for float duration.
	 * @param sp     Starting Vector3 for tween
	 * @param ep  Ending Vector3 for tween
	 * @param duration     Duration of tween.
	 * @TODO Move this to a Tween Library. Tweens.Vector3.Position(start, end, duration)
	 */
	private void moveTo(Vector3 sp, Vector3 ep, Quaternion sr, Quaternion er, float duration) {
//		lookAt(ep.cpy().sub(sp).nor(), ep, sr);
		// sets g to hold the rotation to the target.
//		Quaternion g = sr.cpy();
		Quaternion targetRotation = setTargetRotation(sp, ep);
		Tween rotation = SlerpTween.to(sr, QuaternionAccessor.ROTATION, duration).target(targetRotation.x, targetRotation.y, targetRotation.z, targetRotation.w).ease(Linear.INOUT);
//		Tween rotation = Tween.to(sr, QuaternionAccessor.ROTATION, duration / 4).target(er.x, er.y, er.z, er.w).ease(Linear.INOUT);
		Tween translation = Tween.to(sp, Vector3Accessor.XYZ, duration).target(ep.x, ep.y, ep.z).ease(Linear.INOUT);
		Timeline.createSequence().push(rotation)/*.push(translation)*/.setCallback(new TweenCallback() {
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
		}).start(tweenManager);
	}

	public void lookAt(Vector3 origin, Vector3 target, Quaternion rotation) {
		Vector3 up = new Vector3(0, 1, 0);
		origin = origin.cpy().nor();
		target = target.cpy().nor();
		float dot = origin.dot(target);
		if (Math.abs(dot + 1) < 0.000000001f) {
			rotation.set(up.scl(-1), 180);
			return;
		}
		if (Math.abs(dot - 1) < 0.000000001f) {
			rotation.set(up, 180);
			return;
		}

		float rotAngle = (float) Math.acos(dot);
		Vector3 rotAxis = new Vector3(origin).crs(target).nor();

		rotation.setFromAxisRad(rotAxis, rotAngle);
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
		processingActive = false;
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
				.one(AiComponent.class, PlayerComponent.class)
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
	 * Checks if a entity is taking its turn (processingActive) and, if not, starts the turn
	 * of the next entity.
	 * @TODO Move MOB and player specific stuff out of here. Have a component handle it. AI should be pulled in
	 * from somewhere.
	 * @param deltaTime
	 */
	@Override
	public void update (float deltaTime) {
		tweenManager.update(deltaTime);

		if (!processingActive) {
			processingActive = true;
			// MOB
			if (Mappers.AI.get(sortedActors.get(activeIndex)) != null) {
				Vector3 st = Mappers.POSITION.get(sortedActors.get(activeIndex)).position();
				Quaternion sr = Mappers.ROTATION.get(sortedActors.get(activeIndex)).rotation();
				Vector3 et = new Vector3(MathUtils.random(-20, 20), 0, MathUtils.random(-20, 20));
				Quaternion er = getRotationTo(st, et);
				sr.set(er.nor());
				moveTo(st, et, sr, er, st.dst(et) / 16);
			// player
			} else if (Mappers.PLAYER.get(sortedActors.get(activeIndex)) != null) {
				MessageManager.getInstance().addListener(this, Messages.INTERACT_TOUCH);
			}
		}
	}

	private Quaternion getRotationTo(Vector3 origin, Vector3 target) {
		Quaternion q = new Quaternion();
		origin = origin.cpy().nor();
		target = target.cpy().nor();

		float dot = origin.dot(target);

		Vector3 tmpvec3 = new Vector3();
		Vector3 xUnitVec3 = new Vector3(1,0,0);
		Vector3 yUnitVec3 = new Vector3(0,1,0);
		if (dot < -0.999999) {
			tmpvec3 = xUnitVec3.cpy().crs(origin);

			if (tmpvec3.len() < 0.000001)
				tmpvec3 = yUnitVec3.cpy().crs(origin);

			tmpvec3.nor();
			q.setFromAxisRad(tmpvec3, MathUtils.PI);
		} else if (dot > 0.999999) {
			q.x = 0;
			q.y = 0;
			q.z = 0;
			q.w = 1;
		} else {
			tmpvec3 = origin.cpy().crs(target);
			q.x = tmpvec3.x;
			q.y = tmpvec3.y;
			q.z = tmpvec3.z;
			q.w = 1 + dot;
			q.nor();
		}
		return q;
//		if (d >= 1.0f) return q;
//
//		if (d < (1e-6f - 1.0f)) {
//			Vector3 axis = new Vector3(1,0,0).crs(origin);
//			if (axis.isCollinear(target)) // pick another if colinear
//				axis = new Vector3(0,1,0).crs(origin);
//			axis.nor();
//			q.setFromAxisRad(axis, MathUtils.PI);
//		} else {
//			float s = (float) Math.sqrt((1 + d) * 2);
//			float invs = 1 / s;
//			Vector3 c = origin.cpy().crs(target);
//
//			q.x = c.x * invs;
//			q.y = c.y * invs;
//			q.z = c.z * invs;
//			q.w = s * 0.5f;
//			q.nor();
//		}
//		return q;
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
				Vector3 st = Mappers.POSITION.get(sortedActors.get(activeIndex)).position();
				Quaternion sr = Mappers.ROTATION.get(sortedActors.get(activeIndex)).rotation();
				Vector3 et = (Vector3) msg.extraInfo;
				et.y = 0;
				Quaternion er = getRotationTo(st, et);
				sr.set(er.nor());

				MessageManager.getInstance().removeListener(this, Messages.INTERACT_TOUCH);
				moveTo(st, et, sr, er, st.dst(et) / 16);
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
