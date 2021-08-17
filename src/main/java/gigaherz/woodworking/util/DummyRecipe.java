package gigaherz.woodworking.util;

import com.google.gson.JsonObject;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DummyRecipe implements CraftingRecipe
{
    final ResourceLocation id;

    public DummyRecipe(ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return false;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return null;
    }

    @Override
    public RecipeType<?> getType()
    {
        return null;
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<DummyRecipe>
    {
        @Override
        public DummyRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            return new DummyRecipe(recipeId);
        }

        @Override
        public DummyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            return new DummyRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DummyRecipe recipe)
        {
            // nothing to write
        }
    }
}