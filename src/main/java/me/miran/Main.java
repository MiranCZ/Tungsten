package me.miran;

import me.miran.path.PathExecutor;
import me.miran.render.Color;
import me.miran.render.Cuboid;
import me.miran.world.VoxelWorld;
import net.fabricmc.api.ModInitializer;
import me.miran.render.Renderer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Main implements ModInitializer {

	public static Collection<Renderer> RENDERERS = Collections.synchronizedCollection(new ArrayList<>());
	public static Vec3d TARGET = new Vec3d(0,0,0);
	public static Renderer targetRenderer;
	public static PathExecutor EXECUTOR = new PathExecutor();
	public static VoxelWorld WORLD;

	@Override
	public void onInitialize() {
		updateTargetRenderer();
	}

	public static void updateTargetRenderer() {
		targetRenderer = new Cuboid(new Vec3d(TARGET.x-0.6,TARGET.y-1.1,TARGET.z-0.6),new Vec3d(1.2f,1.2f,1.2f),new Color(255,0,0));
	}

}
