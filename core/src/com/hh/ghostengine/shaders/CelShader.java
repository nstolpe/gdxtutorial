package com.hh.ghostengine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

/**
 * Created by nils on 5/25/16.
 */
public class CelShader extends DefaultShader {
	public CelShader(Renderable renderable) {
		this(renderable,
			new Config(Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString(),
				Gdx.files.internal("shaders/cel.main.fragment.glsl").readString()),
			true);
	}

	public CelShader(Renderable renderable, Config config, boolean on) {
		super(renderable, config, createPrefix(renderable, config) + (on ? "#define celFlag\n" : ""));
	}
}
