package gigaherz.woodworking.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class ChoppingContext extends ItemHandlerWrapper
{
    protected final Player player;
    protected final Tier axeLevel;
    protected final int fortune;
    protected final Random random;

    public ChoppingContext(IItemHandlerModifiable inner, @Nullable Player player, @Nullable Supplier<Vec3> location, @Nullable Tier axeLevel, int fortune, @Nullable Random random)
    {
        super(inner, location, 64);
        this.player = player;
        this.axeLevel = axeLevel;
        this.fortune = fortune;
        this.random = random;
    }

    @Nullable
    public Player getPlayer()
    {
        return player;
    }

    @Nullable
    public Tier getAxeLevel()
    {
        return axeLevel;
    }

    public int getFortune()
    {
        return fortune;
    }

    @Nullable
    public Random getRandom()
    {
        return random;
    }
}