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
 * Created by nils on 6/5/16.
 */
public class TurnSystem extends EntitySystem implements Telegraph {
	private ImmutableArray<Entity> entities;
	private Array<Entity> sortedEntities = new Array<Entity>();
	private int activeIndex;
	private TweenManager tweenManager = new TweenManager();
	private boolean processingActive = false;
	public int turnCount = 0;


	@Override
	public void addedToEngine(Engine engine) {
		entities = engine.getEntitiesFor(Family
						.all(InitiativeComponent.class)
						.one(AiComponent.class, PlayerComponent.class)
						.get());

		if (entities.size() > 0) {
			for (int i = 0; i < entities.size(); i++) sortedEntities.add(entities.get(i));

			sortedEntities.sort(new InitiativeComparator());
		}

		MessageManager.getInstance().addListener(this, Messages.ADVANCE_TURN_CONTROL);
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());

		activeIndex = 0;
	}

	public int activeIndex() {
		return activeIndex;
	}

	private void moveTo(Vector3 destination, Vector3 position, float duration) {
		Tween.to(position, Vector3Accessor.XYZ, duration)
			.target(destination.x, destination.y, destination.z)
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
	public void advanceTurnControl() {
		if (activeIndex + 1 == sortedEntities.size) {
			sortedEntities.clear();
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				Mappers.INITIATIVE.get(e).initiative(MathUtils.random(10));
				sortedEntities.add(e);
			}
			sortedEntities.sort(new InitiativeComparator());
			activeIndex = 0;
			turnCount++;
		} else {
			activeIndex++;
		}
		processingActive = false;
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
			if (Mappers.AI.get(sortedEntities.get(activeIndex)) != null) {
				Vector3 position = Mappers.POSITION.get(sortedEntities.get(activeIndex)).position();
				Vector3 destination = new Vector3(MathUtils.random(-20, 20), 2, MathUtils.random(-20, 20));

				moveTo(destination, position, position.dst(destination) / 8);
			// player
			} else if (Mappers.PLAYER.get(sortedEntities.get(activeIndex)) != null) {
				MessageManager.getInstance().addListener(this, Messages.INTERACT_TOUCH);
			}
		}
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.ADVANCE_TURN_CONTROL:
				advanceTurnControl();
				break;
			case Messages.INTERACT_TOUCH:
				Vector3 destination = (Vector3) msg.extraInfo;
				Vector3 position = Mappers.POSITION.get(sortedEntities.get(activeIndex)).position();

				destination.y = 2;

				MessageManager.getInstance().removeListener(this, Messages.INTERACT_TOUCH);
				moveTo(destination, position, position.dst(destination) / 8);
				break;
			default:
				return false;
		}
		return true;

	}

	private static class InitiativeComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity a, Entity b) {
			return Mappers.INITIATIVE.get(a).initiative() > Mappers.INITIATIVE.get(b).initiative() ? 1 : Mappers.INITIATIVE.get(a).initiative() == Mappers.INITIATIVE.get(b).initiative()  ? 0 : -1;
		}
	}
}
