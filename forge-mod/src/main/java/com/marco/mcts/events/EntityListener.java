package com.marco.mcts.events;

import com.marco.mcts.McTsMod;
import com.marco.mcts.network.EventBroadcaster;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Listens for entity / player events:
 * - Damage taken by the local player
 * - Local player death
 * - Player join / leave (tab list)
 */
public class EntityListener {

    private static EventBroadcaster broadcaster;

    public static void setEventBroadcaster(EventBroadcaster b) {
        broadcaster = b;
    }

    /** Fired when a living entity takes damage. */
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (!McTsMod.isModEnabled()) return;
        if (broadcaster == null) return;

        // Only care about damage to the local player
        if (!(event.getEntity() instanceof Player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (event.getEntity().getId() != mc.player.getId()) return;

        String source = event.getSource().getMsgId();
        float amount  = event.getAmount();
        broadcaster.broadcastDamage(source, amount);
    }

    /** Fired when a living entity dies. */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!McTsMod.isModEnabled()) return;
        if (broadcaster == null) return;

        if (!(event.getEntity() instanceof Player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (event.getEntity().getId() != mc.player.getId()) return;

        broadcaster.broadcastDeath();
    }

    /** Fired when a player logs in (visible on the tab list). */
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!McTsMod.isModEnabled()) return;
        if (broadcaster == null) return;
        broadcaster.broadcastPlayerJoin(event.getEntity().getName().getString());
    }

    /** Fired when a player logs out. */
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!McTsMod.isModEnabled()) return;
        if (broadcaster == null) return;
        broadcaster.broadcastPlayerLeave(event.getEntity().getName().getString());
    }
}
