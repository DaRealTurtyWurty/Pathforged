package dev.turtywurty.pathforged.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TransformationTree {
    private final NonNullList<TransformationNode.RootNode> roots;

    public TransformationTree(NonNullList<TransformationNode.RootNode> roots) {
        this.roots = roots;
    }

    public List<TransformationNode.RootNode> getRoots() {
        return List.copyOf(this.roots);
    }

    public @NotNull TransformationNode.RootNode getRoot(int index) {
        return this.roots.get(index);
    }

    public int getRootCount() {
        return this.roots.size();
    }

    public @Nullable TransformationNode.RootNode getRoot(Block block) {
        for (TransformationNode.RootNode root : this.roots) {
            if (root.getBlock() == block) {
                return root;
            }
        }

        return null;
    }

    public @Nullable TransformationNode getNext(EntityType<?> entityType, Block block) {
        TransformationNode.RootNode root = getRoot(block);

        if (root != null) {
            return root.getChild(entityType);
        }

        // loop through all roots and check if any of them have the block as a child
        for (TransformationNode.RootNode rootNode : this.roots) {
            TransformationNode next = rootNode.getNext(entityType, block);
            if (next != null) {
                return next;
            }
        }

        return null;
    }

    public static class Builder {
        private final NonNullList<TransformationNode.RootNode> roots = NonNullList.create();

        public Builder addRoot(TransformationNode.RootNode root) {
            this.roots.add(root);
            return this;
        }

        public TransformationTree build() {
            if (this.roots.isEmpty())
                throw new IllegalStateException("Cannot build a TransformationTree with no roots!");

            return new TransformationTree(this.roots);
        }
    }

    public static TransformationTree fromJson(JsonObject json) {
        JsonArray rootNodes = json.getAsJsonArray("roots");
        NonNullList<TransformationNode.RootNode> rootNodeList = NonNullList.create();
        for (JsonElement rootNode : rootNodes) {
            rootNodeList.add(TransformationNode.RootNode.Builder.fromJson(rootNode.getAsJsonObject()));
        }

        return new TransformationTree(rootNodeList);
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        JsonArray rootNodes = new JsonArray();
        for (TransformationNode.RootNode rootNode : this.roots) {
            rootNodes.add(rootNode.toJson());
        }

        json.add("roots", rootNodes);
        return json;
    }

    public static Block getBlockByName(String name) {
        return Objects.equals(name, "null") ? null : ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
    }

    public static EntityType<?> getEntityByName(String name) {
        return Objects.equals(name, "null") ? null : ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(name));
    }

    public static String getBlockName(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key == null || block == null ? "null" : key.toString();
    }

    public static String getEntityName(EntityType<?> entityType) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return key == null || entityType == null ? "null" : key.toString();
    }
}
