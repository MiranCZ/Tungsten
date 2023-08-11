package me.miran.path;

import me.miran.Main;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PathExecutor {

    protected List<Node> path;
    protected int tick = 0;

    public PathExecutor() {

	}

	public void setPath(List<Node> path) {
    	this.path = path;
    	this.tick = 0;
	}

	public boolean isRunning() {
        return this.path != null && this.tick <= this.path.size();
    }



    public void tick(ClientPlayerEntity player, GameOptions options) {
    	if(this.tick == this.path.size()) {
		    end(options);
	    } else {
		    Node node = this.path.get(this.tick);

		    if(this.tick != 0) {

			   if (!this.path.get(this.tick - 1).agent.compare(player, false)) {

				   end(options);
				   player.sendMessage(Text.literal("Something went wrong... recalculating path!").formatted(Formatting.AQUA));
				   //Main.startPathing = true;
				   PathFinder.calculateContinuedPathWithMismatch(player.getWorld(),path,tick);
				   return;
			   }
		    }


		    if(node.input != null) {
			    options.forwardKey.setPressed(node.input.forward);
			    options.backKey.setPressed(node.input.back);
			    options.leftKey.setPressed(node.input.left);
			    options.rightKey.setPressed(node.input.right);
			    options.jumpKey.setPressed(node.input.jump);
			    options.sneakKey.setPressed(node.input.sneak);
			    options.sprintKey.setPressed(node.input.sprint);
			    player.prevYaw = player.getYaw();
			    player.prevPitch = player.getPitch();
			    player.setYaw(node.input.yaw);
			    player.setPitch(node.input.pitch);
		    }
	    }

	    this.tick++;
    }

	private void end(GameOptions options) {
		options.forwardKey.setPressed(false);
		options.backKey.setPressed(false);
		options.leftKey.setPressed(false);
		options.rightKey.setPressed(false);
		options.jumpKey.setPressed(false);
		options.sneakKey.setPressed(false);
		options.sprintKey.setPressed(false);
	}

}
