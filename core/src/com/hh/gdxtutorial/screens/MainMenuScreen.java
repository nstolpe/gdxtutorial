package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by nils on 6/3/16.
 */
public class MainMenuScreen extends AbstractScreen {
	private Stage stage;
	private BitmapFont font;
	private TextButton celShaderScreenButton;
	private TextButton gaussianBlurShaderScreenButton;
	private TextButton turnEngineScreenButton;
	private TextButton tiltShiftShaderScreenButton;
	private TextButtonStyle buttonStyle;
	private Table buttonColumn;

	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		font = new BitmapFont();
		buttonStyle = new TextButtonStyle();
		buttonStyle.font = font;
		font.getData().setScale(2.0f, 2.0f);
		buttonStyle.checkedFontColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);

		celShaderScreenButton = new TextButton("CelShaderScreen", buttonStyle);
		gaussianBlurShaderScreenButton = new TextButton("GaussianBlurShaderScreen", buttonStyle);
		tiltShiftShaderScreenButton = new TextButton("TiltShiftShaderScreen", buttonStyle);
		turnEngineScreenButton = new TextButton("TurnEngineScreen", buttonStyle);

		celShaderScreenButton.pad(10.0f);
		celShaderScreenButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MessageManager.getInstance().dispatchMessage(0, 0x02, CelShaderScreen.class);
			}
		});
		gaussianBlurShaderScreenButton.pad(10.0f);
		gaussianBlurShaderScreenButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MessageManager.getInstance().dispatchMessage(0, 0x02, GaussianBlurShaderScreen.class);
			}
		});
		tiltShiftShaderScreenButton.pad(10.0f);
		tiltShiftShaderScreenButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MessageManager.getInstance().dispatchMessage(0, 0x02, TiltShiftShaderScreen.class);
			}
		});
		turnEngineScreenButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MessageManager.getInstance().dispatchMessage(0, 0x02, TurnEngineScreen.class);
			}
		});

		buttonColumn = new Table();
		buttonColumn.setFillParent(true);
		buttonColumn.add(celShaderScreenButton);
		buttonColumn.row();
		buttonColumn.add(gaussianBlurShaderScreenButton);
		buttonColumn.row();
		buttonColumn.add(tiltShiftShaderScreenButton);
		buttonColumn.row();
		buttonColumn.add(turnEngineScreenButton);

		stage.addActor(buttonColumn);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		stage.draw();
		MessageManager.getInstance().update();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		super.dispose();
		stage.dispose();
		font.dispose();
	}
}
