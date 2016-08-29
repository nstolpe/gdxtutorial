package com.hh.ghostengine.shaders;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

/**
 * Created by nils on 6/6/16.
 */
public class PerPixelShaderProvider extends BaseShaderProvider {
	@Override
	protected Shader createShader(Renderable renderable) {
		return new PerPixelShader(renderable);
	}
}
