package dev.turtywurty.pathforged.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.turtywurty.pathforged.Pathforged;
import dev.turtywurty.pathforged.data.TransformationTree;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.turtywurty.pathforged.data.TransformationNode.node;
import static dev.turtywurty.pathforged.data.TransformationNode.root;

public class ServerConfig {
    private static final TransformationTree DEFAULT_TREE = new TransformationTree.Builder()
            .addRoot(root(Blocks.GRASS_BLOCK)
                    .transformation(null, node(Blocks.COARSE_DIRT, 0.25f)
                            .addChild(null, node(Blocks.DIRT_PATH, 0.2f)
                                    .addChild(null, node(Blocks.GRAVEL, 0.15f)))
                            .addChild(EntityType.PIG, node(Blocks.MYCELIUM, 0.1f)
                                    .addChild(null, node(Blocks.PODZOL, 0.05f))
                                    .addChild(EntityType.COW, node(Blocks.DIRT, 0.05f))
                                    .addChild(EntityType.SHEEP, node(Blocks.MOSS_BLOCK, 0.05f))))
                    .build())
            .addRoot(root(Blocks.SAND)
                    .transformation(null, node(Blocks.RED_SAND, 0.25f)
                            .addChild(null, node(Blocks.GRAVEL, 0.15f))
                            .addChild(EntityType.HUSK, node(Blocks.SOUL_SAND, 0.1f)))
                    .transformation(EntityType.SPIDER, node(Blocks.SOUL_SAND, 0.1f)
                            .addChild(null, node(Blocks.COAL_BLOCK, 0.15f)))
                    .build())
            .build();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final LevelResource SERVER_CONFIG = new LevelResource("serverconfig");

    public static TransformationTree getTransformationTree(ServerLevel level) {
        try {
            Path serverConfig = level.getServer().getWorldPath(SERVER_CONFIG);
            Path configPath = serverConfig.resolve(Pathforged.MOD_ID).resolve("config.json");
            if(Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, GSON.toJson(DEFAULT_TREE.toJson()));
            }

            String content = Files.readString(configPath);
            if (content.isEmpty())
                return DEFAULT_TREE;

            JsonObject json = GSON.fromJson(content, JsonObject.class);

            return TransformationTree.fromJson(json);
        } catch (IOException exception) {
            Pathforged.LOGGER.error("Failed to read config file!", exception);
            return DEFAULT_TREE;
        }
    }
}
