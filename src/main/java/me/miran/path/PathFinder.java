package me.miran.path;

import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.render.Color;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PathFinder {

	private static Thread searchThread = null;
	public static boolean shouldStop = false;

	private static final long timeLimitMs = 1000;

	public static void find(WorldView world, Vec3d target) {
		if(searchThread != null)return;

		new Thread(()->findThread(world, target)).start();
	}


	private static void findThread(WorldView world, Vec3d target) {
		shouldStop = false;

		neededDist = 1;

		Agent agent = Agent.of(Objects.requireNonNull(MinecraftClient.getInstance().player));

		List<Node> path1 = new ArrayList<>();
		AtomicReference<List<Node>> path2 = new AtomicReference<>();

		long time = System.currentTimeMillis();
		CountDownLatch latch = new CountDownLatch(1);

		searchThread = new Thread(() -> {
			try {
				path2.set(search(world, target, new Node(null, agent, null, 0), 100, Calculators.A_STAR));
			} catch (Exception e) {
				e.printStackTrace();
			}
			searchThread = null;
			shouldStop = true;
			Main.RENDERERS.clear();
			latch.countDown();

		});
		searchThread.start();

		try {
			path1 = search(world, target, new Node(null, agent, null, 0), 20, Calculators.GREEDY);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (System.currentTimeMillis()< time + timeLimitMs) {
			try {
				latch.await((time+timeLimitMs)-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
        shouldStop = latch.getCount() != 0;

		if (path2.get() != null && !path2.get().isEmpty()) {
			MinecraftClient.getInstance().player.sendMessage(Text.of("A*"));
			Main.EXECUTOR.setPath(path2.get());
		} else {
			MinecraftClient.getInstance().player.sendMessage(Text.of("GREEDY"));
			Main.EXECUTOR.setPath(path1);
		}
		Main.RENDERERS.clear();
		shouldStop = false;
	}

	private static double neededDist;


	private static List<Node> search(WorldView world, Vec3d target, Node start,int blockLimit, HeuristicCalculator calculator ) {
		Main.RENDERERS.clear();
		List<Node> path = null;

		Map<BlockPos,Integer> map = new HashMap<>();


		ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);

		Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(o ->o.heuristic));
		Set<Vec3d> closed = new HashSet<>();

		open.add(start);

		int repeatedTimes = -1;

		HashSet<BlockPos> set = new HashSet<>();

		while(!open.isEmpty() && !shouldStop) {
			Node next = open.poll();
			closed.add(next.agent.getPos());
			if(closed.size() > 1_000_000)break;


			if(next.agent.getPos().squaredDistanceTo(target) <= neededDist /*&& next.agent.onGround*/) {
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
				if(closed.contains(child.agent.getPos()))continue;


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

				child.heuristic = calculator.calculate(next,start,child,target);

				open.add(child);

				if(Main.RENDERERS.size() > 5000) {
					Main.RENDERERS.clear();
				}

				Main.RENDERERS.add(new Line(child.agent.getPos(), child.parent.agent.getPos(), child.color));
			}

			if (set.size() > 600) {
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


					List<Node> l = search(world,targetPos ,start,40,Calculators.A_STAR);
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
				Main.RENDERERS.clear();
			}

		});
		mismatchSearchThread.start();
	}


}
