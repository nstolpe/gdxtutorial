package com.hh.ghostengine.ai;

/**
 * Created by nils on 6/4/16.
 */
public class Messages {
	private Messages() {}
	// Screens for GhostEngine class
	public static final int CHANGE_SCREEN        = 0x00;
	/*
	 * Message sent when at touch of click event is captured by the input.
	 *   Example Telegram.extraInfo sent with this message type:
	 *     Vector3 (x and z hold the screen coordinates),
	 *     Vector2 (x = screen.x, y = screen.z)
	 *     int[]{screen.x, screen.y}
	 */
	public static final int TOUCH_CLICK_INPUT = 0x01;
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
}
