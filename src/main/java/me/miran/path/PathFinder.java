package me.miran.path;

import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.path.calculators.BaritoneCalculator;
import me.miran.path.calculators.HeuristicCalculator;
import me.miran.render.Color;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;

import java.util.*;

public class PathFinder {

	private PathFinder() {

	}



	private static boolean running = false;

	public static void forceStop() {
		running = false;
	}


	public static void findAndSetPathAsync(WorldView world, Vec3d target) {
		if(running)return;
		running = true;

		BaritonePathFinder.initContext();
		nodesExplored = 0;

		new Thread(()-> {
			findThread(world, target);
			running = false;
		}).start();
	}

	private static void findThread(WorldView world, Vec3d target) {
		if (MinecraftClient.getInstance().player == null) return;

		long startMillis = System.currentTimeMillis();

		PlayerEntity player = MinecraftClient.getInstance().player;
		Vec3d t = Main.TARGET;

		IPath baritonePath = calculateBaritonePath(player,new BetterBlockPos(t.x,t.y,t.z));
		if (baritonePath == null) {
			player.sendMessage(Text.literal("Path was not found :(").formatted(Formatting.RED));
			return;
		}

		renderBaritonePath(baritonePath.positions());


		try {
			List<BetterBlockPos> positions = baritonePath.positions();
			List<Node> path = new ArrayList<>();
			Agent agent = Agent.of(MinecraftClient.getInstance().player);
			Node prevNode = null;
			boolean set = false;
			int sectionNum = 0;

			int i = 0;
			while (i < positions.size()) {
				sectionNum++;

				long millis = System.currentTimeMillis();

				int nextI = i+10;
				if (nextI >= positions.size()) nextI = positions.size();
				BlockPos sectionTargetPos = positions.get(nextI-1);
				Vec3d sectionTarget = new Vec3d(sectionTargetPos.getX(),sectionTargetPos.getY(),sectionTargetPos.getZ());

				HeuristicCalculator calculator = new BaritoneCalculator(positions.subList(i,nextI));
				List<Node> pathSection = search(world,sectionTarget , new Node(null,agent , null, 0), 100, calculator,1);
				if (prevNode != null) {
					pathSection.remove(0);
				}
				if (pathSection.size() > 20 && nextI < positions.size()) {
					pathSection = pathSection.subList(0,pathSection.size()-20);
				}

				pathSection.get(0).parent = prevNode;

				path.addAll(pathSection);
				Main.EXECUTOR.renderCurrentPath();
				player.sendMessage(Text.of("Section "+sectionNum +" finished ("+ (System.currentTimeMillis()-millis)+"ms) " + pathSection.size() ));

				if (path.size() > 40 && !set) {
					set = true;
					Main.EXECUTOR.setPath(path);
				}
				prevNode = pathSection.get(pathSection.size()-1);
				agent = prevNode.agent;


				i = nextI;
			}
			if (!path.isEmpty()) {
				sendSuccessMessage(player, System.currentTimeMillis() - startMillis);
			}
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(Text.literal("Something went wrong while calculating path (" + e.getMessage() +")").formatted(Formatting.RED));
		}

	}

	private static void sendSuccessMessage(PlayerEntity player, long timeElapsedMs) {
		player.sendMessage(
				Text.literal("Path found! ").formatted(Formatting.DARK_GREEN)
						.append(Text.literal(nodesExplored + " nodes explored in " +timeElapsedMs + " ms").formatted(Formatting.AQUA))
						.append(Text.literal(" (" + ((int)Math.floor(nodesExplored/(timeElapsedMs/1000d))) +" nodes per second)").formatted(Formatting.GRAY))
		);
	}

	private static void renderBaritonePath(List<BetterBlockPos> positions) {
		Main.TEST.clear();
		for (int i = 1; i < positions.size(); i++) {
			BetterBlockPos start = positions.get(i-1);
			BetterBlockPos end = positions.get(i);

			Main.TEST.add(new Line(new Vec3d(start.x+0.5,start.y+0.1,start.z+0.5),new Vec3d(end.x+0.5,end.y+0.1,end.z+0.5), new Color(255,0,0) ));
		}
	}

	private static IPath calculateBaritonePath(PlayerEntity player,BlockPos pos) {
		try {
			return BaritonePathFinder.tryToFindPath(player.getBlockPos(), new GoalBlock(pos));
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(Text.literal("Something went wrong while calculating baritone path (" + e.getMessage() +")").formatted(Formatting.RED));
			return null;
		}
	}

	public static List<Node> findPathSync(WorldView world, Vec3d target, Node start, int blockLimit, HeuristicCalculator calculator, double neededDist) {
		running = true;
		List<Node> list = search(world, target, start, blockLimit, calculator, neededDist);
		running = false;
		return list;
	}

	private static int nodesExplored = 0;

	private static List<Node> search(WorldView world, Vec3d target, Node start,int blockLimit, HeuristicCalculator calculator, double neededDist) {
		assert blockLimit < Short.MAX_VALUE;

		SearchRenderer renderer = new SearchRenderer();
		renderer.start();

		List<Node> path = null;

		Map<Vec3i,Short> map = new HashMap<>();

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o ->o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		int blocksCount = 0;

		while(!open.isEmpty() && running) {
			nodesExplored++;

			Node next = open.poll();
			if (next.agent.likelyTookDamage) continue;

			closed.add(next.agent.getPos());
			if(closed.size() > 1_000_000) break;


			if(next.agent.squaredDistanceTo(target) <= neededDist && next.agent.onGround) {
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

				renderer.addNode(child);

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
				renderer.endRender();
				path = recalculatePathWithHigherBlockLimit(world,target,start,blockLimit,calculator);
				break;
			}

		}
		renderer.endRender();

		if (path == null) {
			path = recalculatePathWithHigherBlockLimit(world,target,start,blockLimit,calculator);
		}

		return path;
	}

	private static List<Node> recalculatePathWithHigherBlockLimit(WorldView world, Vec3d target,Node start, int blockLimit, HeuristicCalculator calculator){
		if (blockLimit > 2000) {
			return new ArrayList<>();
		}
		blockLimit *= 2;

		return search(world, target,start,blockLimit,calculator,1);
	}


}
