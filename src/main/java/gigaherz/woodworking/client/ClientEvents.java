package gigaherz.woodworking.client;

import gigaherz.woodworking.WoodworkingMod;
import gigaherz.woodworking.chopblock.ChoppingBlockRenderer;
import gigaherz.woodworking.chopblock.ChoppingBlockTileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WoodworkingMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    // ----------------------------------------------------------- Item/Block Models
    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ChoppingBlockTileEntity.TYPE.get(), ChoppingBlockRenderer::new);
    }
}