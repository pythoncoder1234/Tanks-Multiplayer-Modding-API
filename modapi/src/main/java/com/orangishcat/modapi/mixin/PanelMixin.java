package com.orangishcat.modapi.mixin;

import com.orangishcat.modapi.ModAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.Client;
import tanks.network.MessageReader;

@Mixin(Panel.class)
public abstract class PanelMixin
{
    @Shadow public int lastFPS;
    @Unique public int lastWorstFPS, worstFPS;

    @Shadow public abstract double[] getLatencyColor(long l);

    @Inject(method = "update", at = @At("TAIL"))
    public void update(CallbackInfo ci)
    {
        worstFPS = (int) Math.min(worstFPS, 100 / Panel.frameFrequency);
    }

    @Inject(method = "drawBar(D)V", at = @At("HEAD"), cancellable = true)
    public void drawBar(double offset, CallbackInfo ci)
    {
        ci.cancel();

        if (!Drawing.drawing.enableStats)
            return;

        Drawing.drawing.setColor(87, 46, 8);
        Game.game.window.shapeRenderer.fillRect(0, offset + (int) (Panel.windowHeight - 40), (int) (Panel.windowWidth), 40);

        Drawing.drawing.setColor(255, 227, 186);

        Drawing.drawing.setInterfaceFontSize(12);

        double boundary = Game.game.window.getEdgeBounds();

        if (Game.framework == Game.Framework.libgdx)
            boundary += 40;

        Game.game.window.fontRenderer.drawString(boundary + 10, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, Game.version);
        Game.game.window.fontRenderer.drawString(boundary + 10, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "FPS: " + lastFPS + "§255227186032/" + lastFPS + "§255227186255");

        Game.game.window.fontRenderer.drawString(boundary + 600, offset + (int) (Panel.windowHeight - 40 + 10), 0.6, 0.6, Game.screen.screenHint);

        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        long used = total - free;

        Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, ModAPI.version);
        Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "Memory used: " + used / 1048576 + "/" + total / 1048576 + "MB");

        double partyIpLen = 10;
        if (ScreenPartyLobby.isClient && !Game.connectedToOnline)
        {
            String s = "Connected to party";

            if (Game.showIP)
                s = "In party: " + (Game.lastParty.isEmpty() ? "localhost" : Game.lastParty) + (!Game.lastParty.contains(":") ? ":" + Game.port : "");

            partyIpLen = Game.game.window.fontRenderer.getStringSizeX(0.4, s) + 10 + offset;
            Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, s);
            partyIpLen += 50;

            s = "Latency: " + Client.handler.lastLatency + "ms";
            double[] col = getLatencyColor(Client.handler.lastLatency);
            Drawing.drawing.setColor(col[0], col[1], col[2]);
            Game.game.window.fontRenderer.drawString(Panel.windowWidth - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - 10 - offset, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, s);
        }

        if (ScreenPartyLobby.isClient || ScreenPartyHost.isServer)
        {
            Drawing.drawing.setColor(255, 227, 186);

            String s = "Upstream: " + MessageReader.upstreamBytesPerSec / 1024 + "KB/s";
            Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - offset, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, s);

            s = "Downstream: " + MessageReader.downstreamBytesPerSec / 1024 + "KB/s";
            Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - offset, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, s);
        }
    }
}
