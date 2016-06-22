package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by nils on 6/17/16.
 */
public class RotationComponent implements Component {
	private Quaternion rotation;

	public RotationComponent() {}

	public RotationComponent(Quaternion rotation) {
		this.rotation = rotation;
	}

	public RotationComponent rotation(Quaternion rotation) {
		this.rotation = rotation;
		return this;
	}

	public Quaternion rotation() {
		return rotation;
	}
}
