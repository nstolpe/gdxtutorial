package com.hh.gdxtutorial.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by nils on 5/26/16.
 */
public class GaussianBlurShaderProgram extends ShaderProgram {
	public GaussianBlurShaderProgram() {
		super(Gdx.files.internal("shaders/pass.through.vertex.glsl"), Gdx.files.internal("shaders/gaussian.blur.fragment.glsl"));
	}
}
