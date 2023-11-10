package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import me.miran.executors.bridge.BreezilyBridgeExecutor;
import me.miran.executors.bridge.DiagonalGodBridgeExecutor;
import me.miran.executors.ExecutionManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BridgeCmd implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@bridge")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
    //    ExecutionManager.addExecutor(new DiagonalGodBridgeExecutor(1,true, Main.TARGET));
      //  ExecutionManager.addExecutor(new BreezilyBridgeExecutor(0,true,Main.TARGET));

            ServerPlayerEntity player = source.getPlayer();
            HitResult r = player.raycast(4, 0, false);
            if (r == null || r.getType() != HitResult.Type.BLOCK) return 0;

            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) r, 0);


            MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);

        return 0;
    }
}
