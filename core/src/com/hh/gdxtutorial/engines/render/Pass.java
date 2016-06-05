package com.hh.gdxtutorial.engines.render;

import com.badlogic.gdx.utils.Array;

/**
 * Created by nils on 6/5/16.
 */
public interface Pass {
	void render(float delta);
	void begin();
	void end();
}
