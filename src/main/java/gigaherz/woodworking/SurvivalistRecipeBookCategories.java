package gigaherz.woodworking;
/*
import gigaherz.woodworking.api.ChoppingRecipe;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.client.RecipebookCategoryManager;
import net.minecraftforge.common.util.NonNullLazy;

public class WoodworkingRecipeBookCategories
{
    private static final NonNullLazy<WoodworkingRecipeBookCategories> INSTANCE = NonNullLazy.of(WoodworkingRecipeBookCategories::new);

    public static WoodworkingRecipeBookCategories instance()
    {
        return INSTANCE.get();
    }

    public final RecipeBookCategories SAWMILL_SEARCH = RecipeBookCategories.create("SAWMILL_SEARCH",  new ItemStack(Items.COMPASS));
    public final RecipeBookCategories SAWMILL = RecipeBookCategories.create("SAWMILL", new ItemStack(Items.OAK_PLANKS));

    public WoodworkingRecipeBookCategories()
    {
        RecipebookCategoryManager.setRecipebookCategoriesForSearch(SAWMILL_SEARCH, SAWMILL);
        RecipebookCategoryManager.setRecipeCategoryMapper(ChoppingRecipe.CHOPPING, (recipe) -> SAWMILL);
    }
}*/