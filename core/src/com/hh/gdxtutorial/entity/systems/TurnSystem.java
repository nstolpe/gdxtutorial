package com.hh.gdxtutorial.entity.systems;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.tween.accessors.Vector3Accessor;

import java.util.Comparator;

/**
 * Entity system that manages a turn-based portion of a game.
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
	/**
	 * Sets up and starts a tween from Vector3 position to Vector3 destination for float duration.
	 * @param start     Starting Vector3 for tween
	 * @param end  Ending Vector3 for tween
	 * @param duration     Duration of tween.
	 * @TODO Move this to a Tween Library. Tweens.Vector3.Position(start, end, duration)
	 */
	private void moveTo(Vector3 start, Vector3 end, float duration) {
		Tween.to(start, Vector3Accessor.XYZ, duration)
			.target(end.x, end.y, end.z)
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
				// this random generation should be better. random + speed/agility bonus
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
				Vector3 position = Mappers.POSITION.get(sortedActors.get(activeIndex)).position();
				Vector3 destination = new Vector3(MathUtils.random(-20, 20), 2, MathUtils.random(-20, 20));

				moveTo(position, destination, position.dst(destination) / 8);
			// player
			} else if (Mappers.PLAYER.get(sortedActors.get(activeIndex)) != null) {
				MessageManager.getInstance().addListener(this, Messages.INTERACT_TOUCH);
			}
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
				Vector3 destination = (Vector3) msg.extraInfo;
				Vector3 position = Mappers.POSITION.get(sortedActors.get(activeIndex)).position();

				destination.y = 2;

				MessageManager.getInstance().removeListener(this, Messages.INTERACT_TOUCH);
				moveTo(position, destination, position.dst(destination) / 8);
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
