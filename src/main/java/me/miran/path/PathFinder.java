package me.miran.path;

import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.*;

public class PathFinder {

	private static Thread searchThread = null;
	public static boolean shouldStop = false;

	public static void find(WorldView world, Vec3d target) {
		if(searchThread != null)return;
		shouldStop = false;

		blockLimit = 10;
		searchThread = new Thread(() -> {
			try {
				search(world, target);
			} catch(Exception e) {
				e.printStackTrace();
			}

			searchThread = null;
			shouldStop = false;
		});
		searchThread.start();
	}

	private static int blockLimit;

	private static void search(WorldView world, Vec3d target) {
		Main.RENDERERS.clear();


		int i = 0;

		Map<BlockPos,Integer> map = new HashMap<>();


		ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);

		Node start = new Node(null, Agent.of(player), null, 0);

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o -> o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		BlockPos repeatingPos = null;
		int repeatedTimes = -1;

		HashSet<BlockPos> set = new HashSet<>();

		while(!open.isEmpty() && !shouldStop) {
			Node next = open.poll();
			closed.add(next.agent.getPos());
			if(closed.size() > 1_000_000)break;


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
				//double heuristic = 20.0D * child.agent.getPos().distanceTo(target);
				double heuristic = child.pathCost / child.agent.getPos().distanceTo(start.agent.getPos()) * child.agent.getPos().distanceTo(target);


				if (child.agent.onGround) {
					BlockPos pos = new BlockPos(child.agent.blockX,child.agent.blockY,child.agent.blockZ);
					map.put(pos,map.getOrDefault(pos,0)+1);
					int repeat = map.get(pos);
					if (repeat > repeatedTimes) {
						repeatingPos = pos;
						repeatedTimes = repeat;
					}

					if (repeat > blockLimit) {
						set.add(pos);
					}
					if (set.contains(pos)) continue;

				}


				if (child.agent.touchingWater) {
					heuristic = Integer.MAX_VALUE;//we hate water
				}


				child.heuristic = heuristic;

				open.add(child);

				if(Main.RENDERERS.size() > 5000) {
					Main.RENDERERS.clear();
				}

				Main.RENDERERS.add(new Line(child.agent.getPos(), child.parent.agent.getPos(), child.color));
			}

			System.out.println(set.size());
			if (set.size() > 250) {
				if (blockLimit == 200) {
					blockLimit = 1000;
				} else if (blockLimit == 50) {
					blockLimit = 200;
				} else if (blockLimit == 10) {
					blockLimit = 50;
				}

				set.clear();

				player.sendMessage(Text.literal("Limit too low, recalculating with " + blockLimit).formatted(Formatting.DARK_AQUA));
				search(world, target);
				return;
			}

		}
		List<Integer> list = new ArrayList<>(map.values().stream().toList());
		Collections.sort(list);

		player.sendMessage(Text.literal(list.subList(list.size()-10,list.size()).toString()));
	}


}
