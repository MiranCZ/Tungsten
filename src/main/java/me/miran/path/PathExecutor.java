package me.miran.path;

import me.miran.Main;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PathExecutor {

    protected List<Node> path;
    protected int tick = 0;

    public PathExecutor() {

	}

	public void setPath(List<Node> path) {
    	this.path = path;
    	this.tick = 0;

		if (!path.isEmpty()) {
			renderCurrentPath();
		}
	}

	private void renderCurrentPath() {
		Main.PATH_RENDERERS.clear();
		Node n = path.get(path.size()-1);

		while(n.parent != null) {
			Main.PATH_RENDERERS.add(new Line(n.agent.getPos(), n.parent.agent.getPos(), n.color));
			Main.PATH_RENDERERS.add(new Cuboid(n.agent.getPos().subtract(0.05D, 0.05D, 0.05D), new Vec3d(0.1D, 0.1D, 0.1D), n.color));
			n = n.parent;
		}
	}

	public boolean isRunning() {
        return this.path != null && this.tick <= this.path.size();
    }

	public boolean calculating = false;
	private int recalculationMessageCooldown = 0;

    public void tick(ClientPlayerEntity player, GameOptions options) {
		if (calculating) return;


    	if(this.tick == this.path.size()) {
		    end(options);
	    } else {
		    Node node = this.path.get(this.tick);

		    if(this.tick != 0) {

			   if (!this.path.get(this.tick - 1).agent.compare(player, false)) {

				   end(options);
				   if (recalculationMessageCooldown == 0) {
					   player.sendMessage(Text.literal("Something went wrong... recalculating path!").formatted(Formatting.AQUA));
					   recalculationMessageCooldown = 10;//prevents from spamming the message
				   }
				   PathFinder.calculateContinuedPathWithMismatch(player.getWorld(),path,tick);
				   return;
			   }
		    }
			if(recalculationMessageCooldown > 0) {
				recalculationMessageCooldown--;
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

	public void end(GameOptions options) {
		options.forwardKey.setPressed(false);
		options.backKey.setPressed(false);
		options.leftKey.setPressed(false);
		options.rightKey.setPressed(false);
		options.jumpKey.setPressed(false);
		options.sneakKey.setPressed(false);
		options.sprintKey.setPressed(false);
	}

}
