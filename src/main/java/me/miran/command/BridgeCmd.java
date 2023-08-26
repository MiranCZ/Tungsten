package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BridgeCmd implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@bridge")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        Main.bridge = !Main.bridge;

        return 0;
    }
}
