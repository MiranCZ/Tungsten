package me.miran;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import me.miran.command.*;
import me.miran.render.Color;
import me.miran.render.Cuboid;
import me.miran.render.Renderer;
import me.miran.tick.MainClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Main implements ModInitializer {

	public static Collection<Renderer> RENDERERS = Collections.synchronizedCollection(new ArrayList<>());
	public static Collection<Renderer> PATH_RENDERERS = Collections.synchronizedCollection(new ArrayList<>());
	public static Vec3d TARGET = new Vec3d(0,0,0);
	public static Renderer targetRenderer;

	public static boolean startPathing = false;

	public static Collection<Renderer> TEST = Collections.synchronizedCollection(new ArrayList<>());
//bx.d(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext(),<blockpos>);

	@Override
	public void onInitialize() {
		updateTargetRenderer();
		setupBaritoneSettings();

		CommandRegistrationCallback.EVENT.register(new TargetCommand());
		CommandRegistrationCallback.EVENT.register(new StartCommand());
		CommandRegistrationCallback.EVENT.register(new StopCommand());
		CommandRegistrationCallback.EVENT.register(new BridgeCmd());
		CommandRegistrationCallback.EVENT.register(new RecordPathCommand());
		CommandRegistrationCallback.EVENT.register(new PlaceCmd());

		MainClient.onInitializeClient();
	}

	private void setupBaritoneSettings () {
		Settings settings = BaritoneAPI.getSettings();

		settings.allowBreak.value = false;
		settings.allowPlace.value = true;
		settings.allowParkourPlace.value = false;
		settings.renderGoal.value = false;
		settings.renderPath.value = true;
		settings.followRadius.value = 0;
		settings.splicePath.value = false;
		settings.sprintInWater.value = true;
		settings.blocksToAvoid.value.add(Blocks.WATER);
	}
	public static void updateTargetRenderer() {
		targetRenderer = new Cuboid(new Vec3d(TARGET.x-0.1,TARGET.y-0.1,TARGET.z-0.1),new Vec3d(1.2f,1.2f,1.2f),new Color(255,0,0));
	}

}
