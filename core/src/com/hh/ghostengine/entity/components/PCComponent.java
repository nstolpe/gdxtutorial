package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.hh.ghostengine.ai.states.PCState;

/**
 * An empty component that indicates an Entity is a player.
 * Could provide something else later.S
 */
public class PCComponent implements Component {
	public StateMachine<Entity, PCState> stateMachine;

	private PCComponent() {}

	public PCComponent(Entity pc) {
		stateMachine = new DefaultStateMachine<Entity, PCState>(pc, PCState.REST, PCState.GLOBAL);
	}
}
