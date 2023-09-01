package dev.turtywurty.pathforged.handler;

import dev.turtywurty.pathforged.Pathforged;
import dev.turtywurty.pathforged.config.ServerConfig;
import dev.turtywurty.pathforged.data.TransformationNode;
import dev.turtywurty.pathforged.data.TransformationTree;
import dev.turtywurty.pathforged.event.EntityStepOnBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Pathforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityStepHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void stepOnBlock(EntityStepOnBlockEvent event) {
        Entity entity = event.getEntity();
        if(entity.level.isClientSide())
            return;

        Block block = event.getState().getBlock();
        BlockPos pos = event.getPosition();

        TransformationTree transformationTree = Pathforged.getTransformationTree(entity.getLevel());
        TransformationNode nextNode = transformationTree.getNext(entity.getType(), block);

        if (nextNode != null && RANDOM.nextFloat() <= nextNode.getProbability()) {
            Block nextBlock = nextNode.getBlock();
            Pathforged.LOGGER.info("Entity {} transformed {} to {} at {}",
                    entity.getName().getString(),
                    block.getName().getString(),
                    nextBlock.getName().getString(),
                    pos);

            entity.getLevel().setBlockAndUpdate(pos, nextBlock.defaultBlockState());
        }
    }
}
