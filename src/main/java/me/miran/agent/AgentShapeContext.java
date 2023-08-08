package me.miran.agent;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;

import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Predicate;

public class AgentShapeContext implements ShapeContext {

    protected static final ShapeContext ABSENT = new EntityShapeContext(false, -1.7976931348623157E308, ItemStack.EMPTY, fluidState -> false, null) {
        @Override
        public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return defaultValue;
        }
    };

    private final boolean descending;
    private final double minY;
    private final ItemStack heldItem;
    private final Predicate<FluidState> walkOnFluidPredicate;

    protected AgentShapeContext(boolean descending, double minY, ItemStack heldItem, Predicate<FluidState> walkOnFluidPredicate) {
        this.descending = descending;
        this.minY = minY;
        this.heldItem = heldItem;
        this.walkOnFluidPredicate = walkOnFluidPredicate;
    }

    protected AgentShapeContext(Agent agent) {
        this(agent.input.sneaking, agent.box.minY, ItemStack.EMPTY, fluidState -> false);
    }

    @Override
    public boolean isHolding(Item item) {
        return this.heldItem.isOf(item);
    }

    @Override
    public boolean canWalkOnFluid(FluidState state, FluidState fluidState) {
        return this.walkOnFluidPredicate.test(fluidState) && !state.getFluid().matchesType(fluidState.getFluid());
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return this.minY > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - (double)1.0E-5f;
    }

}
