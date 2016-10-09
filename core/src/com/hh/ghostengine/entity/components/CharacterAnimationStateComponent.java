package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.hh.ghostengine.ai.states.CharacterAnimationState;
import com.hh.ghostengine.ai.states.PCState;

/**
 * Created by nils on 10/8/16.
 */
public class CharacterAnimationStateComponent implements Component {
	public StateMachine<Entity, CharacterAnimationState> stateMachine;

	private CharacterAnimationStateComponent() {}

	public CharacterAnimationStateComponent(Entity c) {
		stateMachine = new DefaultStateMachine<Entity, CharacterAnimationState>(c, CharacterAnimationState.REST, CharacterAnimationState.GLOBAL);
	}
}