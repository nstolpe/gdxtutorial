package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

/**
 * Created by nils on 6/5/16.
 */
public class ModelInstanceComponent implements Component {
	private ModelInstance modelInstance;

	/**
	 * Instantiate and return an empty ModelInstanceComponent.
	 */
	public ModelInstanceComponent() {}

	/**
	 * Instantiate a new ModelInstanceComponent and assign its modelInstance.
	 * Return the ModelInstanceComponent.
	 * @param modelInstance
	 */
	public ModelInstanceComponent(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	/**
	 * Set the modelInstance
	 * Return the ModelInstanceComponent.
	 * @param modelInstance
	 * @return
	 */
	public ModelInstanceComponent instance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
		return this;
	}

	/**
	 * Get the ModelInstance
	 * @return
	 */
	public ModelInstance instance() {
		return modelInstance;
	}
}
