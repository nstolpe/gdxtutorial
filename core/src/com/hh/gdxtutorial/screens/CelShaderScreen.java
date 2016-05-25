package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.shaders.CelDepthShaderProvider;
import com.hh.gdxtutorial.shaders.CelLineShaderProgram;
import com.hh.gdxtutorial.shaders.CelShaderProvider;

import java.util.Random;

public class CelShaderScreen extends AbstractScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;
	public ModelBatch depthBatch;
	public SpriteBatch spriteBatch;

	public CelLineShaderProgram celLineShader;
	public FrameBuffer fbo;
	public TextureRegion tr = new TextureRegion();
	public Environment environment;

	public Random random = new Random();

	@Override
	public void show() {
		// declare and configure the camera.
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0.48f, 5.67f, 2.37f);
		camera.lookAt(0, 0, 0);
		camera.near = 1;
		camera.far = 1000;
		camera.update();

		// declare camController and set it as the input processor.
		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);

		// declare the modelBatch, depthBatch and spriteBatch.
		modelBatch = new ModelBatch(new CelShaderProvider());
		depthBatch = new ModelBatch(new CelDepthShaderProvider());
		spriteBatch = new SpriteBatch();

		// declare the cel line shader and set it to use the celLineShader
		celLineShader = new CelLineShaderProgram();

		environment = new Environment();
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);
	}
	@Override
	public void render(float delta) {
		camController.update();

		if (loading && assetManager.update()) doneLoading();

		fbo.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		runModelBatch(depthBatch, camera, instances, null);
		fbo.end();

		tr.setRegion(fbo.getColorBufferTexture());
		tr.flip(false, true);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		runModelBatch(modelBatch, camera, instances, environment);

		spriteBatch.setShader(celLineShader);
		spriteBatch.begin();
		celLineShader.setUniformf("u_size", tr.getRegionWidth(), tr.getRegionHeight());
		spriteBatch.draw(tr, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.end();
	}

	private void runModelBatch(ModelBatch batch, Camera camera, Array<ModelInstance> instances, Environment env) {
		batch.render(instances, environment);
		batch.end();
		batch.begin(camera);
	}

	@Override
	public void doneLoading() {
		super.doneLoading();

		for (float x = -21.0f; x <= 21.0f; x+=2) {
			for (float y = -21.0f; y <= 21.0f; y+=2) {
				ModelInstance instance = new ModelInstance(assetManager.get("models/cube.g3dj", Model.class));
				Material mat = instance.getMaterial("skin");
				mat.set(ColorAttribute.createDiffuse(new Color(random.nextFloat() * 2.0f, 0.0f, random.nextFloat() / 2.0f, 1.0f)));
				instance.transform.setTranslation(x, y, 0.0f);
				instances.add(instance);
			}
		}
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		depthBatch.dispose();
		spriteBatch.dispose();
		celLineShader.dispose();
		fbo.dispose();
		assetManager.dispose();
	}
}
