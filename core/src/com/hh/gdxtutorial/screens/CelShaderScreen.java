package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.shaders.CelLineShaderProgram;

public class CelShaderScreen extends AbstractScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public ModelBatch modelBatch;
	public ModelBatch depthBatch;
	public SpriteBatch spriteBatch;

	public CelLineShaderProgram celLineShader;
	public FrameBuffer fbo;
	public TextureRegion tr = new TextureRegion();

	@Override
	public void doneLoading() {
		super.doneLoading();
	}
}
