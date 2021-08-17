package gigaherz.woodworking.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gigaherz.woodworking.ConfigManager;
import gigaherz.woodworking.WoodworkingMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class ChoppingRecipe implements Recipe<ChoppingContext>
{
    @ObjectHolder("woodworking:chopping")
    public static RecipeSerializer<?> SERIALIZER = null;

    public static RecipeType<ChoppingRecipe> CHOPPING = RecipeType.register(WoodworkingMod.location("chopping").toString());

    public static Optional<ChoppingRecipe> getRecipe(Level world, @Nullable BlockPos pos, ItemStack stack)
    {
        return getRecipe(world, new ChoppingContext(new SingletonInventory(stack), null, pos != null ? () -> Vec3.atCenterOf(pos) : null, null, 0, null));
    }

    public static Optional<ChoppingRecipe> getRecipe(Level world, ChoppingContext ctx)
    {
        return world.getRecipeManager().getRecipeFor(CHOPPING, ctx, world);
    }

    public static Collection<ChoppingRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(CHOPPING);
    }

    private final ResourceLocation id;
    private final String group;
    private final Ingredient input;
    private final ItemStack output;
    private final double outputMultiplier;
    private final double hitCountMultiplier;
    private final int maxOutput;
    private final int sawingTime;

    public ChoppingRecipe(ResourceLocation id, String group, Ingredient input, ItemStack output, double outputMultiplier, double hitCountMultiplier, int maxOutput,
                          int sawingTime)
    {
        this.id = id;
        this.group = group;
        this.input = input;
        this.output = output;
        this.outputMultiplier = outputMultiplier;
        this.hitCountMultiplier = hitCountMultiplier;
        this.maxOutput = maxOutput;
        this.sawingTime = sawingTime;
    }

    public ItemStack getOutput()
    {
        return output;
    }

    public double getOutputMultiplier()
    {
        return outputMultiplier;
    }

    public double getHitCountMultiplier()
    {
        return hitCountMultiplier;
    }

    public int getSawingTime()
    {
        return sawingTime;
    }

    public int getMaxOutput()
    {
        return maxOutput;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    @Override
    public boolean matches(ChoppingContext inv, Level worldIn)
    {
        return input.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(ChoppingContext inv)
    {
        return inv.getPlayer() != null
                ? getResults(inv.getItem(0), inv.getPlayer(), inv.getAxeLevel(), inv.getFortune(), inv.getRandom())
                : getResultsSawmill();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem()
    {
        return output;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public String getGroup()
    {
        return group;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return CHOPPING;
    }

    private ItemStack getResults(ItemStack input, @Nullable Player player, @Nullable Tier axeLevel, int fortune, Random random)
    {
        double number = getOutputMultiplier(axeLevel) * (1 + random.nextFloat() * fortune);

        int whole = (int) Math.floor(number);
        double remainder = number - whole;

        if (random.nextFloat() < remainder)
        {
            whole++;
        }

        if (getMaxOutput() > 0)
            whole = Math.min(whole, getMaxOutput());

        if (whole > 0)
        {
            ItemStack out = getOutput().copy();
            out.setCount(whole);
            return out;
        }

        return ItemStack.EMPTY;
    }

    public double getOutputMultiplier(@Nullable Tier axeLevel)
    {
        double number = ConfigManager.SERVER.choppingWithEmptyHand.get() * getOutputMultiplier();

        if (axeLevel != null && axeLevel.getLevel() >= 0)
            number = Math.max(0, getOutputMultiplier() * ConfigManager.getAxeLevelMultiplier(axeLevel));
        return number;
    }

    private ItemStack getResultsSawmill()
    {
        double number = Math.max(0, getOutputMultiplier() * 4);

        int whole = (int) Math.floor(number);

        if (getMaxOutput() > 0)
            whole = Math.min(whole, getMaxOutput());

        if (whole > 0)
        {
            ItemStack out = getOutput().copy();
            out.setCount(whole);
            return out;
        }

        return ItemStack.EMPTY;
    }

    public double getHitProgress(@Nullable Tier axeLevel)
    {
        return 25 + getHitCountMultiplier() * 25 * Math.max(0, axeLevel != null ? axeLevel.getLevel() : 0);
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<ChoppingRecipe>
    {
        @Override
        public ChoppingRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            JsonElement jsonelement = GsonHelper.isArrayNode(json, "ingredient")
                    ? GsonHelper.getAsJsonArray(json, "ingredient")
                    : GsonHelper.getAsJsonObject(json, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(jsonelement);
            String s1 = GsonHelper.getAsString(json, "result");
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            ItemStack itemstack = new ItemStack(Optional.ofNullable(ForgeRegistries.ITEMS.getValue(resourcelocation)).orElseThrow(() -> new IllegalStateException("Item: " + s1 + " does not exist")));
            double outputMultiplier = GsonHelper.getAsFloat(json, "output_multiplier", 1.0f);
            double hitCountMultiplier = GsonHelper.getAsFloat(json, "hit_count_multiplier", 1.0f);
            int maxOutput = GsonHelper.getAsInt(json, "max_output", 0);
            int sawingTime = GsonHelper.getAsInt(json, "sawing_time", 200);
            return new ChoppingRecipe(recipeId, group, ingredient, itemstack, outputMultiplier, hitCountMultiplier, maxOutput, sawingTime);
        }

        @Override
        public ChoppingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {

            String group = buffer.readUtf(32767);
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack itemstack = buffer.readItem();
            double outputMultiplier = buffer.readDouble();
            double hitCountMultiplier = buffer.readDouble();
            int maxOutput = buffer.readVarInt();
            int sawingTime = buffer.readVarInt();
            return new ChoppingRecipe(recipeId, group, ingredient, itemstack, outputMultiplier, hitCountMultiplier, maxOutput, sawingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ChoppingRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeDouble(recipe.outputMultiplier);
            buffer.writeDouble(recipe.hitCountMultiplier);
            buffer.writeVarInt(recipe.maxOutput);
            buffer.writeVarInt(recipe.sawingTime);
        }
    }
}