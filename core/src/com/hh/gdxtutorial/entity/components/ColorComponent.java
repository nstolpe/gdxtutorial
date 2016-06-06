package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by nils on 6/5/16.
 */
public class ColorComponent implements Component {
	private Color color;

	public ColorComponent() {}

	public ColorComponent(Color color) {
		this.color = color;
	}

	public ColorComponent color(Color color) {
		this.color = color;
		return this;
	}

	public Color color() {
		return color;
	}
}
