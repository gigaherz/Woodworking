package gigaherz.woodworking;

import com.google.common.collect.ImmutableSet;
import gigaherz.woodworking.api.ChoppingRecipe;
import gigaherz.woodworking.chopblock.ChopblockMaterials;
import gigaherz.woodworking.chopblock.ChoppingBlock;
import gigaherz.woodworking.sawmill.gui.SawmillContainer;
import gigaherz.woodworking.sawmill.gui.SawmillScreen;
import gigaherz.woodworking.util.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Set;

@Mod.EventBusSubscriber
@Mod(WoodworkingMod.MODID)
public class WoodworkingMod
{
    public static final String MODID = "woodworking";

    public static WoodworkingMod instance;

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public static final CreativeModeTab WOODWORKING_ITEMS = new CreativeModeTab("woodworking_items")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(WoodworkingBlocks.OAK_CHOPPING_BLOCK.get());
        }
    };

    static final RegSitter HELPER = new RegSitter(WoodworkingMod.MODID);
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, MODID);

    public static final RegistryObject<PoiType> TABLE_POI = POI_TYPES.register("woodworker",
            () -> new PoiType("woodworker", getAllChoppingBlockStates(), 1, 1)
    );

    public static final RegistryObject<VillagerProfession> TAILOR = PROFESSIONS.register("woodworker",
            () -> new VillagerProfession("woodworker", TABLE_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(), null)
    );

    private static Set<BlockState> getAllChoppingBlockStates()
    {
        ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();
        for(ChopblockMaterials mat : ChopblockMaterials.values())
        {
            builder.addAll(PoiType.getBlockStates(mat.getPristine().get()));
        }
        return builder.build();
    }

    public WoodworkingMod()
    {
        instance = this;

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        HELPER.subscribeEvents(modEventBus);
        WoodworkingBlocks.HELPER.subscribeEvents(modEventBus);
        WoodworkingTileEntityTypes.HELPER.subscribeEvents(modEventBus);

        modEventBus.addGenericListener(MenuType.class, this::registerContainers);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(GlobalLootModifierSerializer.class, this::lootModifiers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::gatherData);

        POI_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);

        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigManager.SERVER_SPEC);
    }

    // This is its own method because I don't want WoodworkingData loaded all the time, I just need it loaded in the case where the event fires.
    public void gatherData(GatherDataEvent event)
    {
        WoodworkingData.gatherData(event);
    }

    private void registerContainers(RegistryEvent.Register<MenuType<?>> event)
    {
        event.getRegistry().registerAll(
                withName(new MenuType<>(SawmillContainer::new), "sawmill")
        );
    }

    private void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        CraftingHelper.register(ConfigurationCondition.Serializer.INSTANCE);
        CraftingHelper.register(ConfigToggledIngredientSerializer.NAME, ConfigToggledIngredientSerializer.INSTANCE);

        event.getRegistry().registerAll(
                new ChoppingRecipe.Serializer().setRegistryName("chopping")
        );
    }

    private void lootModifiers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
    {
        MatchBlockCondition.BLOCK_TAG_CONDITION = LootItemConditions.register("woodworking:match_block", new MatchBlockCondition.CSerializer());
        event.getRegistry().registerAll(
                new AppendLootTable.Serializer().setRegistryName(location("append_loot")),
                new ReplaceDrops.Serializer().setRegistryName(location("replace_drops")),
                new LootContainsWrapper.Serializer().setRegistryName(location("loot_contains"))
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(ConfigurationLootCondition::init);
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(SawmillContainer.TYPE, SawmillScreen::new);
    }

    private static <R extends T, T extends IForgeRegistryEntry<T>> R withName(R obj, ResourceLocation name)
    {
        obj.setRegistryName(name);
        return obj;
    }

    private static <R extends T, T extends IForgeRegistryEntry<T>> R withName(R obj, String name)
    {
        return withName(obj, new ResourceLocation(MODID, name));
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}