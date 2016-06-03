package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

/**
 * Extend from this class if you want to have an FPS 2d overlay. Call each implemented method
 * from the derived class via super.
 *
 * Make sure `super.render(delta)` comes at the end of the derived class's `render()` call so
 * the 2d stage is drawn on top.
 */
public abstract class FpsScreen extends AbstractScreen {
	protected Stage stage;
	protected BitmapFont font;
	protected Label label;
	protected StringBuilder stringBuilder;

	/**
	 * Sets up everything we need to draw the FPS overlay.
	 */
	@Override
	public void show() {
		stage = new Stage();
		font = new BitmapFont();
		label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		stage.addActor(label);
		stringBuilder = new StringBuilder();
	}

	/**
	 * Resizes the stage.
	 */
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	/**
	 * Draws the stage.
	 */
	@Override
	public void render(float delta) {
		stringBuilder.setLength(0);
		stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
		label.setText(stringBuilder);
		stage.draw();
	}

	/**
	 * Get rid of our disposables.
	 */
	@Override
	public void dispose() {
		font.dispose();
		stage.dispose();
	}
}
