package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import me.miran.executors.bridge.BreezilyBridgeExecutor;
import me.miran.executors.bridge.DiagonalGodBridgeExecutor;
import me.miran.executors.ExecutionManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class BridgeCmd implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@bridge")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        ExecutionManager.addExecutor(new DiagonalGodBridgeExecutor(1,true, Main.TARGET));
        ExecutionManager.addExecutor(new BreezilyBridgeExecutor(0,true,Main.TARGET));

        return 0;
    }
}
