package me.miran.path;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PathInput {

	public final boolean forward, back, right, left, jump, sneak, sprint;
	public float pitch, yaw;
	@Nullable
	public final BlockPos placedBlock;

	public PathInput(boolean forward, boolean back, boolean right, boolean left, boolean jump, boolean sneak, boolean sprint, float pitch, float yaw) {
		this(forward, back, right, left, jump, sneak, sprint, pitch, yaw,null);
	}

	public PathInput(boolean forward, boolean back, boolean right, boolean left, boolean jump, boolean sneak, boolean sprint, float pitch, float yaw, BlockPos placedBlock) {
		this.forward = forward;
		this.back = back;
		this.right = right;
		this.left = left;
		this.jump = jump;
		this.sneak = sneak;
		this.sprint = sprint;
		this.pitch = pitch;
		this.yaw = yaw;
		this.placedBlock = placedBlock;
	}

}
