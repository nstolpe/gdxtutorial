package com.hh.gdxtutorial.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.entity.systems.CelRenderer;
import com.hh.gdxtutorial.entity.systems.ModelBatchRenderer;
import com.hh.gdxtutorial.entity.systems.TurnSystem;
import com.hh.gdxtutorial.screens.input.DemoInputController;
import com.hh.gdxtutorial.shaders.PerPixelShaderProvider;

/**
 * Created by nils on 5/27/16.
 */
public class CombatScreen extends FpsScreen {
	public Engine engine = new Engine();
	public DemoInputController camController;

	public AssetManager assetManager;

	public ModelBatch modelBatch;

	public Environment environment;
	public Texture tex;

	private Label turnLabel;
	private TextButton defaultRendererButton;
	private TextButton celRendererButton;

	private ModelBatchRenderer modelBatchRenderer;
	private CelRenderer celRenderer;
	// @TODO get animation controller to its own spot.
	private AnimationController controller;
	// particle
	private ParticleSystem particleSystem;
	private ParticleEffect effect;

	// particle

	/**
	 * Setup input, 3d environment, the modelBatch and the assetManager.
	 */
	@Override
	public void show() {
		super.show();

		// declare camController and set it as the input processor.
		camController = new DemoInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch(new PerPixelShaderProvider());

		environment = new Environment();
//		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
		environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -0.5f, -0.6f, -0.7f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.4f, 1.0f, -0.3f));

		assetManager = new AssetManager();
		assetManager.load("models/plane.g3dj", Model.class);
		assetManager.load("models/sphere.g3dj", Model.class);
		assetManager.load("models/mask.ghost.g3dj", Model.class);

		// particle
		particleSystem = new ParticleSystem();
		BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(camera);
		particleSystem.add(billboardParticleBatch);
		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
		assetManager.load("effects/blast.blue.pfx", ParticleEffect.class, loadParam);
		// \particle
	}
	/**
	 * Adds extra turn data to the 2d stage, super gets the fps info.
	 */
	@Override
	public void initStage() {
		super.initStage();
		turnLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		table.row();
		table.add(turnLabel).expandY().bottom();

		defaultRendererButton = new TextButton("Default Renderer", buttonStyle);
		celRendererButton = new TextButton("Cel Renderer", buttonStyle);

		defaultRendererButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				modelBatchRenderer.setProcessing(true);
				celRenderer.setProcessing(false);
			}
		});

		celRendererButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				modelBatchRenderer.setProcessing(false);
				celRenderer.setProcessing(true);
			}
		});

		table.add(defaultRendererButton).expandY().bottom();
		table.add(celRendererButton).expandY().bottom();
	}
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// @TODO get animation controller to its own spot.
		if (controller != null)	controller.update(delta);

		camController.update();
		MessageManager.getInstance().update();

		if (loading && assetManager.update()) doneLoading();

		stringBuilder.setLength(0);
		stringBuilder.append(" Turn: ").append(engine.getSystem(TurnSystem.class) == null ? "" : engine.getSystem(TurnSystem.class).turnCount + ": " + engine.getSystem(TurnSystem.class).activeIndex());
		turnLabel.setText(stringBuilder);

		engine.update(delta);

		// particle
		modelBatch.begin(camera);
		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		modelBatch.render(particleSystem);
		modelBatch.end();
		// \particle
		super.render(delta);
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

	/**
	 * Setup the models for the scene and add the entity systems that rely on actors.
	 * The render system can be changed here, ModelBatch pass is the current.
	 */
	@Override
	public void doneLoading() {
		super.doneLoading();
		setupScene();
		setupActors();
		engine.addSystem(new TurnSystem());
		modelBatchRenderer = new ModelBatchRenderer(modelBatch, camera, environment);
		celRenderer = new CelRenderer(camera, environment);
		engine.addSystem(modelBatchRenderer);
		modelBatchRenderer.setProcessing(false);
		engine.addSystem(celRenderer);

		// particle
		ParticleEffect originalEffect = assetManager.get("effects/blast.blue.pfx", ParticleEffect.class);
		// we cannot use the originalEffect, we must make a copy each time we create new particle effect
		effect = originalEffect.copy();
		effect.translate(new Vector3(4, 4, 4));
		effect.init();
//		effect.start();  // optional: particle will begin playing immediately
		particleSystem.add(effect);
		// particle
	}

	public void setupActors() {
		Entity player = new Entity()
			.add(new PositionComponent(new Vector3(0, 2, 0)))
			.add(new ModelInstanceComponent(new ModelInstance(assetManager.get("models/mask.ghost.g3dj", Model.class))))
			.add(new InitiativeComponent(MathUtils.random(10)))
			.add(new PlayerComponent());

		// @TODO get animation controller to its own spot.
		controller = new AnimationController(player.getComponent(ModelInstanceComponent.class).instance());
//		controller.setAnimation("skeleton|expand", -1);

		engine.addEntity(player);

		// create texture for mobs
		tex = new Texture(Gdx.files.internal("models/sphere-purple.png"), true);
		tex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
		TextureAttribute texAttr = new TextureAttribute(TextureAttribute.Diffuse, tex);

		// create and position the mobs spheres/Actors
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				ModelInstance mi = new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class));
				mi.getMaterial("skin").set(texAttr);
				mi.getMaterial("skin").set(new ColorAttribute(ColorAttribute.Diffuse, MathUtils.random(), MathUtils.random(), MathUtils.random(), 1.0f));
				Entity mob = new Entity()
						.add(new PositionComponent(new Vector3(i * 20, 2, j * 20)))
						.add(new ModelInstanceComponent(mi))
						.add(new InitiativeComponent(MathUtils.random(10)))
						.add(new AiComponent());
				engine.addEntity(mob);
			}
		}
	}

	public void setupScene() {
		Entity p = new Entity()
				.add(new ModelInstanceComponent( new ModelInstance(assetManager.get("models/plane.g3dj", Model.class))))
				.add(new PositionComponent(new Vector3(0.0f, 0.0f, 0.0f)));

		engine.addEntity(p);
	}

	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
		tex.dispose();
		modelBatchRenderer.dispose();
		celRenderer.dispose();
	}
}
