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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Game extends ApplicationAdapter {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;
	public ModelBatch depthBatch;
	public SpriteBatch outlineBatch;
	public CelLineShaderProgram outlineShader;

	public FrameBuffer fbo;
	public TextureRegion textureRegion;

	public Environment environment;

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

		// declare the modelBatch, depthBatch and outlineBatch.
		modelBatch = new ModelBatch(new CelShaderProvider());
		depthBatch = new ModelBatch(new CelDepthShaderProvider());
		outlineBatch= new SpriteBatch();

		// declare the cel line shader.
		outlineShader = new CelLineShaderProgram();

		// add an environment and add a light to it.
		environment = new Environment();
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		// declare the assetManager and load the model.
		assetManager = new AssetManager();
		assetManager.load("models/cube.g3dj", Model.class);

		// set the clear color
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
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

		// set model rotations.
		for (int i = 0; i < instances.size; i++) {
			// use the instances index to give some variation to the rotation speeds
			int f = (i % 5) + 1;
			instances.get(i).transform.rotate(new Vector3(0, 1, 0), (90 * f / 2 * delta) % 360);
		}

		// clear color and depth buffers.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// render depth to the framebuffer
		fbo.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		depthBatch.begin(camera);
		depthBatch.render(instances);
		depthBatch.end();
		fbo.end();

		textureRegion = new TextureRegion(fbo.getColorBufferTexture());
		textureRegion.flip(false, true);

		// render the scene
		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();

		outlineBatch.setShader(outlineShader);
		outlineBatch.begin();
		outlineBatch.draw(textureRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		outlineBatch.end();
		outlineBatch.setShader(null);
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

		outlineShader.setUniformf("u_size", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		outlineBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));

		if (fbo != null) fbo.dispose();
		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
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
		public CelShader(Renderable renderable) {
			this(
					renderable,
					new Config(
							Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString(),
							Gdx.files.internal("shaders/cel.main.fragment.glsl").readString()
					),
					true
			);
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

		@Override
		public void begin() {
			super.begin();
			setUniformf("u_size", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}
}
