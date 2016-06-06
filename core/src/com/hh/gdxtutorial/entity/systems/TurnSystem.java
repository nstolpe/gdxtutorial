package com.hh.gdxtutorial.entity.systems;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.tween.accessors.Vector3Accessor;

import java.util.Comparator;

/**
 * Created by nils on 6/5/16.
 */
public class TurnSystem extends SortedIteratingSystem implements Telegraph {
	private Entity active;
	private TweenManager tweenManager = new TweenManager();
	private boolean processingActive;
	public int turnCount;


	public TurnSystem() {
		super(Family
				.all(InitiativeComponent.class)
				.one(AiComponent.class, PlayerComponent.class)
				.get(),
				new InitiativeComparator());
		MessageManager.getInstance().addListener(this, Messages.ADVANCE_TURN_CONTROL);
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
	}

	public int activeIndex() {
		return getEntities().indexOf(active, true);
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
		ImmutableArray<Entity> entities = getEntities();
		int activeIndex = entities.indexOf(active, true);

		if (activeIndex + 1 == entities.size()) {
			for (Entity e : entities) Mappers.INITIATIVE.get(e).initiative(MathUtils.random(10));
//			forceSort();
			active = entities.get(0);
			turnCount++;
		} else {
			active = entities.get(activeIndex + 1);
		}
		processingActive = false;
	}
	@Override
	public void update (float deltaTime) {
		super.update(deltaTime);
		tweenManager.update(deltaTime);
	}
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		// first turn, first actor
		if (active == null) active = entity;
		if (!processingActive) {
			System.out.println(Mappers.INITIATIVE.get(active).initiative());

			processingActive = true;
			if (Mappers.AI.get(active) != null) {
				Vector3 position = Mappers.POSITION.get(active).position();
				Vector3 destination = new Vector3(MathUtils.random(-20, 20), 2, MathUtils.random(-20, 20));

				moveTo(destination, position, position.dst(destination) / 8);
			} else if (Mappers.PLAYER.get(active) != null) {
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
				Vector3 position = Mappers.POSITION.get(active).position();

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
