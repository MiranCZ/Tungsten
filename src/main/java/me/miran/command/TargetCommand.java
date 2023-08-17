package me.miran.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.miran.Main;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TargetCommand implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        //TODO add also version with no arguments
        dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("@target")).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) -> run(context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos")))));
    }

    private int run(ServerCommandSource source, BlockPos pos) {
        pos = pos.add(0, 1, 0);
        source.sendMessage(Text.literal("Target position was set as: " + pos.toShortString()).formatted(Formatting.GREEN));

        Main.TARGET = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        Main.updateTargetRenderer();
        return 0;
    }
}
