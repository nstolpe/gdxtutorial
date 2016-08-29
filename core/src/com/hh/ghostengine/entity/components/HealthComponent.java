package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by nils on 6/22/16.
 */
public class HealthComponent implements Component {
	public int totalHealth = 5;
	public int currentHealth = 5;

	public HealthComponent(int health) {
		totalHealth = health;
		currentHealth = health;
	}
}
