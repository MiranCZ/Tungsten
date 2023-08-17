package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StartCommand implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@start")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        source.sendMessage(Text.literal("Started pathing").formatted(Formatting.GREEN));
        Main.startPathing = true;

        return 0;
    }
}
