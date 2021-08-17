package gigaherz.woodworking.integration;

import gigaherz.woodworking.WoodworkingBlocks;
import gigaherz.woodworking.WoodworkingMod;
import gigaherz.woodworking.api.ChoppingRecipe;
import gigaherz.woodworking.chopblock.ChopblockMaterials;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.stream.Stream;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final ResourceLocation ID = WoodworkingMod.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        Stream.of(ChopblockMaterials.values()).forEach(v -> {
            registration.addRecipeCatalyst(new ItemStack(v.getPristine().get()), ChoppingCategory.UID);
        });
        registration.addRecipeCatalyst(new ItemStack(WoodworkingBlocks.SAWMILL.get()), ChoppingCategory.UID);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        registry.addRecipeCategories(new ChoppingCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        ClientWorld world = Objects.requireNonNull(Minecraft.getInstance().world);
        registration.addRecipes(ChoppingRecipe.getAllRecipes(world), ChoppingCategory.UID);
    }
}