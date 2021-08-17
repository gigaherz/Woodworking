package gigaherz.woodworking;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import gigaherz.woodworking.chopblock.ChopblockMaterials;
import gigaherz.woodworking.chopblock.ChoppingBlock;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.*;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gigaherz.woodworking.WoodworkingMod.MODID;

public class WoodworkingData
{
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if (event.includeClient())
        {
            gen.addProvider(new Lang(gen));
            //gen.addProvider(new ItemModels(gen, event.getExistingFileHelper()));
            //gen.addProvider(new BlockStates(gen, event.getExistingFileHelper()));
        }
        if (event.includeServer())
        {

            BlockTags blockTags = new BlockTags(gen, event.getExistingFileHelper());
            gen.addProvider(blockTags);
            gen.addProvider(new ItemTags(gen, blockTags, event.getExistingFileHelper()));
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new LootTables(gen));
        }
    }

    public static ITag.INamedTag<Item> makeItemTag(String id)
    {
        return makeItemTag(new ResourceLocation(id));
    }

    public static ITag.INamedTag<Item> makeItemTag(ResourceLocation id)
    {
        return net.minecraft.tags.ItemTags.makeWrapperTag(id.toString());
    }

    private static class Recipes extends RecipeProvider implements IDataProvider, IConditionBuilder
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
        {
            Arrays.stream(ChopblockMaterials.values())
                    .forEach(log -> {
                        ITag<Item> tag = makeItemTag(log.getMadeFrom());
                        ShapedRecipeBuilder.shapedRecipe(log.getPristine().get())
                                .patternLine("ll")
                                .key('l', tag)
                                .addCriterion("has_log", hasItem(tag))
                                .build(consumer);
                    });
        }
    }

    private static class ItemTags extends ItemTagsProvider implements IDataProvider
    {
        public ItemTags(DataGenerator gen, BlockTags blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, MODID, existingFileHelper);
        }

        @Override
        protected void registerTags()
        {
            this.getOrCreateBuilder(makeItemTag(WoodworkingMod.location("chopping_blocks")))
                    .add(Arrays.stream(ChopblockMaterials.values())
                            .flatMap(block -> Stream.of(block.getPristine(), block.getChipped(), block.getDamaged()).map(reg -> reg.get().asItem()))
                            .toArray(Item[]::new));


        }
    }

    private static class BlockTags extends BlockTagsProvider implements IDataProvider
    {
        public BlockTags(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, MODID, existingFileHelper);
        }

        @Override
        protected void registerTags()
        {
            this.getOrCreateBuilder(net.minecraft.tags.BlockTags.makeWrapperTag(WoodworkingMod.location("chopping_blocks").toString()))
                    .add(Arrays.stream(ChopblockMaterials.values())
                            .flatMap(block -> Stream.of(block.getPristine(), block.getChipped(), block.getDamaged()).map(Supplier::get))
                            .toArray(Block[]::new));
        }
    }

    private static class LootTables extends LootTableProvider implements IDataProvider
    {
        public LootTables(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootParameterSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTableManager.validateLootTable(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLootTables
        {
            @Override
            protected void addTables()
            {
                this.registerDropSelfLootTable(WoodworkingBlocks.SAWMILL.get());

                for(ChopblockMaterials mat : ChopblockMaterials.values())
                {
                    this.registerDropSelfLootTable(mat.getPristine().get());
                    this.registerDropSelfLootTable(mat.getChipped().get());
                    this.registerDropSelfLootTable(mat.getDamaged().get());
                }
            }

            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(b -> b.getRegistryName().getNamespace().equals(MODID))
                        .collect(Collectors.toList());
            }
        }
    }

    private static class Lang extends LanguageProvider
    {
        public Lang(DataGenerator gen)
        {
            super(gen, WoodworkingMod.MODID, "en_us");
        }

        @Override
        protected void addTranslations()
        {
            add("itemGroup.woodworking_items", "Woodworking");
            add("text.woodworking.jei.category.chopping", "Chopping Block");

            add(WoodworkingBlocks.OAK_CHOPPING_BLOCK.get(), "Oak Chopping Block");
            add(WoodworkingBlocks.CHIPPED_OAK_CHOPPING_BLOCK.get(), "Used Oak Chopping Block");
            add(WoodworkingBlocks.DAMAGED_OAK_CHOPPING_BLOCK.get(), "Weathered Oak Chopping Block");

            add(WoodworkingBlocks.BIRCH_CHOPPING_BLOCK.get(), "Birch Chopping Block");
            add(WoodworkingBlocks.CHIPPED_BIRCH_CHOPPING_BLOCK.get(), "Used Birch Chopping Block");
            add(WoodworkingBlocks.DAMAGED_BIRCH_CHOPPING_BLOCK.get(), "Weathered Birch Chopping Block");

            add(WoodworkingBlocks.SPRUCE_CHOPPING_BLOCK.get(), "Spruce Chopping Block");
            add(WoodworkingBlocks.CHIPPED_SPRUCE_CHOPPING_BLOCK.get(), "Used Spruce Chopping Block");
            add(WoodworkingBlocks.DAMAGED_SPRUCE_CHOPPING_BLOCK.get(), "Weathered Spruce Chopping Block");

            add(WoodworkingBlocks.JUNGLE_CHOPPING_BLOCK.get(), "Jungle Chopping Block");
            add(WoodworkingBlocks.CHIPPED_JUNGLE_CHOPPING_BLOCK.get(), "Used Jungle Chopping Block");
            add(WoodworkingBlocks.DAMAGED_JUNGLE_CHOPPING_BLOCK.get(), "Weathered Jungle Chopping Block");

            add(WoodworkingBlocks.DARK_OAK_CHOPPING_BLOCK.get(), "Dark Oak Chopping Block");
            add(WoodworkingBlocks.CHIPPED_DARK_OAK_CHOPPING_BLOCK.get(), "Used Dark Oak Chopping Block");
            add(WoodworkingBlocks.DAMAGED_DARK_OAK_CHOPPING_BLOCK.get(), "Weathered Dark Oak Chopping Block");

            add(WoodworkingBlocks.ACACIA_CHOPPING_BLOCK.get(), "Acacia Chopping Block");
            add(WoodworkingBlocks.CHIPPED_ACACIA_CHOPPING_BLOCK.get(), "Used Acacia Chopping Block");
            add(WoodworkingBlocks.DAMAGED_ACACIA_CHOPPING_BLOCK.get(), "Weathered Acacia Chopping Block");

            add(WoodworkingBlocks.SAWMILL.get(), "Sawmill");

            add("container.sawmill.title", "Sawmill");
        }
    }
}