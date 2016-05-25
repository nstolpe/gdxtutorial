package com.hh.gdxtutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Tutorial extends ApplicationAdapter {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;
	public ModelBatch depthBatch;
	public SpriteBatch spriteBatch;
	public CelLineShaderProgram celLineShader;
	public TiltShiftShaderProgram tiltShiftShader;

	public FrameBuffer fbo2;
	public FrameBuffer fbo1;
	public TextureRegion depthTextureRegion = new TextureRegion();
	public TextureRegion sceneTextureRegion = new TextureRegion();

	public Environment environment;

	public boolean loading = true;

	public Random random = new Random();

	@Override
	public void create () {
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
		tiltShiftShader = new TiltShiftShaderProgram();
		if (tiltShiftShader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + tiltShiftShader.getLog());

		// add an environment and add a light to it.
		environment = new Environment();
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		// declare the assetManager and load the model.
		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);

		// set the clear color
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	}

	@Override
	public void render (){
		// cache delta
		final float delta = Math.min(1/30f, Gdx.graphics.getDeltaTime());
		// update the camController (this also updates the camera).
		camController.update();
		// trigger done loading when the assets are loaded.
		if (loading && assetManager.update())
			doneLoading();

		updateModels(delta);
		// render depth to fbo1
		drawDepth();
		// render scene (cel and outline) to fbo2
		drawScene();
		// draw tilt shift passes and output the last.
		drawTiltShift();
	}

	private void drawTiltShift() {
		int blurPasses = 4;

		for (int i = 1; i <= blurPasses; i++) {
			fbo1.begin();
			spriteBatch.setShader(tiltShiftShader);
			spriteBatch.begin();
			tiltShiftShader.setUniformf("u_size", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			tiltShiftShader.setUniformf("u_tiltPercentage", 0.85f);
			tiltShiftShader.setUniformf("u_dimension", new Vector2(0, 1));

			if (i == 1) spriteBatch.draw(sceneTextureRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			else spriteBatch.draw(fbo2.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			spriteBatch.end();
			fbo1.end();

			if (i != blurPasses) fbo2.begin();

			spriteBatch.begin();
			tiltShiftShader.setUniformf("u_size", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			tiltShiftShader.setUniformf("u_tiltPercentage", 0.85f);
			tiltShiftShader.setUniformf("u_dimension", new Vector2(1, 0));
			spriteBatch.draw(fbo1.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			spriteBatch.end();

			if (i != blurPasses) fbo2.end();
			else spriteBatch.setShader(null);
		}
	}

	/*
	 * Draws a bunch of models.
	 */
	private void drawModels(ModelBatch modelBatch, Camera camera, Array<ModelInstance> instances, Environment environment) {
		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}
	private void drawScene() {
		// render the scene and the outlines to fbo2
		fbo2.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		drawModels(modelBatch, camera, instances, environment);
		drawOutlines();
		fbo2.end();

		sceneTextureRegion.setRegion(fbo2.getColorBufferTexture());
		sceneTextureRegion.flip(false, true);
	}

	private void drawDepth() {
		fbo1.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		drawModels(depthBatch, camera, instances, null);
		fbo1.end();

		depthTextureRegion.setRegion(fbo1.getColorBufferTexture());
		depthTextureRegion.flip(false, true);
	}

	/*
	 * Draws the outlines.
	 */
	private void drawOutlines() {
		spriteBatch.setShader(celLineShader);
		spriteBatch.begin();
		celLineShader.setUniformf("u_size", depthTextureRegion.getRegionWidth(), depthTextureRegion.getRegionHeight());
		spriteBatch.draw(depthTextureRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.end();
	}

	private void updateModels(float delta) {
		// set model rotations.
		for (int i = 0; i < instances.size; i++) {
			// use the instances index to give some variation to the rotation speeds
			int f = (i % 5) + 1;
			instances.get(i).transform.rotate(new Vector3(0, 1, 0), (90 * f / 8 * delta) % 360);
		}
	}

	/*
	 * On screen/window resize:
	 * Reset the camera viewportWidth and viewportHeight
	 * Set the spriteBatch's projection matrix.
	 * Init/reinit the fbo1
	 */
	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();

		spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));

		if (fbo2 != null) fbo2.dispose();
		fbo2 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);

		if (fbo1 != null) fbo1.dispose();
		fbo1 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		depthBatch.dispose();
		spriteBatch.dispose();
		celLineShader.dispose();
		tiltShiftShader.dispose();
		fbo2.dispose();
		fbo1.dispose();
		assetManager.dispose();
	}
	/*
	 * Add modelInstances to the scene once all assets are done loading.
	 */
	public void doneLoading() {
		loading = false;

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
	/*
	 * Inner class for custom shader provider. Should move to its own class file in actual use.
	 */
	public class CelShaderProvider extends BaseShaderProvider {
		@Override
		protected Shader createShader(Renderable renderable) {
			return new CelShader(renderable);
		}
	}
	/*
	 * Inner class for custom shader. Should move to its own class file in actual use.
	 */
	public class CelShader extends DefaultShader {
		public CelShader(Renderable renderable) {
			this(renderable,
				new Config(Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString(),
					Gdx.files.internal("shaders/cel.main.fragment.glsl").readString()),
				true);
		}

		public CelShader(Renderable renderable, Config config, boolean on) {
			super(renderable, config, createPrefix(renderable, config) + (on ? "#define celFlag\n" : ""));
		}
	}

	public class CelDepthShaderProvider extends BaseShaderProvider {
		@Override
		protected Shader createShader(final Renderable renderable) {
			return new CelDepthShader(renderable);
		}
	}

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

	public class CelLineShaderProgram extends ShaderProgram {
		public CelLineShaderProgram() {
			super(Gdx.files.internal("shaders/cel.line.vertex.glsl"), Gdx.files.internal("shaders/cel.line.fragment.glsl"));
		}
	}

	public class TiltShiftShaderProgram extends ShaderProgram {
		public TiltShiftShaderProgram() {
			super(Gdx.files.internal("shaders/tilt.shift.vertex.glsl"), Gdx.files.internal("shaders/tilt.shift.fragment.glsl"));
		}
	}

}
