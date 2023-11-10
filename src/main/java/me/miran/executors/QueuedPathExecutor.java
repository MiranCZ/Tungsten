package me.miran.executors;

import me.miran.path.PathFinder;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class QueuedPathExecutor extends InputExecutor implements TargetExecutor{
    private final Vec3i target;
    private boolean calculating = false;
    private boolean running = true;
    private int delay =5;


    public QueuedPathExecutor(int priority, Vec3i target) {
        super(priority, true);
        this.target = target;
    }

    @Override
    public void tick(ClientPlayerEntity player, GameOptions options) {
        if (!calculating) {
            if (delay == 0) {
                player.sendMessage(Text.of("finishing path"));
                calculating = true;
                PathFinder.findAndSetPathAsync(player.world, target, new PathExecutor(target,priority, queuedExecutor));
            } else {
                delay--;
            }
        }
        if (calculating && !PathFinder.isRunning()) {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public Vec3i getTarget() {
        return target;
    }
}
