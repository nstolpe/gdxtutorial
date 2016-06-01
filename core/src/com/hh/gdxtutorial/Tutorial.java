package com.hh.gdxtutorial;

import com.badlogic.gdx.Game;
import com.hh.gdxtutorial.screens.TurnManagerScreen;

public class Tutorial extends Game {

	@Override
	public void create () {
//		setScreen(new CelShaderScreen());
//		setScreen(new GaussianBlurShaderScreen());
		setScreen(new TurnManagerScreen());
//		setScreen(new TiltShiftShaderScreen());
	}

}
