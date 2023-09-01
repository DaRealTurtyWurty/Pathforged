package dev.turtywurty.pathforged.handler;

import dev.turtywurty.pathforged.Pathforged;
import dev.turtywurty.pathforged.config.ServerConfig;
import dev.turtywurty.pathforged.data.TransformationNode;
import dev.turtywurty.pathforged.data.TransformationTree;
import dev.turtywurty.pathforged.event.EntityStepOnBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.stream.StreamSupport;

@Mod.EventBusSubscriber(modid = Pathforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityStepHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void stepOnBlock(EntityStepOnBlockEvent event) {
        Entity entity = event.getEntity();
        if(entity.level.isClientSide())
            return;
        if(entity instanceof Player player && player.isCreative() && ServerConfig.preventInCreative())
            return;
        if(ServerConfig.leatherBootsPreventTransformation() &&
                StreamSupport.stream(entity.getArmorSlots().spliterator(), false)
                        .anyMatch(stack -> stack.is(Items.LEATHER_BOOTS)))
            return;

        if(!ServerConfig.allowWater() && entity.isInWater())
            return;
        if(!ServerConfig.allowLava() && entity.isInLava())
            return;

        Block block = event.getState().getBlock();
        BlockPos pos = event.getPosition();

        TransformationTree transformationTree = Pathforged.getTransformationTree(entity.getLevel());
        TransformationNode nextNode = transformationTree.getNext(entity.getType(), block);
        if (nextNode == null)
            return;

        double probability = nextNode.getProbability();
        double underwaterProbabilityDecrease = ServerConfig.underwaterProbabilityDecrease();
        boolean shouldTransform = RANDOM.nextDouble() < (entity.isInWater() ? probability - underwaterProbabilityDecrease : probability);

        if (shouldTransform) {
            Block nextBlock = nextNode.getBlock();
            if (ServerConfig.logTransformation())
                Pathforged.LOGGER.info("Entity {} transformed {} to {} at {}",
                        entity.getName().getString(),
                        block.getName().getString(),
                        nextBlock.getName().getString(),
                        pos);

            entity.getLevel().setBlockAndUpdate(pos, nextBlock.defaultBlockState());
        }
    }
}
