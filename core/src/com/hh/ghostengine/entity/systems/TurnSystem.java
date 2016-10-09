package com.hh.ghostengine.entity.systems;

import aurelienribon.tweenengine.*;
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
import com.badlogic.gdx.utils.ArrayMap;
import com.hh.ghostengine.ai.Messages;
import com.hh.ghostengine.ai.states.NPCState;
import com.hh.ghostengine.ai.states.PCState;
import com.hh.ghostengine.entity.components.*;
import com.hh.ghostengine.libraries.tweenengine.Tweens;
import com.hh.ghostengine.libraries.tweenengine.Callbacks;
import com.hh.ghostengine.libraries.tweenengine.accessors.QuaternionAccessor;
import com.hh.ghostengine.libraries.tweenengine.accessors.Vector3Accessor;

import java.util.Comparator;

/**
 * Entity system that manages a turn-based portion of a game. A turn consists of a set of actions
 * performed by all Actors in the turn.
 */
public class TurnSystem extends EntitySystem implements Telegraph {
	private ImmutableArray<Entity> actors;
	private Array<Entity> sortedActors = new Array<Entity>();
	private int activeIndex;
	private boolean inTurn = false;
	public int turnCount = 0;
	public float attentionRadius = 20.0f;

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
				.one(NPCComponent.class, PCComponent.class)
				.get());

		if (actors.size() > 0) {
			for (int i = 0; i < actors.size(); i++) sortedActors.add(actors.get(i));
			sortedActors.sort(new InitiativeComponentComparator());
		}

		MessageManager.getInstance().addListener(this, Messages.ADVANCE_TURN_CONTROL);
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
		Tween.registerAccessor(Quaternion.class, new QuaternionAccessor());

		activeIndex = 0;
	}
	/**
	 * Checks if a entity is taking its turn (inTurn) and, if not, starts the turn
	 * of the next entity.
	 * @TODO Move NPC and player specific stuff out of here. Have a component handle it. NPC should be pulled in
	 * from somewhere.
	 * @param deltaTime
	 */
	@Override
	public void update (float deltaTime) {
		// update the state machine of each actor
		for (Entity actor : actors) {
			if (Mappers.NPC.has(actor)) Mappers.NPC.get(actor).stateMachine.update();
			if (Mappers.PC.has(actor)) Mappers.PC.get(actor).stateMachine.update();
		}
		// if a turn has just ended,  update the active
		// actor and start a new one.
		if (!inTurn) startNextTurn();
	}
	/**
	 * From Telegraph. Will listen to advance a turn and receive touch input for a user controlled character.
	 * @param msg   extraInfo is used with TOUCH_CLICK_INPUT. It expects a Vector3 and casts to it.
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.ADVANCE_TURN_CONTROL:
				advanceTurnControl();
				break;
			default:
				return false;
		}
		return true;
	}
	public void startNextTurn() {
		inTurn = true;
		Entity active = sortedActors.get(activeIndex);

		if (Mappers.PC.get(active) != null)
			Mappers.PC.get(active).stateMachine.changeState(PCState.WAIT);
		else if (Mappers.NPC.get(active) != null)
			Mappers.NPC.get(active).stateMachine.changeState(NPCState.EVALUATE);
	}
	/**
	 * Getter for activeIndex.
	 * @return
	 */
	public int activeIndex() {
		return activeIndex;
	}
	public ImmutableArray<Entity> actors() {
		return this.actors;
	}
	public void getValidTargets(final Entity actor) {
		final Vector3 position = Mappers.POSITION.get(actor).position;
		Vector3 tmp;

		// create a map of distances to entities
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
			actor.add(new TargetComponent(validTargets.get(keys.first())));
			Mappers.NPC.get(actor).stateMachine.changeState(NPCState.TARGETING);
		}
	}

	/**
	 * Passes control of the turn to the next actor in sortedActors
	 * If the activeIndex is the last entity, generate random values
	 * for the InitiativeComponent and re-sort sorted actors.
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
			sortedActors.sort(new InitiativeComponentComparator());
			activeIndex = 0;
			turnCount++;
		} else {
			activeIndex++;
		}

		inTurn = false;
	}
	/**
	 * Comparator for =, >, <
	 * Used to determine sequence of turn actions.
	 */
	private static class InitiativeComponentComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity a, Entity b) {
			return Mappers.INITIATIVE.get(a).initiative() > Mappers.INITIATIVE.get(b).initiative() ? 1 : Mappers.INITIATIVE.get(a).initiative() == Mappers.INITIATIVE.get(b).initiative()  ? 0 : -1;
		}
	}
}