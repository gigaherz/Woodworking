package gigaherz.woodworking.sawmill.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Set;

public class SawmillScreen extends AbstractContainerScreen<SawmillContainer>
{
    public static final ResourceLocation GUI_TEXTURE_LOCATION = new ResourceLocation("minecraft:textures/gui/container/furnace.png");

    public SawmillScreen(SawmillContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
    }

    @Override
    public void init()
    {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE_LOCATION);

        int x = this.leftPos;
        int y = this.topPos;
        this.blit(matrixStack, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isBurning())
        {
            int k = this.getBurnLeftScaled(13);
            this.blit(matrixStack, x + 56, y + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        int l = this.getCookProgressScaled(24);
        this.blit(matrixStack, x + 79, y + 34, 176, 14, l + 1, 16);
    }

    private int getCookProgressScaled(int pixels)
    {
        int total = menu.getTotalCookTime();
        if (total == 0) return 0;
        int progress = menu.getCookTime();
        return pixels * progress / total;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int total = menu.getTotalBurnTime();

        if (total == 0)
        {
            total = 200;
        }

        return pixels * menu.getRemainingBurnTime() / total;
    }
}