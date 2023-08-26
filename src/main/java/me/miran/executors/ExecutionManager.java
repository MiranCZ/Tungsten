package me.miran.executors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class ExecutionManager {

    private static InputExecutor executor = null;
    private static final Queue<InputExecutor> executorQueue = new PriorityQueue<>(Comparator.comparingInt(executor ->executor.priority));

    public static void addExecutor(InputExecutor inputExecutor) {
        if (executor == null) {
            executor = inputExecutor;
            return;
        }
        if (executor.priority < inputExecutor.priority) {
            executor = inputExecutor;
            return;
        }
        if (inputExecutor.queuedExecutor) {
            executorQueue.add(inputExecutor);
        }


    }

    public static boolean isAnyExecutorRunning() {
        return executor == null;
    }

    public static void tick(ClientPlayerEntity player, GameOptions options) {
        if (executor != null) {
            executor.tick(player, options);

            if (!executor.isRunning()) {
                executor = executorQueue.poll();
                resetInputs();
            }
        }
    }

    public static void stopAll() {
        executor = null;
        executorQueue.clear();

        resetInputs();
    }

    private static void resetInputs() {
        GameOptions options = MinecraftClient.getInstance().options;
        options.forwardKey.setPressed(false);
        options.backKey.setPressed(false);
        options.leftKey.setPressed(false);
        options.rightKey.setPressed(false);
        options.jumpKey.setPressed(false);
        options.sneakKey.setPressed(false);
        options.sprintKey.setPressed(false);
    }

    @Nullable
    public static InputExecutor getCurrentExecutor() {
        return executor;
    }

    private ExecutionManager(){
    }



}
