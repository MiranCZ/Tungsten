package me.miran.executors.bridge;

import me.miran.Main;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.Vec3d;

public class BreezilyBridgeExecutor extends BridgeExecutor {


    private boolean left = false;
    public BreezilyBridgeExecutor(Vec3d target) {
        this(0,false,target);
    }

    public BreezilyBridgeExecutor(int priority, boolean queuedExecutor,Vec3d target) {
        super(priority, queuedExecutor,target);
    }

    @Override
    protected void tickBridge(ClientPlayerEntity player, GameOptions options) {
        placeBlock();

        options.backKey.setPressed(true);
        boolean xCords;

        if (Math.floor(player.getZ()) == Main.TARGET.z) {
            xCords = true;
        } else if (Math.floor(player.getX()) == Main.TARGET.x) {
            xCords = false;
        } else {
            running = false;
            return;
        }


        double dist = xCords ? player.getZ() - target.z : player.getX() - target.x;

        player.prevYaw = player.getYaw();
        player.prevPitch = player.getPitch();
        double d;
        if (xCords) {
            d = player.getX()-target.x;
            if (d < 0) {
                player.setYaw(90);
            } else {
                player.setYaw(-90);
            }
        } else {
            d = player.getZ()-target.z;
            if (d < 0) {
                player.setYaw(180);
            } else {
                player.setYaw(0);
            }
            d=-d;
        }
        if (Math.abs(d) < 0.7) {
            running = false;
            options.leftKey.setPressed(false);
            options.rightKey.setPressed(false);
            options.backKey.setPressed(false);
            return;
        }

        player.setPitch(80.5f);


        if (dist < 0.4) {
            left = d < 0;
        } else if (dist > 0.6) {
            left = d > 0;
        }


        options.leftKey.setPressed(left);
        options.rightKey.setPressed(!left);
    }

}
