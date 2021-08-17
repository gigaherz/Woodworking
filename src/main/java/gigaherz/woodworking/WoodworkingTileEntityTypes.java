package gigaherz.woodworking;

import gigaherz.woodworking.chopblock.ChoppingBlockTileEntity;
import gigaherz.woodworking.sawmill.SawmillTileEntity;
import gigaherz.woodworking.util.RegSitter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;

public class WoodworkingTileEntityTypes
{
    static final RegSitter HELPER = new RegSitter(WoodworkingMod.MODID);

    public static final RegistryObject<BlockEntityType<SawmillTileEntity>> SAWMILL_RACK_TILE_ENTITY_TYPE = HELPER.tileEntity("sawmill");

    public static final RegistryObject<BlockEntityType<ChoppingBlockTileEntity>> CHOPPING_BLOCK_TILE_ENTITY_TYPE = HELPER.tileEntity("chopping_block", ChoppingBlockTileEntity::new,
            WoodworkingBlocks.OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.BIRCH_CHOPPING_BLOCK,
            WoodworkingBlocks.SPRUCE_CHOPPING_BLOCK,
            WoodworkingBlocks.JUNGLE_CHOPPING_BLOCK,
            WoodworkingBlocks.DARK_OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.ACACIA_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_BIRCH_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_SPRUCE_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_JUNGLE_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_DARK_OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.CHIPPED_ACACIA_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_BIRCH_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_SPRUCE_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_JUNGLE_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_DARK_OAK_CHOPPING_BLOCK,
            WoodworkingBlocks.DAMAGED_ACACIA_CHOPPING_BLOCK).defer();
}