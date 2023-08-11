package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import me.miran.Main;
import me.miran.path.PathExecutor;
import me.miran.path.PathFinder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StopCommand implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@stop")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        source.sendMessage(Text.literal("Force stopping search thread").formatted(Formatting.DARK_GREEN));
        PathFinder.shouldStop = true;
        Main.EXECUTOR = new PathExecutor();
        Main.EXECUTOR.end(MinecraftClient.getInstance().options);

        return 0;
    }
}
