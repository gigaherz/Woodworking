package gigaherz.woodworking.chopblock;

import gigaherz.woodworking.WoodworkingBlocks;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

public enum ChopblockMaterials implements IStringSerializable
{
    OAK("oak", WoodworkingBlocks.OAK_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_OAK_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_OAK_CHOPPING_BLOCK, "oak_logs"),
    BIRCH("birch", WoodworkingBlocks.BIRCH_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_BIRCH_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_BIRCH_CHOPPING_BLOCK, "birch_logs"),
    SPRUCE("spruce", WoodworkingBlocks.SPRUCE_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_SPRUCE_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_SPRUCE_CHOPPING_BLOCK, "spruce_logs"),
    JUNGLE("jungle", WoodworkingBlocks.JUNGLE_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_JUNGLE_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_JUNGLE_CHOPPING_BLOCK, "jungle_logs"),
    DARK_OAK("dark_oak", WoodworkingBlocks.DARK_OAK_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_DARK_OAK_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_DARK_OAK_CHOPPING_BLOCK, "dark_oak_logs"),
    ACACIA("acacia", WoodworkingBlocks.ACACIA_CHOPPING_BLOCK, WoodworkingBlocks.CHIPPED_ACACIA_CHOPPING_BLOCK, WoodworkingBlocks.DAMAGED_ACACIA_CHOPPING_BLOCK, "acacia_logs");

    private final String name;
    private final RegistryObject<ChoppingBlock> pristine;
    private final RegistryObject<ChoppingBlock> chipped;
    private final RegistryObject<ChoppingBlock> damaged;
    private final ResourceLocation madeFrom;

    ChopblockMaterials(String name, RegistryObject<ChoppingBlock> pristine, RegistryObject<ChoppingBlock> chipped, RegistryObject<ChoppingBlock> damaged, String madeFrom)
    {
        this.name = name;
        this.pristine = pristine;
        this.chipped = chipped;
        this.damaged = damaged;
        this.madeFrom = new ResourceLocation(madeFrom);
    }

    @Override
    public String getString()
    {
        return this.name;
    }

    public RegistryObject<ChoppingBlock> getPristine()
    {
        return pristine;
    }

    public RegistryObject<ChoppingBlock> getChipped()
    {
        return chipped;
    }

    public RegistryObject<ChoppingBlock> getDamaged()
    {
        return damaged;
    }

    public ResourceLocation getMadeFrom()
    {
        return madeFrom;
    }
}