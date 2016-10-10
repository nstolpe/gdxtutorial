package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;

/**
 * Created by nils on 10/10/16.
 */
public class PointLightComponent implements Component {
	public PointLight light;

	public PointLightComponent(PointLight light) {
		this.light = light;
	}
}
