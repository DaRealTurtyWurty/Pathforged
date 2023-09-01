package dev.turtywurty.pathforged.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.HashMap;
import java.util.Map;

public class EntityStepOnBlockEvent extends EntityEvent {
    public static final Map<Entity, BlockPos> LAST_POSITIONS = new HashMap<>();

    private final BlockPos position;
    private final BlockState state;

    public EntityStepOnBlockEvent(Entity entity, BlockPos position, BlockState state) {
        super(entity);
        this.position = position;
        this.state = state;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public BlockState getState() {
        return this.state;
    }
}
