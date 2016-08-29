package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by nils on 6/5/16.
 */
public class InitiativeComponent implements Component {
	private int initiative;

	public InitiativeComponent(int initiative) {
		this.initiative = initiative;
	}
	public InitiativeComponent initiative(int initiative) {
		this.initiative = initiative;
		return this;
	}
	public int initiative() {
		return initiative;
	}
}
