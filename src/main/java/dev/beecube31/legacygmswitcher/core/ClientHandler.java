package dev.beecube31.legacygmswitcher.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Gui {

    private static class GameModeInfo {
        private final String translationKey;
        private final String commandName;
        private final ItemStack icon;

        public GameModeInfo(String translationKey, String commandName, ItemStack icon) {
            this.translationKey = translationKey;
            this.commandName = commandName;
            this.icon = icon;
        }
    }

    public static class GameSwitcherStubGui extends GuiScreen {
        private final ClientHandler handler;

        public GameSwitcherStubGui(ClientHandler handler) {
            this.handler = handler;
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) {
            if (keyCode == Keyboard.KEY_F4) {
                handler.nextItem();
                return;
            }

            if (keyCode == Keyboard.KEY_ESCAPE) {
                handler.isVisible = false;
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(LegacyGMS.MODID,"textures/gui/container/gamemode_switcher.png");

    private boolean isVisible = false;
    private int selectedIndex = 0;
    private final List<GameModeInfo> gameModes = new ArrayList<>();
    private final Minecraft mc = Minecraft.getMinecraft();

    private String lastGameModeName = null;

    public ClientHandler() {
        gameModes.add(new GameModeInfo("gameMode.creative", "creative", new ItemStack(Blocks.grass)));
        gameModes.add(new GameModeInfo("gameMode.survival", "survival", new ItemStack(Items.iron_sword)));
        gameModes.add(new GameModeInfo("gameMode.adventure", "adventure", new ItemStack(Items.map)));
        gameModes.add(new GameModeInfo("gameMode.spectator", "spectator", new ItemStack(Items.ender_eye)));
    }

    private boolean hasPermission() {
        return mc.thePlayer != null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInputEv(InputEvent.KeyInputEvent event) {

        if (Keyboard.getEventKey() == Keyboard.KEY_F4 && Keyboard.getEventKeyState()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                if (!isVisible) {
                    if (!hasPermission()) {
                        ChatComponentText textComp = new ChatComponentText(I18n.format("debug.gamemodes.error"));
                        textComp.getChatStyle().setColor(EnumChatFormatting.RED);
                        mc.thePlayer.addChatComponentMessage(textComp);
                        return;
                    }
                    openMenu();
                    if (!(mc.currentScreen instanceof GameSwitcherStubGui)) {
                        mc.displayGuiScreen(new GameSwitcherStubGui(this));
                    }
                    mc.setIngameNotInFocus();
                } else {
                    nextItem();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTickEv(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isVisible) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                applyGameMode();
                isVisible = false;
                mc.setIngameFocus();
            }
        }
    }

    @SubscribeEvent
    public void onGameOverlayEv(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !isVisible) {
            return;
        }

        ScaledResolution res = event.resolution;
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        int renderSlotSize = 26;
        int count = gameModes.size();

        int windowWidth = 125;
        int windowHeight = 75;

        int centerX = width / 2;
        int centerY = height / 2;

        int windowX = centerX - windowWidth / 2;
        int windowY = centerY - windowHeight / 2 - 30;

        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        int slotsStartX = centerX - windowWidth / 2 + 3;
        int slotsY = windowY + 27;

        for (int i = 0; i < count; i++) {
            int x = slotsStartX + i * (renderSlotSize) + i * 5;
            if (mouseX >= x && mouseX < x + renderSlotSize && mouseY >= slotsY && mouseY < slotsY + renderSlotSize) {
                selectedIndex = i;
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawScaledCustomSizeModalRect(windowX, windowY, 0, 0, 125, 75, windowWidth, windowHeight, 256, 256);

        String modeName = I18n.format(gameModes.get(selectedIndex).translationKey);

        drawCenteredString(mc.fontRendererObj, modeName, centerX, windowY + 7, 0xFFFFFF);

        for (int i = 0; i < count; i++) {
            int x = slotsStartX + i * (renderSlotSize) + i * 5;
            boolean isSelected = (i == selectedIndex);

            mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            drawScaledCustomSizeModalRect(x, slotsY, 0, 75, 26, 26, renderSlotSize, renderSlotSize, 256, 256);

            if (isSelected) {
                drawScaledCustomSizeModalRect(x, slotsY, 26, 75, 26, 26, renderSlotSize, renderSlotSize, 256, 256);
            }

            ItemStack icon = gameModes.get(i).icon;
            RenderHelper.enableGUIStandardItemLighting();
            int iconOffset = (renderSlotSize - 16) / 2;
            mc.getRenderItem().renderItemAndEffectIntoGUI(icon, x + iconOffset, slotsY + iconOffset);
            RenderHelper.disableStandardItemLighting();
        }

        drawCenteredString(mc.fontRendererObj, I18n.format("debug.gamemodes.select_next", "§b" + I18n.format("debug.gamemodes.press_f4") + "§r -"),
                centerX, windowY + windowHeight - 12, 0xAAAAAA);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void openMenu() {
        isVisible = true;

        if (mc.playerController != null) {
            String currentMode = mc.playerController.getCurrentGameType().getName();
            int currentModeIndex = 0;

            for (int i = 0; i < gameModes.size(); i++) {
                if (gameModes.get(i).commandName.equalsIgnoreCase(currentMode)) {
                    currentModeIndex = i;
                    break;
                }
            }

            if (lastGameModeName != null && !lastGameModeName.equalsIgnoreCase(currentMode)) {
                for (int i = 0; i < gameModes.size(); i++) {
                    if (gameModes.get(i).commandName.equalsIgnoreCase(lastGameModeName)) {
                        selectedIndex = i;
                        return;
                    }
                }
            }

            selectedIndex = (currentModeIndex + 1) % gameModes.size();
        }
    }

    private void nextItem() {
        selectedIndex++;
        if (selectedIndex >= gameModes.size()) {
            selectedIndex = 0;
        }
    }

    private void closeMenu() {
        mc.displayGuiScreen(null);
        isVisible = false;
    }

    private void applyGameMode() {
        if (selectedIndex >= 0 && selectedIndex < gameModes.size()) {
            GameModeInfo targetMode = gameModes.get(selectedIndex);

            if (mc.playerController != null) {
                lastGameModeName = mc.playerController.getCurrentGameType().getName();
            }

            if (lastGameModeName == null || !lastGameModeName.equalsIgnoreCase(targetMode.commandName)) {
                mc.thePlayer.sendChatMessage("/gamemode " + targetMode.commandName);
                closeMenu();
            }
        }
    }
}