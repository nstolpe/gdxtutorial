package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

/**
 * Created by nils on 6/26/16.
 */
public class TargetComponent implements Component {
	public Entity target;

	public TargetComponent(Entity target) {
		this.target = target;
	}
}
