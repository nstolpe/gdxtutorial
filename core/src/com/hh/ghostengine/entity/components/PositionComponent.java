package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nils on 6/5/16.
 */
public class PositionComponent implements Component {
	public Vector3 position;

	public PositionComponent() {}
	/**
	 * Constructor takes Vec3 for position.
	 * @param position
	 */
	public PositionComponent(Vector3 position) {
		this.position = position;
	}
	/**
	 * Setter for the position Vector3. Return the PositionComponent.
	 * @param position
	 * @return
	 */
	public PositionComponent position(Vector3 position) {
		this.position = position;
		return this;
	}
	/**
	 * Getter for the Vector3 position.
	 * @return
	 */
	public Vector3 position() {
		return position;
	}
}
