package me.miran.executors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.ApiStatus;

public abstract class InputExecutor {

    public static void resetKeys() {
        GameOptions options = MinecraftClient.getInstance().options;
        options.forwardKey.setPressed(false);
        options.backKey.setPressed(false);
        options.leftKey.setPressed(false);
        options.rightKey.setPressed(false);
        options.jumpKey.setPressed(false);
        options.sneakKey.setPressed(false);
        options.sprintKey.setPressed(false);
    }

    public final int priority;
    public final boolean queuedExecutor;

    public InputExecutor(int priority, boolean queuedExecutor) {
        this.priority = priority;
        this.queuedExecutor = queuedExecutor;
    }

    public abstract void tick(ClientPlayerEntity player, GameOptions options);
    public abstract boolean isRunning();
}
