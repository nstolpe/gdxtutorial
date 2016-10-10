package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;

/**
 * Created by nils on 10/10/16.
 */
public class SpotLightComponent implements Component {
	public SpotLight light;

	public SpotLightComponent(SpotLight light) {
		this.light = light;
	}
}
