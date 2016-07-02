package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.hh.gdxtutorial.ai.states.NPCState;

/**
 * Created by nils on 6/5/16.
 */
public class NPCComponent implements Component {
	public StateMachine<Entity, NPCState> stateMachine;

	private NPCComponent() {}

	public NPCComponent(Entity npc) {
		stateMachine = new DefaultStateMachine<Entity, NPCState>(npc, NPCState.REST, NPCState.GLOBAL);
	}
}
