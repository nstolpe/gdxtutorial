package com.hh.ghostengine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

/**
 * Created by nils on 6/6/16.
 */
public class PerPixelShader  extends DefaultShader {
	public static class Config extends DefaultShader.Config {

		public Config (final String vertexShader, final String fragmentShader) {
			super(vertexShader, fragmentShader);
			numBones = 12;
			numPointLights = 2;
			numSpotLights = 5;
			numDirectionalLights = 2;
		}
	}

	public PerPixelShader (final Renderable renderable) {
		super(renderable, new Config(
				Gdx.files.internal("shaders/per.pixel.vertex.glsl").readString(),
				Gdx.files.internal("shaders/per.pixel.fragment.glsl").readString()
		));
	}
}
