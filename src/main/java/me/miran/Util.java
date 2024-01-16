package me.miran;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public class Util {
    private Util() {
    }

    /**
     * @return {@code null} if the item is not a {@link BlockItem}, an instance of {@link Block} otherwise.
     */
    @Nullable
    public static Block asBlock(Item item) {
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }
}
