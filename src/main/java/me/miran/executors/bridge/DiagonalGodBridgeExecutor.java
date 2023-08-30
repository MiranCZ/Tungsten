package me.miran.executors.bridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.Vec3d;

public class DiagonalGodBridgeExecutor extends BridgeExecutor {

    private byte preparing = 2;
    private byte ticks = 10;
    private Vec3d prevPos = new Vec3d(0,0,0);

    private final float yaw;


    public DiagonalGodBridgeExecutor(Vec3d target) {
       this(0,false,target);
    }

    public DiagonalGodBridgeExecutor(int priority,boolean queuedExecutor, Vec3d target) {
        super(priority, queuedExecutor,target);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        double xDist = player.getX()-target.x;
        double zDist = player.getZ()-target.z;
        if (xDist < 0 && zDist < 0) {
            yaw = 135;
        } else if (xDist > 0 && zDist < 0) {
            yaw = -135;
        } else if (xDist < 0 && zDist > 0) {
            yaw = 45;
        } else {
            yaw = -45;
        }
    }

    @Override
    protected void tickBridge(ClientPlayerEntity player, GameOptions options) {


        if ((Math.floor(player.getX()) == target.x || Math.floor(player.getZ()) == target.z) && !player.getWorld().getBlockState(player.getBlockPos().add(0,-1,0)).isAir()) {
            running = false;

            placeBlock();
            options.backKey.setPressed(false);
            options.sneakKey.setPressed(true);
            return;
        }



        player.prevYaw = player.getYaw();
        player.prevPitch = player.getPitch();

        player.setYaw(yaw);
        player.setPitch(76.1f);

        if (preparing == 0) {
            placeBlock();

            options.backKey.setPressed(true);
        } else {
            prepare(player, options);
        }
        prevPos = player.getPos();
    }

    private void prepare(ClientPlayerEntity player, GameOptions options) {
        if (preparing == 2) {
            if (player.getPos().equals(prevPos)) {
                preparing = 1;
                options.backKey.setPressed(false);
                return;
            }
            options.sneakKey.setPressed(true);
            options.backKey.setPressed(true);
        } else {
            if (ticks == 0) {
                placeBlock();
                preparing = 0;
                options.sneakKey.setPressed(false);
                options.leftKey.setPressed(false);

                return;
            }
            options.sneakKey.setPressed(true);
            options.leftKey.setPressed(true);

            ticks--;
        }
    }
}
