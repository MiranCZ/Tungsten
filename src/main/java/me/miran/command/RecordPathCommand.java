package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RecordPathCommand implements CommandRegistrationCallback {

    /*
    Main.RENDERERS.add(new Line(n.agent.getPos(), n.parent.agent.getPos(), n.color));
					Main.RENDERERS.add(new Cuboid(n.agent.getPos().subtract(0.05D, 0.05D, 0.05D), new Vec3d(0.1D, 0.1D, 0.1D), n.color));

     */

    public static boolean recording = false;


    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((CommandManager.literal("@record")).executes((context)-> run(context.getSource())));
    }

    private int run(ServerCommandSource source) {
        recording = !recording;

        if (recording) {
            source.sendMessage(Text.literal("Started recording your path").formatted(Formatting.GREEN));
        } else {
            source.sendMessage(Text.literal("Stopped recording your path").formatted(Formatting.DARK_GREEN));
        }

        return 0;
    }
}
