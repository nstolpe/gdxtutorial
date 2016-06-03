package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.shaders.GaussianBlurShaderProgram;

import java.util.Random;

/**
 * Created by nils on 5/26/16.
 */
public class GaussianBlurShaderScreen extends FpsScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;
	public SpriteBatch spriteBatch;

	public GaussianBlurShaderProgram gaussianBlurShader;

	public Array<FrameBuffer> fbos = new Array<FrameBuffer>();
	public TextureRegion tr = new TextureRegion();
	public Environment environment;

	public Random random = new Random();
	public Vector2 dimension = new Vector2(1, 0);

	@Override
	public void show() {
		fbos.setSize(2);

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

		gaussianBlurShader = new GaussianBlurShaderProgram();
		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch();
		spriteBatch.setShader(gaussianBlurShader);


		environment = new Environment();
//		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);

		super.show();
	}
	@Override
	public void render(float delta) {
		camController.update();
		updateModels(delta);
		if (loading && assetManager.update()) doneLoading();

		FrameBuffer fbo = fbos.get(0);
		fbo.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		runModelBatch(modelBatch, camera, instances, environment);
		fbo.end();

		tr.setRegion(fbo.getColorBufferTexture());
		tr.flip(false, true);

		gaussianBlur();
		super.render(delta);
	}

	private void gaussianBlur() {
		int blurPasses = 10;
		for (int i = 1; i <= blurPasses; i++) {
			gaussianPass(dimension, tr, fbos.get(0));
			dimension.set(dimension.y, dimension.x);
			tr.setRegion(fbos.get(0).getColorBufferTexture());
			gaussianPass(dimension, tr, i == blurPasses ? null : fbos.get(1));
			dimension.set(dimension.y, dimension.x);
			tr.setRegion(fbos.get(1).getColorBufferTexture());
		}
	}
	private void gaussianPass(Vector2 dimension, TextureRegion source, FrameBuffer fbo) {
		if (fbo != null) fbo.begin();
		spriteBatch.begin();
		gaussianBlurShader.setUniformf("u_size", fbos.get(0).getWidth(), fbos.get(0).getHeight());
		gaussianBlurShader.setUniformf("u_dimension", dimension);
		spriteBatch.draw(source, 0, 0, source.getRegionWidth(), source.getRegionHeight());
		spriteBatch.end();
		if (fbo != null) fbo.end();
	}

	public void updateModels(float delta) {
		// set model rotations.
		for (int i = 0; i < instances.size; i++) {
			// use the instances index to give some variation to the rotation speeds
			int f = (i % 5) + 1;
			instances.get(i).transform.rotate(new Vector3(0, 1, 0), (90 * f / 8 * delta) % 360);
		}
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();

		spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));

		for (int i = 0; i < fbos.size; i++) {
			if (fbos.get(i) != null) fbos.get(i).dispose();
			fbos.set(i, new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true));
		}
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
		super.dispose();
		modelBatch.dispose();
		spriteBatch.dispose();
		gaussianBlurShader.dispose();
		assetManager.dispose();
		instances.clear();

		for (FrameBuffer fbo: fbos) fbo.dispose();
	}
}

