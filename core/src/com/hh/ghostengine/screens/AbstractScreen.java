package com.hh.ghostengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;

/**
 * Abstract screen class that implements stubs for Screen methods.
 *
 */
public abstract class AbstractScreen implements Screen {
	// loading state. true until switched off.
	protected boolean loading = true;
	// transparent black clear color.
	protected Color clearColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	@Override
	public void show() {}

	@Override
	public void resize(int width, int height) {}

	/**
	 * Set the clear and clear depth and color.
	 * @param delta
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {}

	/**
	 * Trigger after assets have been loded to do things like
	 * create model instances.
	 */
	public void doneLoading() {
		loading = false;
	}

	/**
	 * Utility function for drawing an array of ModelInstances with a ModelBatch. The Environment can be null.
	 * @param batch
	 * @param camera
	 * @param instances
	 * @param environment
	 */
	protected void runModelBatch(ModelBatch batch, Camera camera, Array<ModelInstance> instances, Environment environment) {
		batch.begin(camera);
		batch.render(instances, environment);
		batch.end();
	}
}
