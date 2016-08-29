package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by nils on 6/5/16.
 */
public class ModelInstanceComponent implements Component {
	public ModelInstance instance;
	public AnimationController controller;
	public BoundingBox boundingBox;
	public Vector3 center = new Vector3();
	public Vector3 dimensions = new Vector3();
	public float radius;

	// nodes keyed to strings where things can be attached.
	private ArrayMap<String, Node> attachmentPoints = new ArrayMap<String, Node>();

	/**
	 * Instantiate and return an empty ModelInstanceComponent.
	 */
	public ModelInstanceComponent() {}
	/**
	 * Instantiate a new ModelInstanceComponent and assign its instance.
	 * Return the ModelInstanceComponent.
	 * @param instance
	 */
	public ModelInstanceComponent(ModelInstance instance) {
		this.instance = instance;
		boundingBox = instance.calculateBoundingBox(new BoundingBox());
		boundingBox.getCenter(center);
		boundingBox.getDimensions(dimensions);
		radius = dimensions.len() / 2f;
		this.controller = new AnimationController(this.instance);
	}
	/**
	 * Set the instance
	 * Return the ModelInstanceComponent.
	 * @param modelInstance
	 * @return
	 */
	public ModelInstanceComponent instance(ModelInstance modelInstance) {
		this.instance = modelInstance;
		return this;
	}
	/**
	 * Get the ModelInstance
	 * @return
	 */
	public ModelInstance instance() {
		return instance;
	}
	/**
	 * Set
	 * @param controller
	 * @return
	 */
	public ModelInstanceComponent controller(AnimationController controller) {
		this.controller = controller;
		return this;
	}

	/**
	 * Getter for the controller.
	 * @return
	 */
	public AnimationController controller() {
		return controller;
	}
}
