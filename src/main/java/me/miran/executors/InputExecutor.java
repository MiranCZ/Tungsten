package me.miran.executors;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.ApiStatus;

public abstract class InputExecutor {

    public final int priority;
    public final boolean queuedExecutor;

    public InputExecutor(int priority, boolean queuedExecutor) {
        this.priority = priority;
        this.queuedExecutor = queuedExecutor;
    }

    public abstract void tick(ClientPlayerEntity player, GameOptions options);
    public abstract boolean isRunning();
}
