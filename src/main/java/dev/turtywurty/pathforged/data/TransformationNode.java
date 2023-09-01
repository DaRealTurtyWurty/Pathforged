package dev.turtywurty.pathforged.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

import static dev.turtywurty.pathforged.data.TransformationTree.*;

public class TransformationNode {
    final Block block;
    final float probability;
    final Map<EntityType<?>, TransformationNode> children = new HashMap<>();

    public TransformationNode(Block block, float probability) {
        this.block = block;
        this.probability = probability;
    }

    public TransformationNode(Block block, float probability, Map<EntityType<?>, TransformationNode> children) {
        this(block, probability);
        this.children.putAll(children);
    }

    public Block getBlock() {
        return this.block;
    }

    public float getProbability() {
        return this.probability;
    }

    public Map<EntityType<?>, TransformationNode> getChildren() {
        return Map.copyOf(this.children);
    }

    public TransformationNode getChild(EntityType<?> entityType) {
        return this.children.containsKey(entityType) ? this.children.get(entityType) : this.children.get(null);
    }

    // Recursively get the next node
    public TransformationNode getNext(EntityType<?> entityType, Block block) {
        if (this.block == block) {
            return getChild(entityType);
        }

        for (TransformationNode child : this.children.values()) {
            TransformationNode next = child.getNext(entityType, block);

            if (next != null) {
                return next;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "TransformationNode{" +
                "block=" + block +
                ", probability=" + probability +
                ", children=" + children +
                '}';
    }

    public JsonObject toJson() {
        var json = new JsonObject();
        json.addProperty("block", getBlockName(this.block));
        json.addProperty("probability", this.probability);

        var children = new JsonObject();
        for (Map.Entry<EntityType<?>, TransformationNode> entry : this.children.entrySet()) {
            children.add(getEntityName(entry.getKey()), entry.getValue().toJson());
        }

        json.add("children", children);
        return json;
    }

    public static class Builder {
        final Block block;
        final float probability;
        final Map<EntityType<?>, TransformationNode> children = new HashMap<>();

        public Builder(Block block, float probability) {
            this.block = block;
            this.probability = probability;
        }

        public Builder addChild(EntityType<?> entityType, TransformationNode.Builder child) {
            this.children.put(entityType, child.build());
            return this;
        }

        public TransformationNode build() {
            return new TransformationNode(this.block, this.probability, this.children);
        }

        private static TransformationNode.Builder fromJson(JsonObject obj) {
            String blockStr = obj.get("block").getAsString();
            float probability = obj.get("probability").getAsFloat();
            JsonObject children = obj.getAsJsonObject("children");

            Block block = getBlockByName(blockStr);
            TransformationNode.Builder builder = TransformationNode.node(block, probability);
            for (Map.Entry<String, JsonElement> entry : children.entrySet()) {
                String entityStr = entry.getKey();
                JsonObject child = entry.getValue().getAsJsonObject();

                EntityType<?> entityType = getEntityByName(entityStr);
                builder.addChild(entityType, fromJson(child));
            }

            return builder;
        }
    }

    public static RootNode.Builder root(Block block) {
        return new RootNode.Builder(block);
    }

    public static TransformationNode.Builder node(Block block, float probability) {
        return new TransformationNode.Builder(block, probability);
    }

    public static class RootNode extends TransformationNode {
        private RootNode(Block block) {
            super(block, 1);
        }

        @Override
        public JsonObject toJson() {
            var json = new JsonObject();

            json.addProperty("block", getBlockName(this.block));

            var children = new JsonObject();
            for (Map.Entry<EntityType<?>, TransformationNode> entry : this.children.entrySet()) {
                children.add(getEntityName(entry.getKey()), entry.getValue().toJson());
            }

            json.add("children", children);

            return json;
        }

        public static class Builder {
            private final Block block;
            private final Map<EntityType<?>, TransformationNode> children = new HashMap<>();

            public Builder(Block block) {
                this.block = block;
            }

            public Builder transformation(EntityType<?> entityType, TransformationNode.Builder child) {
                this.children.put(entityType, child.build());
                return this;
            }

            public RootNode build() {
                var root = new RootNode(this.block);
                root.children.putAll(this.children);
                return root;
            }

            public static TransformationNode.RootNode fromJson(JsonObject obj) {
                String blockStr = obj.get("block").getAsString();
                JsonObject children = obj.getAsJsonObject("children");

                Block block = getBlockByName(blockStr);
                TransformationNode.RootNode.Builder builder = TransformationNode.root(block);

                for (Map.Entry<String, JsonElement> entry : children.entrySet()) {
                    String entityStr = entry.getKey();
                    JsonObject child = entry.getValue().getAsJsonObject();

                    EntityType<?> entityType = getEntityByName(entityStr);
                    builder.transformation(entityType, TransformationNode.Builder.fromJson(child));
                }

                return builder.build();
            }
        }
    }
}
