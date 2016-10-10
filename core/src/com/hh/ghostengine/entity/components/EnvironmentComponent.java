package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Environment;

/**
 * Created by nils on 10/10/16.
 */
public class EnvironmentComponent implements Component {
	public Environment environment;

	public EnvironmentComponent(Environment environment) {
		this.environment = environment;
	}
}
