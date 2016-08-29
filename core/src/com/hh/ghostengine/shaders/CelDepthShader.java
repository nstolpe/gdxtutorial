package com.hh.ghostengine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

/**
 * Created by nils on 5/25/16.
 */
public class CelDepthShader extends DepthShader {
	public final int u_near = register("u_near");
	public final int u_far = register("u_far");

	public CelDepthShader(Renderable renderable) {
		super(renderable, new DepthShader.Config(Gdx.files.internal("shaders/cel.depth.vertex.glsl").readString(), Gdx.files.internal("shaders/cel.depth.fragment.glsl").readString()));
	}

	@Override
	public void begin (Camera camera, RenderContext context) {
		super.begin(camera, context);
		set(u_near, camera.near);
		set(u_far, camera.far);
	}
}
