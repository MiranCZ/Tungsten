package me.miran.path;

import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.*;

public class PathFinder {

	public static boolean active = false;

	public static void find(WorldView world, Vec3d target) {
		if(active)return;
		active = true;

		new Thread(() -> {
			try {
				search(world, target);
			} catch(Exception e) {
				e.printStackTrace();
			}

			active = false;
		}).start();
	}

	private static void search(WorldView world, Vec3d target) {
		Main.RENDERERS.clear();

		ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);

		Node start = new Node(null, Agent.of(player), null, 0);

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o -> o.pathCost + o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		while(!open.isEmpty()) {
			Node next = open.poll();
			closed.add(next.agent.getPos());
			if(closed.size() > 100000)break;

			if(next.agent.getPos().squaredDistanceTo(target) <= 1.0D && next.agent.onGround) {
				Main.RENDERERS.clear();
				Node n = next;
				List<Node> path = new ArrayList<>();

				while(n.parent != null) {
					path.add(n);
					Main.RENDERERS.add(new Line(n.agent.getPos(), n.parent.agent.getPos(), n.color));
					Main.RENDERERS.add(new Cuboid(n.agent.getPos().subtract(0.05D, 0.05D, 0.05D), new Vec3d(0.1D, 0.1D, 0.1D), n.color));
					n = n.parent;
				}

				path.add(n);
				Collections.reverse(path);
				Main.EXECUTOR.setPath(path);
				break;
			}

			for(Node child : next.getChildren(world)) {
				if(closed.contains(child.agent.getPos()))continue;
				child.heuristic = child.pathCost / child.agent.getPos().distanceTo(start.agent.getPos()) * child.agent.getPos().distanceTo(target);
				//child.heuristic = 20.0D * child.frame.getPos().distanceTo(target);
				open.add(child);

				if(Main.RENDERERS.size() > 5000) {
					Main.RENDERERS.clear();
				}

				Main.RENDERERS.add(new Line(child.agent.getPos(), child.parent.agent.getPos(), child.color));
			}
		}
	}

}
