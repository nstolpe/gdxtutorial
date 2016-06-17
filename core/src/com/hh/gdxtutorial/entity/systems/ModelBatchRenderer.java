package com.hh.gdxtutorial.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.Mappers;
import com.hh.gdxtutorial.entity.components.ModelInstanceComponent;
import com.hh.gdxtutorial.entity.components.PositionComponent;

/**
 * An entity system that renders all entities with a PositionComponent and ModelInstanceComponent.
 */
public class ModelBatchRenderer extends EntitySystem implements Disposable, Telegraph {
	protected ImmutableArray<Entity> entities;
	protected ModelBatch modelBatch;
	protected Family family = Family.all(PositionComponent.class, ModelInstanceComponent.class).get();
	public Color clearColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	public Camera camera;
	public Environment env;

	public ModelBatchRenderer(ModelBatch modelBatch, Camera camera, Environment env) {
		this.modelBatch = modelBatch;
		this.camera = camera;
		this.env = env;
		MessageManager.getInstance().addListener(this, Messages.SCREEN_RESIZE);
	}

	/**
	 * Sets a new ModelBatch and disposes of the old, if it exists.
	 * @param modelBatch
	 */
	public void modelBatch(ModelBatch modelBatch) {
		if (this.modelBatch != null) this.modelBatch.dispose();
		this.modelBatch = modelBatch;
	}

	/**
	 * Getter for modelBatch.
	 * @return
	 */
	public ModelBatch modelBatch() {
		return this.modelBatch;
	}
	/**
	 * Triggered when SCREEN_RESIZE message is received.
	 * @param dimensions
	 */
	public void resize(Vector2 dimensions) {
		camera.viewportWidth = dimensions.x;
		camera.viewportHeight = dimensions.y;
		camera.update();
	}
	/**
	 * Run the modelBatch
	 * @param deltaTime
	 */
	@Override
	public void update(float deltaTime) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		modelBatch.begin(camera);
		for (Entity e : entities) {
			ModelInstance instance = Mappers.MODEL_INSTANCE.get(e).instance();
			instance.transform.setTranslation(Mappers.POSITION.get(e).position());
			modelBatch.render(instance, env);
		}
		modelBatch.end();
	}
	@Override
	public void addedToEngine(Engine engine) {
		entities = engine.getEntitiesFor(family);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		System.out.println("removing from engine");
	}
	/**
	 * Disposables are managed here.
	 */
	@Override
	public void dispose() {
		modelBatch.dispose();
	}
	/**
	 * Handles incoming messages
	 * @param msg
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.SCREEN_RESIZE:
				resize((Vector2) msg.extraInfo);
				break;
			default:
				return false;
		}
		return true;
	}
}
