package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.agent.AgentRaycastContext;
import me.miran.executors.ExecutionManager;
import me.miran.executors.bridge.BreezilyBridgeExecutor;
import me.miran.executors.bridge.DiagonalGodBridgeExecutor;
import me.miran.mixin.minecraft.KeyBindingAccessor;
import me.miran.path.PathFinder;
import me.miran.render.Color;
import me.miran.render.Cube;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.WorldView;

public class PlaceCmd implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@place")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        agentTest();
        return 0;
    }

    private int raaaun(ServerCommandSource source) {
        Main.TEST.clear();
        PlayerEntity player = source.getPlayer();
       /* player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,Main.TARGET.add(0.5,0.5,0.5));

        KeyBindingAccessor keyBinding = (KeyBindingAccessor) MinecraftClient.getInstance().options.useKey;

        KeyBinding.onKeyPressed(keyBinding.getBoundKey());*/

        BlockPos pos = new BlockPos(Main.TARGET);
        for (Direction direction : Direction.values()) {
            BlockPos dirPos = pos.offset(direction);
            if (baritone.bx.c(PathFinder.bsi, dirPos.getX(),dirPos.getY(),dirPos.getZ())) {
                Main.TEST.add(new Cube(dirPos, Color.BLUE));

                Vec3d vPos = new Vec3d(dirPos.getX(),dirPos.getY(),dirPos.getZ());

                Vec3i dirV = direction.getVector();
                vPos = vPos.add(0.5,0.5,0.5);
                vPos = vPos.add(new Vec3d(dirV.getX()*-0.4d,dirV.getY()*-0.4d,dirV.getZ()*-0.4d));

                player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,vPos);

                if (raycast(player,direction, vPos)) {
                    player.sendMessage(Text.of("ready to place"));
                    return 0;
                }
            }


        }
        player.sendMessage(Text.of("not possible"));

        return 0;
    }

    private boolean raycast(PlayerEntity player, Direction neededDir, Vec3d target) {
        float maxDistance = 5;
        Vec3d rot = player.getRotationVector();
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        BlockHitResult rayTraceResult = player.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE, player));//MinecraftClient.getInstance().player.raycast(10, 0, false);
        if (rayTraceResult.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos blockPos = rayTraceResult.getBlockPos();

            if (blockPos.getX() == Math.floor(target.x) && blockPos.getY() == Math.floor(target.y) && blockPos.getZ() == Math.floor(target.z)) {
                player.sendMessage(Text.of("looking at target " + rayTraceResult.getSide()));
                return rayTraceResult.getSide().equals(neededDir.getOpposite());
            } else {
                player.sendMessage(Text.of("wrong block " + blockPos + " ; " + target));
                return false;
            }

        }
        player.sendMessage(Text.of("target not found "));
        return false;
    }

    private void agentTest() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Agent agent = Agent.of(mc.player);
      //  mc.world.setBlockState(mc.player.getBlockPos(), Blocks.DIAMOND_BLOCK.getDefaultState());

        BlockPos pos = new BlockPos(Main.TARGET);
        for (Direction direction : Direction.values()) {
            BlockPos dirPos = pos.offset(direction);
            if (baritone.bx.c(PathFinder.bsi, dirPos.getX(),dirPos.getY(),dirPos.getZ())) {
                Main.TEST.add(new Cube(dirPos, Color.BLUE));

                Vec3d vPos = new Vec3d(dirPos.getX(),dirPos.getY(),dirPos.getZ());

                Vec3i dirV = direction.getVector();
                vPos = vPos.add(0.5,0.5,0.5);
                vPos = vPos.add(new Vec3d(dirV.getX()*-0.4d,dirV.getY()*-0.4d,dirV.getZ()*-0.4d));

                agent.lookAt(vPos);

                if (raycast(agent,direction, vPos,mc.world)) {
                    mc.player.sendMessage(Text.of(agent.yaw + " ; " + agent.pitch));
                    return;
                }
            }


        }
    }

    private boolean raycast(Agent player, Direction neededDir, Vec3d target, WorldView world) {
        float maxDistance = 4;
        Vec3d rot = player.getRotationVector();
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        BlockHitResult rayTraceResult = world.raycast(new AgentRaycastContext(start, end, RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE, player));//MinecraftClient.getInstance().player.raycast(10, 0, false);
        if (rayTraceResult.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos blockPos = rayTraceResult.getBlockPos();

            if (blockPos.getX() == Math.floor(target.x) && blockPos.getY() == Math.floor(target.y) && blockPos.getZ() == Math.floor(target.z)) {
                return rayTraceResult.getSide().equals(neededDir.getOpposite());
            } else {
                return false;
            }

        }
        return false;
    }


}
