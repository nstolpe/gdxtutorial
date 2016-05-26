package com.hh.gdxtutorial.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by nils on 5/25/16.
 */
public class TiltShiftShaderProgram extends ShaderProgram {
	public TiltShiftShaderProgram() {
		super(Gdx.files.internal("shaders/pass.through.vertex.glsl"), Gdx.files.internal("shaders/tilt.shift.fragment.glsl"));
	}
}
