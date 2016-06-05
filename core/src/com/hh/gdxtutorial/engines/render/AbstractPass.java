package com.hh.gdxtutorial.engines.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

/**
 * Created by nils on 6/5/16.
 */
public class AbstractPass implements Pass {
	public Array<Pass> input;
	public Array<Pass> output;
	public Color clearColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	@Override
	public void render(float delta) {

	}

	@Override
	public void begin() {

	}

	@Override
	public void end() {

	}
}