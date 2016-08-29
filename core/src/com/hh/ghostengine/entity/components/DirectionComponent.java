package com.hh.ghostengine.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nils on 6/5/16.
 */
public class DirectionComponent implements Component {
	private Vector3 direction = new Vector3(0, 0, -1);

	public DirectionComponent() {}
	/**
	 * Constructor takes Vec3 for direction.
	 * @param direction
	 */
	public DirectionComponent(Vector3 direction) {
		this.direction = direction;
	}
	/**
	 * Setter for the direction Vector3. Return the DirectionComponent.
	 * @param direction
	 * @return
	 */
	public DirectionComponent direction(Vector3 direction) {
		this.direction = direction;
		return this;
	}
	/**
	 * Getter for the Vector3 direction.
	 * @return
	 */
	public Vector3 direction() {
		return direction;
	}
}
