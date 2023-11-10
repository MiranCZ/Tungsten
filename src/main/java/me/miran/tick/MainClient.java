package me.miran.tick;


import com.mojang.brigadier.arguments.FloatArgumentType;
import me.miran.mixin.tick.MinecraftAccessor;
import me.miran.mixin.tick.TimerAccessor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class MainClient {



	public static void onInitializeClient() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(CommandManager.literal("tickrate").requires(source -> source.hasPermissionLevel(4)).then(CommandManager.argument("ticks", FloatArgumentType.floatArg()).requires(source -> source.hasPermissionLevel(4)).executes(c -> {
				float tickrate = FloatArgumentType.getFloat(c, "ticks");
				if (tickrate < 0.1 || tickrate > 100) {
					c.getSource().sendError(Text.literal("Tickrate needs to be between '0.1' and '100' (" + tickrate + " was given)").formatted(Formatting.RED));
					return -1;
				}

				((MinecraftServerMixin) c.getSource().getServer()).setMsPerTick((int) (1000/tickrate));


				onTickratePacket(tickrate);


				return 1;
			})).executes(c -> {
				c.getSource().sendError(Text.literal("You need to provide a tickrate").formatted(Formatting.RED));

				return 1;
			}));
		});
	}

	public static void onTickratePacket(float tickrate) {
		((TimerAccessor) ((MinecraftAccessor) MinecraftClient.getInstance()).getRenderTickCounter()).setTickTime(1000F / tickrate);
	}
}
