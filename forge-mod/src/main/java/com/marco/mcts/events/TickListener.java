package com.marco.mcts.events;

import com.marco.mcts.McTsMod;
import com.marco.mcts.actions.MovementActions;
import com.marco.mcts.network.EventBroadcaster;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles client-tick events.
 *
 * Responsibilities:
 * - Broadcasts a "tick" event to the TypeScript runtime every game tick.
 * - Applies scheduled movement inputs (forward/back/left/right ticks,
 *   sneak, sprint, walkTo).
 */
public class TickListener {

    private static EventBroadcaster broadcaster;
    private static final MovementActions.MovementState moveState = MovementActions.state;

    public static void setEventBroadcaster(EventBroadcaster b) {
        broadcaster = b;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!McTsMod.isModEnabled()) return;

        // Broadcast tick event to TS runtime
        if (broadcaster != null) {
            broadcaster.broadcastTick();
        }

        applyMovement();
    }

    /** Apply any pending movement inputs to the player. */
    private void applyMovement() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Minecraft mc = Minecraft.getInstance();

        // Apply directional movement by setting key states
        mc.options.keyUp.setDown(moveState.forwardTicks > 0);
        mc.options.keyDown.setDown(moveState.backTicks > 0);
        mc.options.keyLeft.setDown(moveState.leftTicks > 0);
        mc.options.keyRight.setDown(moveState.rightTicks > 0);
        mc.options.keyShift.setDown(moveState.sneaking);
        mc.options.keySprint.setDown(moveState.sprinting);

        // Decrement tick counters
        if (moveState.forwardTicks > 0) moveState.forwardTicks--;
        if (moveState.backTicks    > 0) moveState.backTicks--;
        if (moveState.leftTicks    > 0) moveState.leftTicks--;
        if (moveState.rightTicks   > 0) moveState.rightTicks--;

        // Simple walkTo: look at target and hold forward until close enough
        double[] target = moveState.walkToTarget;
        if (target != null) {
            double dx = target[0] - player.getX();
            double dz = target[2] - player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < 1.0) {
                // Arrived
                moveState.walkToTarget = null;
                mc.options.keyUp.setDown(false);
            } else {
                // Face the target
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                player.setYRot(yaw);
                player.yRotO = yaw;
                mc.options.keyUp.setDown(true);
            }
        }
    }
}
