package com.hh.gdxtutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Game extends ApplicationAdapter {
	public PerspectiveCamera camera;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;

	public Environment environment;

	public CameraInputController camController;

	public boolean loading = true;
	public Random random = new Random();

	@Override
	public void create (){
		// declare and configure the camera.
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 5, 5);
		camera.lookAt(0, 0, 0);
		camera.near = 1;
		camera.far = 1000;
		camera.update();
		// declare camController and set it as the input processor.
		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);
		// declare the modelBatch and environment, add a light to the environment.
		modelBatch = new ModelBatch(new CelShaderProvider());
		environment = new Environment();
		environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -0.5f, 0.5f, -1.0f));
		// declare the assetManager and load the model.
		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);
		// set the clear color
		Gdx.gl.glClearColor(1, 1, 1, 1);
	}

	@Override
	public void render (){
		// cache delta
		final float delta = Math.min(1/30f, Gdx.graphics.getDeltaTime());
		// update the camController (this also updates the camera).
		camController.update();
		// clear color and depth buffers.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// trigger done loading when the assets are loaded.
		if (loading && assetManager.update())
			doneLoading();

		// set model rotations.
		for (int i = 0; i < instances.size; i++) {
			// use the instances index to give some variation to the rotation speeds
			int f = (i % 5) + 1;
			instances.get(i).transform.rotate(new Vector3(0, 1, 0), (90 * f * delta) % 360);
		}

		// render the scene
		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	/*
	 * On screen/window resize:
	 * Reset the camera viewportWidth and viewportHeight
	 */
	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		assetManager.dispose();
	}
	/*
	 * Add modelInstances to the scene once all assets are done loading.
	 */
	public void doneLoading() {
		loading = false;

		for (float x = -3.0f; x <= 3.0f; x+=2) {
			for (float y = -3.0f; y <= 3.0f; y+=2) {
				ModelInstance instance = new ModelInstance(assetManager.get("models/cube.g3dj", Model.class));
				Material mat = instance.getMaterial("skin");
				mat.set(ColorAttribute.createDiffuse(new Color(random.nextFloat(), 0.0f, random.nextFloat(), 1.0f)));
				instance.transform.setTranslation(x, y, 0.0f);
				instances.add(instance);
			}
		}
	}
	/*
	 * Inner class to for custom shader provider. Should move to its own class file in actual use.
	 */
	public class CelShaderProvider extends BaseShaderProvider {
		@Override
		protected Shader createShader(Renderable renderable) {
			return new CelShader(renderable);
		}
	}
	/*
	 * Inner class to for custom shader. Should move to its own class file in actual use.
	 */
	public class CelShader extends DefaultShader {
		public boolean celOn = true;
		public CelShader(Renderable renderable) {
			this(
				renderable,
				new Config(Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString(), Gdx.files.internal("shaders/cel.frag.glsl").readString())
			);
		}

		public CelShader(Renderable renderable, Config config) {
			super(renderable, config, createPrefix(renderable, config) + "#define celFlag\n");
		}
	}
}
