package me.miran.executors;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * just a dummy class when baritone is executing
 */
public class BaritoneExecutor extends InputExecutor{

    private final Vec3i target;
    private boolean started = false;
    public BaritoneExecutor(int priority, boolean queuedExecutor, Vec3i target) {
        super(priority, queuedExecutor);
        this.target = target;
    }

    @Override
    public void tick(ClientPlayerEntity player, GameOptions options) {
        if (!started) {
            player.sendMessage(Text.of("baritone path"));
            started =true;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(target.getX(),target.getY(),target.getZ()));
        }
    }

    @Override
    public boolean isRunning() {
        if (!started) return true;
        return  BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }
}
