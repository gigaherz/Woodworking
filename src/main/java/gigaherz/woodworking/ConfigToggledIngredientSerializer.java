package gigaherz.woodworking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class ConfigToggledIngredientSerializer implements IIngredientSerializer<Ingredient>
{
    public static ResourceLocation NAME = WoodworkingMod.location("config_toggled_ingredient");
    public static ConfigToggledIngredientSerializer INSTANCE = new ConfigToggledIngredientSerializer();

    @Override
    public Ingredient parse(FriendlyByteBuf buffer)
    {
        return Ingredient.EMPTY;
    }

    @Override
    public Ingredient parse(JsonObject json)
    {
        String categoryName = GsonHelper.getAsString(json, "category");
        String keyName = GsonHelper.getAsString(json, "key");

        return new ConfigToggledIngredient(
                categoryName, keyName,
                CraftingHelper.getIngredient(json.getAsJsonObject("then")),
                CraftingHelper.getIngredient(json.getAsJsonObject("else"))
        );
    }

    @Override
    public void write(FriendlyByteBuf buffer, Ingredient ingredient)
    {
        // Not used.
    }

    public static class ConfigToggledIngredient extends Ingredient
    {
        private final String categoryName;
        private final String keyName;
        private final Ingredient then;
        private final Ingredient other;

        protected ConfigToggledIngredient(String categoryName, String keyName, Ingredient then, Ingredient other)
        {
            super(Stream.empty());
            this.categoryName = categoryName;
            this.keyName = keyName;
            this.then = then;
            this.other = other;
        }

        protected boolean getConfigValue()
        {
            return ConfigManager.getConfigBoolean("common", categoryName, keyName);
        }

        @Override
        public ItemStack[] getItems()
        {
            return getConfigValue() ? then.getItems() : other.getItems();
        }

        @Override
        public boolean test(@Nullable ItemStack stack)
        {
            return getConfigValue() ? then.test(stack) : other.test(stack);
        }

        @Override
        public IntList getStackingIds()
        {
            return getConfigValue() ? then.getStackingIds() : other.getStackingIds();
        }

        @Override
        public boolean isEmpty()
        {
            return getConfigValue() ? then.isEmpty() : other.isEmpty();
        }

        private static Method invalidateMethod = ObfuscationReflectionHelper.findMethod(Ingredient.class, "invalidate");

        @Override
        protected void invalidate()
        {
            try
            {
                invalidateMethod.invoke(then);
                invalidateMethod.invoke(other);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isSimple()
        {
            return getConfigValue() ? then.isSimple() : other.isSimple();
        }

        @Override
        public IIngredientSerializer<? extends Ingredient> getSerializer()
        {
            return INSTANCE;
        }

        @Override
        public JsonElement toJson()
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", NAME.toString());
            obj.addProperty("category", categoryName);
            obj.addProperty("key", keyName);
            obj.add("then", then.toJson());
            obj.add("else", other.toJson());
            return obj;
        }
    }
}