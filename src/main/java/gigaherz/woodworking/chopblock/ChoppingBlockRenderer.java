package gigaherz.woodworking.chopblock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ChoppingBlockRenderer
        implements BlockEntityRenderer<ChoppingBlockTileEntity>
{
    private final Minecraft mc = Minecraft.getInstance();

    public ChoppingBlockRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(ChoppingBlockTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn)
    {
        Level level = te.getLevel();
        if (level == null)
            return;

        BlockState state = level.getBlockState(te.getBlockPos());
        if (!(state.getBlock() instanceof ChoppingBlock))
            return;

        //if (destroyStage < 0)
        {
            matrixStack.pushPose();

            ItemRenderer itemRenderer = mc.getItemRenderer();

            LazyOptional<IItemHandler> linv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            linv.ifPresent((inv) -> {
                ItemStack stack = inv.getStackInSlot(0);
                if (stack.getCount() > 0)
                {
                    matrixStack.pushPose();
                    matrixStack.translate(0.5, 0.5, 0.5);

                    matrixStack.translate(0, -4.5 / 16.0f, 0);
                    matrixStack.scale(2, 2, 2);

                    BakedModel ibakedmodel = itemRenderer.getModel(stack, level, null, 0);
                    itemRenderer.render(stack, ItemTransforms.TransformType.GROUND, true, matrixStack, buffer, combinedLightIn, combinedOverlayIn, ibakedmodel);
                    /*int breakStage = te.getBreakStage();
                    if (breakStage >= 0)
                    {
                        renderItem(stack, ItemCameraTransforms.TransformType.GROUND, breakStage);
                    }*/

                    matrixStack.popPose();
                }
            });

            matrixStack.popPose();
        }
    }
}