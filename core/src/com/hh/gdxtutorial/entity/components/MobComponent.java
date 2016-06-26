package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.hh.gdxtutorial.ai.states.MobState;

/**
 * Created by nils on 6/5/16.
 */
public class MobComponent implements Component {
	public StateMachine<Entity, MobState> stateMachine;

	private MobComponent() {}

	public MobComponent(Entity mob) {
		stateMachine = new DefaultStateMachine<Entity, MobState>(mob, MobState.REST, MobState.GLOBAL);
	}
}
