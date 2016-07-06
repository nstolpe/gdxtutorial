package com.hh.gdxtutorial.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.entity.systems.ModelBatchRenderer;
import com.hh.gdxtutorial.entity.systems.TurnSystem;
import com.hh.gdxtutorial.screens.input.DemoInputController;
import com.hh.gdxtutorial.shaders.PerPixelShaderProvider;
import com.hh.gdxtutorial.singletons.Manager;

/**
 * Created by nils on 5/27/16.
 */
public class CombatScreen extends FpsScreen {
	public DemoInputController camController;

	public ModelBatch modelBatch;

	public Environment environment;

	private Label turnLabel;

	private ModelBatchRenderer modelBatchRenderer;
	// particle @TODO should come from config.
	private ParticleEffect blastRed;
	private ParticleEffect blastBlue;

	/**
	 * Setup input, 3d environment, the modelBatch and the modelBatchRenderer.assetManager.
	 */
	@Override
	public void show() {
		super.show();
		// declare camController and set it as the input processor.
		camController = new DemoInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch(new PerPixelShaderProvider());

		environment = new Environment();
//		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));

		environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -0.5f, -0.6f, -0.7f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.4f, 1.0f, -0.3f));

		modelBatchRenderer = new ModelBatchRenderer(modelBatch, camera, environment);
		modelBatchRenderer.setProcessing(false);

		// gets rid of the moire effect
		ModelLoader.ModelParameters texParam = new ModelLoader.ModelParameters();
		texParam.textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
		texParam.textureParameter.genMipMaps = true;

		modelBatchRenderer.assetManager.load("models/plane.g3dj", Model.class, texParam);
		modelBatchRenderer.assetManager.load("models/mask.ghost.white.g3dj", Model.class, texParam);
		modelBatchRenderer.assetManager.load("models/mask.ghost.red.g3dj", Model.class, texParam);

		// @TODO move this to renderer.
		ParticleEffectLoader.ParticleEffectLoadParameter particleParam = new ParticleEffectLoader.ParticleEffectLoadParameter(modelBatchRenderer.particleSystem.getBatches());
		modelBatchRenderer.assetManager.load("effects/blast.blue.pfx", ParticleEffect.class, particleParam);
		modelBatchRenderer.assetManager.load("effects/blast.red.pfx", ParticleEffect.class, particleParam);
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
	}
	@Override
	public void render(float delta) {
		Engine engine = Manager.getInstance().engine();
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		camController.update();
		MessageManager.getInstance().update();
		Manager.getInstance().update(delta);

		if (loading && modelBatchRenderer.assetManager.update()) doneLoading();

		stringBuilder.setLength(0);
		stringBuilder.append(" Turn: ").append(engine.getSystem(TurnSystem.class) == null ? "" : engine.getSystem(TurnSystem.class).turnCount + ": " + engine.getSystem(TurnSystem.class).activeIndex());
		turnLabel.setText(stringBuilder);

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

		Manager.getInstance().engine().addSystem(new TurnSystem());
		Manager.getInstance().engine().addSystem(modelBatchRenderer);
		modelBatchRenderer.setProcessing(true);
	}

	public void setupActors() {
		blastRed = modelBatchRenderer.assetManager.get("effects/blast.red.pfx", ParticleEffect.class);
		createActor("models/mask.ghost.red.g3dj", blastRed, 0, 0, true);

		blastBlue = modelBatchRenderer.assetManager.get("effects/blast.blue.pfx", ParticleEffect.class);

		for (int i = -1; i <= 1; i += 2)
			for (int j = -1; j <= 1; j += 2)
				createActor("models/mask.ghost.white.g3dj", blastBlue, i * 20, j * 20, false);
	}

	/**
	 * Creates an actor. Needs to come after doneLoading() to ensure assets are loaded.
	 * @param modelString  String path to the actors model, relative to android/assets.
	 * @param effectSource ParticleEffect source for the particle effect. Will be copied.
	 * @param x            x position of the actor.
	 * @param z            z position of the actor.
	 * @param player       Boolean, true if the actor is a player.
	 */
	public void createActor(String modelString, ParticleEffect effectSource, int x, int z, boolean player) {
		ParticleEffect effectInstance = effectSource.copy();
		ModelInstance instance = new ModelInstance(modelBatchRenderer.assetManager.get(modelString, Model.class));
		EffectsComponent.Effect effect = new EffectsComponent.Effect("emit.root", effectInstance, (RegularEmitter) effectInstance.getControllers().first().emitter);
		Entity entity = new Entity()
				.add(new PositionComponent(new Vector3(x, 0, z)))
				.add(new RotationComponent(new Quaternion()))
				.add(new ModelInstanceComponent(instance))
				.add(new InitiativeComponent(MathUtils.random(10)))
				.add(new HealthComponent(MathUtils.random(6, 10)))
				.add(new EffectsComponent().addEffect("blast", effect));
		if (player) {
			entity.add(new PCComponent(entity));
		} else {
			NPCComponent mob = new NPCComponent(entity);
			entity.add(mob);
		}
		Manager.getInstance().engine().addEntity(entity);
	}

	public void setupScene() {
		Entity p = new Entity()
				.add(new ModelInstanceComponent( new ModelInstance(modelBatchRenderer.assetManager.get("models/plane.g3dj", Model.class))))
				.add(new RotationComponent(new Quaternion()))
				.add(new PositionComponent(new Vector3(0.0f, 0.0f, 0.0f)));

		Manager.getInstance().engine().addEntity(p);
	}

	@Override
	public void dispose() {
		super.dispose();
		Manager.getInstance().engine().removeAllEntities();
		modelBatchRenderer.dispose();
	}
}
