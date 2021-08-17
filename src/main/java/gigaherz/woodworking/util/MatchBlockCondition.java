package gigaherz.woodworking.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MatchBlockCondition implements LootItemCondition
{
    public static LootItemConditionType BLOCK_TAG_CONDITION;

    @Nullable
    final List<Block> blockList;
    @Nullable
    final Tag.Named<Block> blockTag;

    public MatchBlockCondition(@Nullable List<Block> blockList, @Nullable Tag.Named<Block> blockTag)
    {
        this.blockList = blockList;
        this.blockTag = blockTag;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        BlockState state = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null)
            return false;
        if (blockTag != null)
            return blockTag.contains(state.getBlock());
        if (blockList != null)
            return blockList.contains(state.getBlock());
        return false;
    }

    @Override
    public LootItemConditionType getType()
    {
        return BLOCK_TAG_CONDITION;
    }

    public static class CSerializer implements Serializer<MatchBlockCondition>
    {
        @Override
        public void serialize(JsonObject json, MatchBlockCondition value, JsonSerializationContext context)
        {
            if (value.blockTag != null)
                json.addProperty("tag", value.blockTag.getName().toString());
        }

        @Override
        public MatchBlockCondition deserialize(JsonObject json, JsonDeserializationContext context)
        {
            if (json.has("tag"))
            {
                ResourceLocation tagName = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                return new MatchBlockCondition(null, BlockTags.createOptional(tagName));
            }
            else if(json.has("blocks"))
            {
                List<Block> blockNames = Lists.newArrayList();
                for(JsonElement e : GsonHelper.getAsJsonArray(json, "blocks"))
                {
                    ResourceLocation blockName = new ResourceLocation(e.getAsString());
                    blockNames.add(ForgeRegistries.BLOCKS.getValue(blockName));
                }
                return new MatchBlockCondition(blockNames, null);
            }
            else if(json.has("block"))
            {
                ResourceLocation blockName = new ResourceLocation(GsonHelper.getAsString(json, "block"));
                return new MatchBlockCondition(Collections.singletonList(ForgeRegistries.BLOCKS.getValue(blockName)), null);
            }
            throw new RuntimeException("match_block must have one of 'tag', 'block' or 'blocks' key");
        }
    }
}