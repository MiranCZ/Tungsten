package me.miran.path;

import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.render.Color;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
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
		neededDist = 1;
		searchThread = new Thread(() -> {
			try {
				Main.EXECUTOR.setPath(search(world, target,new Node(null, Agent.of(Objects.requireNonNull(MinecraftClient.getInstance().player)), null, 0)));
			} catch(Exception e) {
				e.printStackTrace();
			}

			searchThread = null;
			shouldStop = false;
		});
		searchThread.start();
	}

	private static int blockLimit;
	private static double neededDist;

	private static List<Node> search(WorldView world, Vec3d target, Node start) {
		Main.RENDERERS.clear();
		List<Node> path = null;

		Map<BlockPos,Integer> map = new HashMap<>();


		ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);

		//Node start = new Node(null, Agent.of(player), null, 0);

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o -> o.pathCost+ o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		int repeatedTimes = -1;

		HashSet<BlockPos> set = new HashSet<>();

		while(!open.isEmpty() && !shouldStop) {
			Node next = open.poll();
			closed.add(next.agent.getPos());
			if(closed.size() > 1_000_000)break;


			if(next.agent.getPos().squaredDistanceTo(target) <= neededDist && next.agent.onGround) {
				path = new ArrayList<>();
				Main.RENDERERS.clear();
				Node n = next;


				while(n.parent != null) {
					path.add(n);
					Main.RENDERERS.add(new Line(n.agent.getPos(), n.parent.agent.getPos(), n.color));
					Main.RENDERERS.add(new Cuboid(n.agent.getPos().subtract(0.05D, 0.05D, 0.05D), new Vec3d(0.1D, 0.1D, 0.1D), n.color));
					n = n.parent;
				}

				path.add(n);
				Collections.reverse(path);
				break;
			}

			for(Node child : next.getChildren(world)) {
				if(closed.contains(child.agent.getPos()))continue;
				double heuristic = 20.0D * child.agent.getPos().distanceTo(target);
				//double heuristic = child.pathCost / child.agent.getPos().distanceTo(start.agent.getPos()) * child.agent.getPos().distanceTo(target);


				if (child.agent.onGround) {
					BlockPos pos = new BlockPos(child.agent.blockX,child.agent.blockY,child.agent.blockZ);
					map.put(pos,map.getOrDefault(pos,0)+1);
					int repeat = map.get(pos);
					if (repeat > repeatedTimes) {
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

			//System.out.println(set.size());
			if (set.size() > 250) {
				path = recalculatePathWithHigherBlockLimit(player,world,target,start);
			}


		}

		if (path == null) {
			if (blockLimit < 1000) {
				path = recalculatePathWithHigherBlockLimit(player,world,target,start);
			} else {
				player.sendMessage(Text.literal("Wasn't able to find a path... sorry :(").formatted(Formatting.DARK_RED));
				path = new ArrayList<>();
			}
		}

		return path;
	}

	private static List<Node> recalculatePathWithHigherBlockLimit(PlayerEntity player,WorldView world, Vec3d target,Node start){
		if (blockLimit == 200) {
			blockLimit = 1000;
		} else if (blockLimit == 50) {
			blockLimit = 200;
		} else if (blockLimit == 10) {
			blockLimit = 50;
		}


		player.sendMessage(Text.literal("Limit too low, recalculating with " + blockLimit).formatted(Formatting.DARK_AQUA));
		return search(world, target,start);
	}

	public static void calculateContinuedPathWithMismatch(WorldView world,final List<Node> path, int tick) {
		neededDist = 5;
		new Thread(new Runnable() {
			@Override
			public void run() {

				ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
				Node expectedStart = new Node(null, Agent.of(player), Color.WHITE, 0);

				Node start = new Node(null, Agent.of(player), Color.WHITE, 0);

				Node lastNode = null;
				List<Node> path2 = path.subList(tick, path.size());//remove already executed nodes



				for (int i = 0;i < path2.size();) {
					Node a = path2.get(path2.size()-1);
					Node lastGround = null;
					for (Node node : path2.subList(i,path2.size())) {
						if (node.agent.getPos().distanceTo(start.agent.getPos()) > 10 && lastGround != null) {
							a = lastGround;
							break;
						}
						if (node.agent.onGround) {
							lastGround = node;
						}
					}
					i = path2.indexOf(a)+1;
					if (i >= path2.size()) {
						neededDist = 1;//needs to walk fully to the target
					}


					List<Node> l = search(world, a.agent.getPos(),start);
					lastNode = l.get(l.size()-1);
					start =lastNode;



					if (!expectedStart.agent.compare(player,false)) {
						//PathExecutor will call this function again, so there is no point in trying to come up with a path
						return;
					}
				}


				Node n = lastNode;

				List<Node> l = new ArrayList<>();
				while(n.parent != null) {
					l.add(n);
					n = n.parent;
				}
				l.add(n);
				Collections.reverse(l);

				Main.EXECUTOR.setPath(l);
			}
		}).start();
	}


}
