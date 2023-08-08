package me.miran.agent;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public class AgentRaycastContext extends RaycastContext {

	private final ShapeType shapeType;
	private final AgentShapeContext shapeContext;

	public AgentRaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, Agent agent) {
		super(start, end, shapeType, fluidHandling, null);
		this.shapeType = shapeType;
		this.shapeContext = new AgentShapeContext(agent);
	}

	@Override
	public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
		return this.shapeType.get(state, world, pos, this.shapeContext);
	}

}
