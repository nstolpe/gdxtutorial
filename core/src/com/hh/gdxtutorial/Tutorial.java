package com.hh.gdxtutorial;

import com.badlogic.gdx.Game;
import com.hh.gdxtutorial.screens.CelShaderScreen;
import com.hh.gdxtutorial.screens.GaussianBlurShaderScreen;
import com.hh.gdxtutorial.screens.TurnEngineScreen;

public class Tutorial extends Game {

	@Override
	public void create () {
//		setScreen(new CelShaderScreen());
//		setScreen(new GaussianBlurShaderScreen());
		setScreen(new TurnEngineScreen());
//		setScreen(new TiltShiftShaderScreen());
	}

}
