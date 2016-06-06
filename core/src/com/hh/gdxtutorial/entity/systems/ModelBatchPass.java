package com.hh.gdxtutorial.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.hh.gdxtutorial.entity.components.Mappers;
import com.hh.gdxtutorial.entity.components.ModelInstanceComponent;
import com.hh.gdxtutorial.entity.components.PositionComponent;

/**
 * Created by nils on 6/5/16.
 */
public class ModelBatchPass extends EntitySystem {
	private ImmutableArray<Entity> entities;

	public ModelBatch modelBatch;
	public Camera cam;
	public Environment env;

	public ModelBatchPass() {
		this(new ModelBatch(), new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), null);
	}

	public ModelBatchPass(ModelBatch batch, Camera cam, Environment env) {
		this.modelBatch = batch;
		this.cam = cam;
		this.env = env;
	}

	@Override
	public void update(float deltaTime) {
		modelBatch.begin(cam);
		for (Entity e : entities) {
			ModelInstance instance = Mappers.MODEL_INSTANCE.get(e).instance();
			instance.transform.setTranslation(Mappers.POSITION.get(e).position());
			modelBatch.render(instance, env);
		}
		modelBatch.end();
	}
	@Override
	public void addedToEngine(Engine engine) {
		entities = engine.getEntitiesFor(Family.all(PositionComponent.class, ModelInstanceComponent.class).get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		System.out.println("removing from engine");
	}

}
