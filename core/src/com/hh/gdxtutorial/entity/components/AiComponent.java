package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;

/**
 * Created by nils on 6/5/16.
 */
public class AiComponent implements Component {
	public StateMachine<AiComponent, MobState> stateMachine;

	public AiComponent () {
		stateMachine = new DefaultStateMachine<AiComponent, MobState>(this, MobState.REST);
	}

	public static enum MobState implements State<AiComponent> {
		REST() {
			@Override
			public void enter(AiComponent entity) {

			}

			@Override
			public void update(AiComponent entity) {

			}

			@Override
			public void exit(AiComponent entity) {

			}

			@Override
			public boolean onMessage(AiComponent entity, Telegram telegram) {
				return false;
			}
		},
		ATTACK() {
			@Override
			public void enter(Entity entity) {

			}

			@Override
			public void update(Entity entity) {

			}

			@Override
			public void exit(Entity entity) {

			}

			@Override
			public boolean onMessage(Entity entity, Telegram telegram) {
				return false;
			}
		},
		ATTACK_PRE() {
			@Override
			public void enter(Entity entity) {

			}

			@Override
			public void update(Entity entity) {

			}

			@Override
			public void exit(Entity entity) {

			}

			@Override
			public boolean onMessage(Entity entity, Telegram telegram) {
				return false;
			}
		},
		ATTACK_POST() {
			@Override
			public void enter(Entity entity) {

			}

			@Override
			public void update(Entity entity) {

			}

			@Override
			public void exit(Entity entity) {

			}

			@Override
			public boolean onMessage(Entity entity, Telegram telegram) {
				return false;
			}
		}
	}
}
