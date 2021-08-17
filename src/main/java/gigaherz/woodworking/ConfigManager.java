package gigaherz.woodworking;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static
    {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig
    {
        public final ForgeConfigSpec.DoubleValue choppingDegradeChance;
        public final ForgeConfigSpec.DoubleValue choppingExhaustion;
        public final ForgeConfigSpec.DoubleValue choppingWithEmptyHand;
        public final ForgeConfigSpec.ConfigValue<Config> axeLevels;

        ServerConfig(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Settings for the chopping block").push("chopping");
            choppingDegradeChance = builder
                    .comment("The average number of uses before degrading to the next phase will be 1/DegradeChance. Default is 16.67 average uses.")
                    .defineInRange("DegradeChance", 0.06, 0, Double.MAX_VALUE);
            choppingExhaustion = builder.defineInRange("Exhaustion", 0.0025, 0, Double.MAX_VALUE);
            choppingWithEmptyHand = builder.defineInRange("EmptyHandFactor", 0.4, 0, Double.MAX_VALUE);
            builder.pop();

            axeLevels = builder
                    .comment("Specify the effective strength of each harvest level when used in chopping.")
                    .define(Arrays.asList("axe_levels"), () -> Config.of(InMemoryFormat.defaultInstance()), x -> true, Config.class);
        }
    }

    public static final Object2DoubleMap<ResourceLocation> axeLevelMap = new Object2DoubleOpenHashMap<>();

    public static double getAxeLevelMultiplier(@Nullable Tier axeLevel)
    {
        if (axeLevel == null)
            return 1;

        if (axeLevelMap.containsKey(axeLevel))
            return axeLevelMap.get(axeLevel);

        return 1 + axeLevel.getLevel();
    }

    private static final Set<String> warns = new HashSet<>();
    public static boolean getConfigBoolean(String spec, String... path)
    {
        ForgeConfigSpec spec1 = /*spec.equals("common") ? COMMON_SPEC :*/ SERVER_SPEC;
        ForgeConfigSpec.BooleanValue value = spec1.getValues().get(Arrays.asList(path));
        if (value == null)
        {
            String pathJoined = String.join("/", path);
            if (!warns.contains(pathJoined))
            {
                LOGGER.warn("Config path not found: " + pathJoined + ". This message will only show once per path.");
                warns.add(pathJoined);
            }
            return false;
        }
        return value.get();
    }

    @Mod.EventBusSubscriber(modid = WoodworkingMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Events
    {
        private static final Pattern AXE_LEVEL_ENTRY_PATTERN = Pattern.compile("^AxeLevel(?<level>[0-9]+)$");

        @SubscribeEvent
        public static void modConfig(ModConfigEvent event)
        {
            ModConfig config = event.getConfig();
            if (config.getSpec() != SERVER_SPEC)
                return;

            Config axeLevels = SERVER.axeLevels.get();

            for (Config.Entry e : axeLevels.entrySet())
            {
                Matcher m = AXE_LEVEL_ENTRY_PATTERN.matcher(e.getKey());
                if (m.matches())
                {
                    String numberPart = m.group("level");
                    ResourceLocation levelNumber = new ResourceLocation(numberPart);
                    axeLevelMap.put(levelNumber, e.getInt());
                }
            }
        }
    }
}