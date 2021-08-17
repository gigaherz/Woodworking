package gigaherz.woodworking;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationLootCondition implements LootItemCondition
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ResourceLocation NAME = WoodworkingMod.location("configuration");
    public static final LootItemConditionType TYPE = LootItemConditions.register(NAME.toString(), new ConfigurationLootCondition.CSerializer());

    private final String categoryName;
    private final String keyName;

    public static void init()
    {
        LOGGER.debug("ConfigurationLootCondition Init called.");
    }

    public ConfigurationLootCondition(String categoryName, String keyName)
    {
        this.categoryName = categoryName;
        this.keyName = keyName;
    }

    @Override
    public LootItemConditionType getType()
    {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        return ConfigManager.getConfigBoolean("common", categoryName, keyName);
    }

    public static class CSerializer implements Serializer<ConfigurationLootCondition>
    {
        @Override
        public void serialize(JsonObject json, ConfigurationLootCondition value, JsonSerializationContext ctx)
        {
            json.add("category", new JsonPrimitive(value.categoryName));
            json.add("key", new JsonPrimitive(value.keyName));
        }

        @Override
        public ConfigurationLootCondition deserialize(JsonObject json, JsonDeserializationContext ctx)
        {
            String categoryName = GsonHelper.getAsString(json, "category");
            String keyName = GsonHelper.getAsString(json, "key");

            return new ConfigurationLootCondition(categoryName, keyName);
        }
    }
}