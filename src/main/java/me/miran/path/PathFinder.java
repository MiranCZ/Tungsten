package me.miran.path;

import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.path.calculators.BaritoneCalculator;
import me.miran.path.calculators.Calculators;
import me.miran.path.calculators.HeuristicCalculator;
import me.miran.render.Color;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;

import java.util.*;

public class PathFinder {

	private static Thread searchThread = null;
	public static boolean shouldStop = false;

	public static void find(WorldView world, Vec3d target) {
		if(searchThread != null)return;
		shouldStop = false;

		BaritonePathFinder.initContext();
		nodesExplored = 0;

		startRenderThread();

		searchThread = new Thread(()->findThread(world, target));
		searchThread.start();
	}

	private static void startRenderThread() {
		//probably not the best way  to implement it
		//TODO: maybe improve this in the future
		new Thread(()-> {
			System.out.println("RENDER THREAD START");

			HashSet<Node> set = new HashSet<>();

			while (!shouldStop) {
				final Node n = renderNode;

				if (n == null || n.parent == null || set.contains(n)) continue;

				set.add(n);

				if(Main.RENDERERS.size() > 5000) {
					Main.RENDERERS.clear();
				}

				Main.RENDERERS.add(new Line(n.agent.getPos(), n.parent.agent.getPos(), n.color));
			}

			System.out.println("RENDER THREAD END");
		}).start();
	}


	private static Node renderNode;

	private static void findThread(WorldView world, Vec3d target) {
		long startMillis = System.currentTimeMillis();

		neededDist = 1;

		Agent agent = Agent.of(Objects.requireNonNull(MinecraftClient.getInstance().player));

		List<Node> path1 = new ArrayList<>();

		PlayerEntity player = MinecraftClient.getInstance().player;
		Vec3d t = Main.TARGET;
		BlockPos pos = new BetterBlockPos(t.x,t.y,t.z);

		IPath path;
		try {
			path = BaritonePathFinder.tryToFindPath(player.getBlockPos(), new GoalBlock(pos));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (path == null) {
			player.sendMessage(Text.literal("path not found").formatted(Formatting.RED));
			shouldStop = false;
			searchThread = null;
			return;
		}

		HeuristicCalculator calculator = new BaritoneCalculator(new ArrayList<>(path.positions()));

		List<BetterBlockPos> positions = path.positions();

		Main.TEST.clear();
		for (int i = 1; i < positions.size(); i++) {
			BetterBlockPos start = positions.get(i-1);
			BetterBlockPos end = positions.get(i);

			Main.TEST.add(new Line(new Vec3d(start.x+0.5,start.y+0.1,start.z+0.5),new Vec3d(end.x+0.5,end.y+0.1,end.z+0.5), new Color(255,0,0) ));

		}

		try {
			path1 = search(world, target, new Node(null, agent, null, 0), 10, calculator);
		} catch (Exception e) {
			e.printStackTrace();
		}


		Main.EXECUTOR.setPath(path1);

		Main.RENDERERS.clear();
		shouldStop = false;
		searchThread = null;

		long timeElapsed = System.currentTimeMillis()-startMillis;
		player.sendMessage(
				Text.literal("Path found! ").formatted(Formatting.DARK_GREEN)
						.append(Text.literal(nodesExplored + " nodes explored in " +timeElapsed + " ms").formatted(Formatting.AQUA))
						.append(Text.literal(" (" + ((int)Math.floor(nodesExplored/(timeElapsed/1000d))) +" nodes per second)").formatted(Formatting.GRAY))
		);

		shouldStop = true;
	}

	private static double neededDist;
	private static int nodesExplored = 0;


	private static List<Node> search(WorldView world, Vec3d target, Node start,int blockLimit, HeuristicCalculator calculator ) {
		assert blockLimit < Short.MAX_VALUE;

		Main.RENDERERS.clear();
		List<Node> path = null;

		Map<Vec3i,Short> map = new HashMap<>();


		ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o ->o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		int blocksCount = 0;

		while(!open.isEmpty() && !shouldStop) {
			nodesExplored++;

			Node next = open.poll();
			renderNode = next;

			closed.add(next.agent.getPos());
			if(closed.size() > 1_000_000)break;


			if(next.agent.squaredDistanceTo(target) <= neededDist /*&& next.agent.onGround*/) {
				path = new ArrayList<>();

				Node n = next;

				while(n.parent != null) {
					path.add(n);
					n = n.parent;
				}

				path.add(n);
				Collections.reverse(path);
				break;
			}

			for(Node child : next.getChildren(world)) {

				if(child.agent.touchingWater || closed.contains(child.agent.getPos())) continue;

				if (child.agent.onGround) {
					Vec3i pos = new Vec3i(child.agent.blockX,child.agent.blockY,child.agent.blockZ);
					int repeat = map.getOrDefault(pos, (short) 0);
					map.put(pos, (short) (repeat+1));

					if (repeat == blockLimit) {
						blocksCount++;
						continue;
					}

					if (repeat >= blockLimit) {
						continue;
					}
				}

				child.heuristic = calculator.calculate(next,start,child,target);

				open.add(child);
			}

			if (blocksCount > 600) {
				path = recalculatePathWithHigherBlockLimit(player,world,target,start,blockLimit,calculator);
				break;
			}

		}
		if (path == null) {
			path = recalculatePathWithHigherBlockLimit(player,world,target,start,blockLimit,calculator);
		}

		return path;
	}

	private static List<Node> recalculatePathWithHigherBlockLimit(PlayerEntity player,WorldView world, Vec3d target,Node start, int blockLimit, HeuristicCalculator calculator){
		if (blockLimit > 2000) {
			return new ArrayList<>();
		}
		blockLimit *= 2;

		return search(world, target,start,blockLimit,calculator);
	}

	private static Thread mismatchSearchThread = null;

	public static void calculateContinuedPathWithMismatch(WorldView world,final List<Node> path, int tick) {
		if (mismatchSearchThread != null) return;

		neededDist = 5;
		mismatchSearchThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Main.EXECUTOR.calculating = true;
				try {
					runSearch();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Main.EXECUTOR.calculating = false;
				mismatchSearchThread = null;
			}



			private void runSearch() {

				ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
				Node expectedStart = new Node(null, Agent.of(player), Color.WHITE, 0);


				Node start = new Node(null, Agent.of(player), Color.WHITE, 0);

				Node lastNode = null;
				List<Node> path2 = path.subList(tick, path.size());//remove already executed nodes



				for (int i = 0;i < path2.size();) {
					if (shouldStop) return;

					Node a = path2.get(path2.size()-1);
					Node lastGround = null;
					int ticks = 0;
					for (Node node : path2.subList(i,path2.size())) {
						ticks++;
						if (ticks > 20 && lastGround != null) {
							a = lastGround;
							break;
						}
						if (node.agent.onGround) {
							lastGround = node;
						}
					}
					i = path2.indexOf(a)+1;

					Vec3d targetPos = a.agent.getPos();
					if (i >= path2.size()) {
						targetPos = Main.TARGET;
						neededDist = 1;//needs to walk fully to the target
					}


					List<Node> l = search(world,targetPos ,start,40, Calculators.A_STAR);
					lastNode = l.get(l.size()-1);
					start =lastNode;



					if (!expectedStart.agent.compare(player,false)) {
						//PathExecutor will call this function again, so there is no point in trying to come up with a path
						return;
					}
				}


				Node n = lastNode;

				List<Node> l = new ArrayList<>();
				while(n != null) {
					l.add(n);
					n = n.parent;
				}
				Collections.reverse(l);

				Main.EXECUTOR.setPath(l);
				Main.RENDERERS.clear();
			}

		});
		mismatchSearchThread.start();
	}


}
