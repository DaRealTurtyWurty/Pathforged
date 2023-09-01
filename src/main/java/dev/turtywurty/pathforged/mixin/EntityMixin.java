package dev.turtywurty.pathforged.mixin;

import dev.turtywurty.pathforged.event.EntityStepOnBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.turtywurty.pathforged.event.EntityStepOnBlockEvent.LAST_POSITIONS;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void pathforged$onStepOnBlock(MoverType pType, Vec3 pPos, CallbackInfo callback, Vec3 colVec, double colVecLengthSqr, boolean hasXChanged, boolean hasZChanged, BlockPos belowPos, BlockState belowState, Block belowBlock) {
        Entity entity = (Entity) (Object) this;

        if (LAST_POSITIONS.containsKey(entity) && LAST_POSITIONS.get(entity).equals(belowPos))
            return;

        LAST_POSITIONS.put(entity, belowPos);
        MinecraftForge.EVENT_BUS.post(new EntityStepOnBlockEvent(entity, belowPos, belowState));
    }

    @Inject(
            method = "setRemoved",
            at = @At("TAIL")
    )
    private void pathforged$setRemoved(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        LAST_POSITIONS.remove(entity);
    }
}
