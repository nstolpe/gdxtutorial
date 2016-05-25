package com.hh.gdxtutorial.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by nils on 5/25/16.
 */
public class CelLineShaderProgram extends ShaderProgram {
	public CelLineShaderProgram() {
		super(Gdx.files.internal("shaders/cel.line.vertex.glsl"), Gdx.files.internal("shaders/cel.line.fragment.glsl"));
	}
}
