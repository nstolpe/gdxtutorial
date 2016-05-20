package com.hh.gdxtutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Game extends ApplicationAdapter {
	public PerspectiveCamera camera;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public FrameBuffer fbo;
	public TextureRegion textureRegion;

	public SpriteBatch spriteBatch;
	public ModelBatch modelBatch;

	public Environment environment = new Environment();

	public CameraInputController camController;

	public boolean loading = true;
	@Override
	public void create () {
		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 5, 5);
		camera.lookAt(0, 0, 0);
		camera.near = 1;
		camera.far = 1000;
		camera.update();

		environment.add(new DirectionalLight().set(0.8f, 0.8f, 1.8f, -1f, -0.8f, 0.2f));

		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);

		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);
	}

	@Override
	public void render () {
		final float delta = Math.min(1/30f, Gdx.graphics.getDeltaTime());
		camera.update();
		camController.update();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (loading && assetManager.update())
			doneLoading();

		for(ModelInstance instance: instances)
			instance.transform.rotate(new Vector3(0, 1, 0), (90 * delta) % 360);

		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	@Override
	public void resize (int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		spriteBatch.dispose();
		assetManager.dispose();
	}
	public void doneLoading() {
		loading = false;
		ModelInstance instance = new ModelInstance(assetManager.get("models/cube.g3dj", Model.class));
		instance.transform.setTranslation(0.0f, 0.0f, 0.0f);
		instances.add(instance);
	}
}
