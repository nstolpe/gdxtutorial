package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

/**
 * Created by nils on 10/10/16.
 */
public class AmbientComponent implements Component {
	public ColorAttribute ambient;

	public AmbientComponent(ColorAttribute ambient) {
		this.ambient = ambient;
	}
}
