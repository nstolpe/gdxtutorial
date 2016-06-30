package com.hh.gdxtutorial.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.entity.systems.ModelBatchRenderer;
import com.hh.gdxtutorial.entity.systems.TurnSystem;
import com.hh.gdxtutorial.helpers.Utility;
import com.hh.gdxtutorial.screens.FpsScreen;
import com.hh.gdxtutorial.screens.MainMenuScreen;
import com.hh.gdxtutorial.screens.input.DemoInputController;
import com.hh.gdxtutorial.shaders.PerPixelShaderProvider;

/**
 * Created by nils on 5/27/16.
 */
public class SscceScreen extends FpsScreen {
	public DemoInputController camController;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public AssetManager assetManager = new AssetManager();
	public ModelBatch modelBatch;
	public Environment environment;
	public Quaternion rotation = new Quaternion(new Vector3(0,1,0), 180);
	public ModelInstance character;

	/**
	 * Setup input, 3d environment, the modelBatch and the modelBatchRenderer.assetManager.
	 */
	@Override
	public void show() {
		super.show();
		camController = new DemoInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch(new PerPixelShaderProvider());

		environment = new Environment();

		environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -0.5f, -0.6f, -0.7f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.4f, 1.0f, -0.3f));

		ModelLoader.ModelParameters texParam = new ModelLoader.ModelParameters();
		texParam.textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
		texParam.textureParameter.genMipMaps = true;
		assetManager.load("models/plane.g3dj", Model.class, texParam);
		assetManager.load("models/mask.ghost.red.g3dj", Model.class, texParam);
		assetManager.finishLoading();
		character = new ModelInstance(assetManager.get("models/mask.ghost.red.g3dj", Model.class));

		instances.add(character);
		instances.add(new ModelInstance(assetManager.get("models/plane.g3dj", Model.class)));
		rotation = Utility.getRotTo(new Vector3(0,0,-1), new Vector3(0,0,0), new Vector3(5,0,5));
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		camController.update();
		MessageManager.getInstance().update();

		character.transform.set(new Vector3(0,0,0), rotation);

		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	/**
	 * Dispatches the SCREEN_RESIZE message to anything that's listening.
	 * Current Listeners: CelRenderer
	 * Calls super for the FpsScreen overlay.
	 * @param width
	 * @param height
	 */
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		MessageManager.getInstance().dispatchMessage(0, Messages.SCREEN_RESIZE, new Vector2(width, height));
	}

	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
	}
}
