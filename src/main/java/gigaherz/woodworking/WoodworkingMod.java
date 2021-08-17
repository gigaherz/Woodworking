package gigaherz.woodworking;

import com.google.common.collect.ImmutableSet;
import com.sun.nio.zipfs.ZipFileSystem;
import gigaherz.woodworking.api.ChoppingRecipe;
import gigaherz.woodworking.chopblock.ChopblockMaterials;
import gigaherz.woodworking.chopblock.ChoppingBlock;
import gigaherz.woodworking.sawmill.gui.SawmillContainer;
import gigaherz.woodworking.sawmill.gui.SawmillScreen;
import gigaherz.woodworking.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public static final ItemGroup WOODWORKING_ITEMS = new ItemGroup("woodworking_items")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(WoodworkingBlocks.OAK_CHOPPING_BLOCK.get());
        }
    };

    static final RegSitter HELPER = new RegSitter(WoodworkingMod.MODID);
    private static final DeferredRegister<PointOfInterestType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, MODID);

    public static final RegistryObject<PointOfInterestType> TABLE_POI = POI_TYPES.register("woodworker",
            () -> new PointOfInterestType("woodworker", getAllChoppingBlockStates(), 1, 1)
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
            builder.addAll(PointOfInterestType.getAllStates(mat.getPristine().get()));
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

        modEventBus.addGenericListener(ContainerType.class, this::registerContainers);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
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

    private void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                withName(new ContainerType<>(SawmillContainer::new), "sawmill")
        );
    }

    private void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        CraftingHelper.register(ConfigurationCondition.Serializer.INSTANCE);
        CraftingHelper.register(ConfigToggledIngredientSerializer.NAME, ConfigToggledIngredientSerializer.INSTANCE);

        event.getRegistry().registerAll(
                new ChoppingRecipe.Serializer().setRegistryName("chopping")
        );
    }

    private void lootModifiers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
    {
        MatchBlockCondition.BLOCK_TAG_CONDITION = LootConditionManager.register("woodworking:match_block", new MatchBlockCondition.Serializer());
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
        ScreenManager.registerFactory(SawmillContainer.TYPE, SawmillScreen::new);
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