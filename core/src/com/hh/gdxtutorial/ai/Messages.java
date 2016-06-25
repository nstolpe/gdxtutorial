package com.hh.gdxtutorial.ai;

import com.badlogic.ashley.core.Entity;

/**
 * Created by nils on 6/4/16.
 */
public class Messages {
	// Screens for Tutorial class
	public static final int CHANGE_SCREEN        = 0x00;
	// interact button (left) has been clicked
	public static final int INTERACT_TOUCH       = 0x01;
	// advanceActor
	public static final int ADVANCE_TURN_CONTROL = 0x02;
	public static final int SCREEN_RESIZE        = 0x03;
	public static final int DEFAULT_RENDERER     = 0x04;
	public static final int CEL_RENDERER         = 0x05;
	public static final int TARGET_ACQUIRED      = 0x06;
	public static final int ATTACK_PRE           = 0x07;
	public static final int ATTACK               = 0x08;
	public static final int ATTACK_POST          = 0x09;
	public static final int REST                 = 0x10;

	/**
	 * Message extraInfo
	 */
	public static class TargetMessageData {
		public Entity actor;
		public Entity target;

		public TargetMessageData() {}

		public TargetMessageData(Entity actor, Entity target) {
			this.actor = actor;
			this.target = target;
		}
	}
}
