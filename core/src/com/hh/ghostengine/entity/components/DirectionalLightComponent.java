package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

/**
 * Created by nils on 10/10/16.
 */
public class DirectionalLightComponent implements Component {
	public DirectionalLight light;

	public DirectionalLightComponent(DirectionalLight light) {
		this.light = light;
	}
}
