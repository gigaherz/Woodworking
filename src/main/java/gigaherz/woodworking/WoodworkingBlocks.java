package gigaherz.woodworking;

import gigaherz.woodworking.chopblock.ChoppingBlock;
import gigaherz.woodworking.sawmill.SawmillBlock;
import gigaherz.woodworking.sawmill.SawmillTileEntity;
import gigaherz.woodworking.util.RegSitter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.Item;
import net.minecraftforge.fmllegacy.RegistryObject;

public class WoodworkingBlocks
{
    static final RegSitter HELPER = new RegSitter(WoodworkingMod.MODID);

    public static final RegistryObject<SawmillBlock> SAWMILL = HELPER
            .block("sawmill", () -> new SawmillBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.5F).sound(SoundType.STONE)))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).withBlockEntity(SawmillTileEntity::new).defer();

    public static final RegistryObject<ChoppingBlock> OAK_CHOPPING_BLOCK = HELPER
            .block("oak_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_OAK_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_OAK_CHOPPING_BLOCK = HELPER
            .block("chipped_oak_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_OAK_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_OAK_CHOPPING_BLOCK = HELPER
            .block("damaged_oak_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> BIRCH_CHOPPING_BLOCK = HELPER
            .block("birch_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_BIRCH_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_BIRCH_CHOPPING_BLOCK = HELPER
            .block("chipped_birch_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_BIRCH_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_BIRCH_CHOPPING_BLOCK = HELPER
            .block("damaged_birch_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> SPRUCE_CHOPPING_BLOCK = HELPER
            .block("spruce_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_SPRUCE_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_SPRUCE_CHOPPING_BLOCK = HELPER
            .block("chipped_spruce_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_SPRUCE_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_SPRUCE_CHOPPING_BLOCK = HELPER
            .block("damaged_spruce_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> JUNGLE_CHOPPING_BLOCK = HELPER
            .block("jungle_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_JUNGLE_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_JUNGLE_CHOPPING_BLOCK = HELPER
            .block("chipped_jungle_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_JUNGLE_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_JUNGLE_CHOPPING_BLOCK = HELPER
            .block("damaged_jungle_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DARK_OAK_CHOPPING_BLOCK = HELPER
            .block("dark_oak_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_DARK_OAK_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_DARK_OAK_CHOPPING_BLOCK = HELPER
            .block("chipped_dark_oak_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_DARK_OAK_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_DARK_OAK_CHOPPING_BLOCK = HELPER
            .block("damaged_dark_oak_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> ACACIA_CHOPPING_BLOCK = HELPER
            .block("acacia_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.CHIPPED_ACACIA_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> CHIPPED_ACACIA_CHOPPING_BLOCK = HELPER
            .block("chipped_acacia_chopping_block", () -> getChoppingBlock(WoodworkingBlocks.DAMAGED_ACACIA_CHOPPING_BLOCK))
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    public static final RegistryObject<ChoppingBlock> DAMAGED_ACACIA_CHOPPING_BLOCK = HELPER
            .block("damaged_acacia_chopping_block", WoodworkingBlocks::getChoppingBlock)
            .withItem(new Item.Properties().tab(WoodworkingMod.WOODWORKING_ITEMS)).defer();

    private static ChoppingBlock getChoppingBlock()
    {
        return new ChoppingBlock(null, defaultChopBlockProperties());
    }

    private static ChoppingBlock getChoppingBlock(RegistryObject<ChoppingBlock> breaksInto)
    {
        return new ChoppingBlock(() -> breaksInto.get().defaultBlockState(), defaultChopBlockProperties());
    }

    private static BlockBehaviour.Properties defaultChopBlockProperties()
    {
        return BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(5.0f)/*.harvestTool(ToolType.AXE).harvestLevel(0)*/;
    }
}