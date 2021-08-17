package gigaherz.woodworking.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class LootContainsWrapper extends LootModifier
{
    private final IGlobalLootModifier childModifier;
    private final Ingredient itemMatcher;

    public LootContainsWrapper(LootItemCondition[] lootConditions, IGlobalLootModifier childModifier, Ingredient itemMatcher)
    {
        super(lootConditions);
        this.childModifier = childModifier;
        this.itemMatcher = itemMatcher;
    }

    @Nonnull
    @Override
    public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        if (generatedLoot.stream().noneMatch(itemMatcher))
            return generatedLoot;

        return childModifier.apply(generatedLoot, context);
    }

    public static class Serializer extends GlobalLootModifierSerializer<LootContainsWrapper>
    {
        @Override
        public LootContainsWrapper read(ResourceLocation location, JsonObject object, LootItemCondition[] contidions)
        {
            IGlobalLootModifier child = deserializeModifier(location, GsonHelper.getAsJsonObject(object, "modifier"));
            Ingredient itemMatcher = CraftingHelper.getIngredient(GsonHelper.getAsJsonObject(object, "matching"));
            return new LootContainsWrapper(contidions, child, itemMatcher);
        }

        @Override
        public JsonObject write(LootContainsWrapper instance)
        {
            JsonObject object = new JsonObject();
            object.add("matching", instance.itemMatcher.toJson());

            //LootModifierManager.getSerializerForName(instance.childModifier)
            //object.add("modifier", instance.childModifier.);
            //return object;

            throw new RuntimeException("Not implemented.");
        }

        private static final Gson GSON_INSTANCE = new GsonBuilder()
                .registerTypeHierarchyAdapter(LootItemFunction.class, LootItemFunctions.createGsonAdapter())
                .registerTypeHierarchyAdapter(LootItemCondition.class, LootItemConditions.createGsonAdapter())
                .create();
        private IGlobalLootModifier deserializeModifier(ResourceLocation location, JsonElement element) {
            JsonObject object = element.getAsJsonObject();
            LootItemCondition[] lootConditions = GSON_INSTANCE.fromJson(object.get("conditions"), LootItemCondition[].class);

            // For backward compatibility with the initial implementation, fall back to using the location as the type.
            // TODO: Remove fallback in 1.16
            ResourceLocation serializer = location;
            if (object.has("type"))
            {
                serializer = new ResourceLocation(GsonHelper.getAsString(object, "type"));
            }

            return ForgeRegistries.LOOT_MODIFIER_SERIALIZERS.getValue(serializer).read(location, object, lootConditions);
        }
    }
}