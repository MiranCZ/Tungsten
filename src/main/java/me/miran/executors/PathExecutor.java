package me.miran.executors;

import me.miran.Main;
import me.miran.command.StopCommand;
import me.miran.command.TargetCommand;
import me.miran.executors.bridge.BridgeExecutor;
import me.miran.path.Node;
import me.miran.path.PathFinder;
import me.miran.path.PathRebuilder;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class PathExecutor extends InputExecutor implements TargetExecutor {

    protected List<Node> path;
    protected int tick = 0;
	private final Vec3i target;

    public PathExecutor(Vec3i target) {
		this(target,0,false);
	}

	public PathExecutor(Vec3i target,int priority, boolean queuedExecutor) {
		super(priority, queuedExecutor);
		this.target = target;
	}

	public void setPath(List<Node> path) {
    	this.path = path;
    	this.tick = 0;

		if (!path.isEmpty()) {
			renderCurrentPath();
		}
	}

	public void renderCurrentPath() {
		if (path == null || path.isEmpty()) return;

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
				   //PathRebuilder.calculateContinuedPathWithMismatch(player.getWorld(),path,tick,this);

				   ExecutionManager.stopAll();
				   PathFinder.findAndSetPathAsync(player.getWorld(),new Vec3i(Main.TARGET.x,Main.TARGET.y,Main.TARGET.z),this);
				   end(options);


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

				//we just trust the sim that we are placing the right block :d
				//TODO try to equip block
				if (node.input.placedBlock != null) {
					player.sendMessage(Text.of("placing"));
					BridgeExecutor.placeBlock();
				}

				/*if (tick+1 < path.size() && path.get(tick+1).input.placedBlock != null) {
					player.sendMessage(Text.of("placing"));
					System.out.println("tried to place block");
					BridgeExecutor.placeBlock();
				}*/

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

	@Override
	public Vec3i getTarget() {
		return target;
	}
}
