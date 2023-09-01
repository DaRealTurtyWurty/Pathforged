package dev.turtywurty.pathforged;

import com.mojang.logging.LogUtils;
import dev.turtywurty.pathforged.config.ServerConfig;
import dev.turtywurty.pathforged.data.TransformationTree;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

@Mod(Pathforged.MOD_ID)
public class Pathforged {
    public static final String MOD_ID = "pathforged";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicReference<Pair<Level, TransformationTree>> TREE = new AtomicReference<>();

    public Pathforged() {
        LOGGER.info("Pathforged is loading!");
    }

    public static TransformationTree getTransformationTree(Level level) {
        Pair<Level, TransformationTree> pair = TREE.get();
        if (pair == null || pair.getLeft().equals(level)) {
            TREE.set(Pair.of(level, ServerConfig.getTransformationTree((ServerLevel) level)));
        }

        return TREE.get().getRight();
    }

    @Mod.EventBusSubscriber(modid = Pathforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void levelLoad(LevelEvent.Load event) {
            LevelAccessor level = event.getLevel();
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
                return;

            TransformationTree tree = getTransformationTree(serverLevel);
            TREE.set(Pair.of(serverLevel, tree));
        }
    }
}
