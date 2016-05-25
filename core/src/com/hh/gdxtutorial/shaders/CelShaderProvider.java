package com.hh.gdxtutorial.shaders;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

/**
 * Created by nils on 5/25/16.
 */
public class CelShaderProvider extends BaseShaderProvider {
	@Override
	protected Shader createShader(Renderable renderable) {
		return new CelShader(renderable);
	}
}