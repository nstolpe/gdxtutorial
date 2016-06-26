package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.ComponentMapper;

/**
 * Created by nils on 6/5/16.
 */
public class Mappers {
	public static final ComponentMapper<ColorComponent>         COLOR          = ComponentMapper.getFor(ColorComponent.class);
	public static final ComponentMapper<ModelInstanceComponent> MODEL_INSTANCE = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<PositionComponent>      POSITION       = ComponentMapper.getFor(PositionComponent.class);
	public static final ComponentMapper<DirectionComponent>     DIRECTION      = ComponentMapper.getFor(DirectionComponent.class);
	public static final ComponentMapper<RotationComponent>      ROTATION       = ComponentMapper.getFor(RotationComponent.class);
	public static final ComponentMapper<InitiativeComponent>    INITIATIVE     = ComponentMapper.getFor(InitiativeComponent.class);
	public static final ComponentMapper<PlayerComponent>        PLAYER         = ComponentMapper.getFor(PlayerComponent.class);
	public static final ComponentMapper<MobComponent>           MOB            = ComponentMapper.getFor(MobComponent.class);
	public static final ComponentMapper<EffectsComponent>       EFFECTS        = ComponentMapper.getFor(EffectsComponent.class);
	public static final ComponentMapper<TargetComponent>        TARGET         = ComponentMapper.getFor(TargetComponent.class);
}
