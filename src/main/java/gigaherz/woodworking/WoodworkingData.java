package gigaherz.woodworking;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import gigaherz.woodworking.chopblock.ChopblockMaterials;
import gigaherz.woodworking.chopblock.ChoppingBlock;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

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
            gen.addProvider(new LootGen(gen));
        }
    }

    public static Tag.Named<Item> makeItemTag(String id)
    {
        return makeItemTag(new ResourceLocation(id));
    }

    public static Tag.Named<Item> makeItemTag(ResourceLocation id)
    {
        return net.minecraft.tags.ItemTags.bind(id.toString());
    }

    private static class Recipes extends RecipeProvider implements DataProvider, IConditionBuilder
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
        {
            Arrays.stream(ChopblockMaterials.values())
                    .forEach(log -> {
                        Tag<Item> tag = makeItemTag(log.getMadeFrom());
                        ShapedRecipeBuilder.shaped(log.getPristine().get())
                                .pattern("ll")
                                .define('l', tag)
                                .unlockedBy("has_log", has(tag))
                                .save(consumer);
                    });

            ShapedRecipeBuilder.shaped(WoodworkingBlocks.SAWMILL.get())
                    .pattern("ddd")
                    .pattern("iii")
                    .pattern("ccc")
                    .define('d', Tags.Items.GEMS_DIAMOND)
                    .define('i', Tags.Items.INGOTS_IRON)
                    .define('c', net.minecraft.tags.ItemTags.STONE_CRAFTING_MATERIALS)
                    .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                    .save(consumer);
        }
    }

    private static class ItemTags extends ItemTagsProvider implements DataProvider
    {
        public ItemTags(DataGenerator gen, BlockTags blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            this.tag(makeItemTag(WoodworkingMod.location("chopping_blocks")))
                    .add(Arrays.stream(ChopblockMaterials.values())
                            .flatMap(block -> Stream.of(block.getPristine(), block.getChipped(), block.getDamaged()).map(reg -> reg.get().asItem()))
                            .toArray(Item[]::new));


        }
    }

    private static class BlockTags extends BlockTagsProvider implements DataProvider
    {
        public BlockTags(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            this.tag(net.minecraft.tags.BlockTags.bind(WoodworkingMod.location("chopping_blocks").toString()))
                    .add(Arrays.stream(ChopblockMaterials.values())
                            .flatMap(block -> Stream.of(block.getPristine(), block.getChipped(), block.getDamaged()).map(Supplier::get))
                            .toArray(Block[]::new));

            this.tag(net.minecraft.tags.BlockTags.bind("minecraft:mineable/axe"))
                    .add(Arrays.stream(ChopblockMaterials.values())
                            .flatMap(block -> Stream.of(block.getPristine(), block.getChipped(), block.getDamaged()).map(Supplier::get))
                            .toArray(Block[]::new));
        }
    }

    private static class LootGen extends LootTableProvider implements DataProvider
    {
        public LootGen(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootContextParamSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLoot
        {
            @Override
            protected void addTables()
            {
                this.dropSelf(WoodworkingBlocks.SAWMILL.get());

                for(ChopblockMaterials mat : ChopblockMaterials.values())
                {
                    this.dropSelf(mat.getPristine().get());
                    this.dropSelf(mat.getChipped().get());
                    this.dropSelf(mat.getDamaged().get());
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