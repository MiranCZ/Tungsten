package me.miran.path;

import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.path.calculators.Calculators;
import me.miran.render.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PathRebuilder {



    private static boolean running = false;

    public static void calculateContinuedPathWithMismatch(WorldView world, final List<Node> path, int tick) {
        if (running) return;
        running = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.EXECUTOR.calculating = true;
                try {
                    runSearch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Main.EXECUTOR.calculating = false;
                running = false;
            }



            private void runSearch() {

                ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
                Node expectedStart = new Node(null, Agent.of(player), Color.WHITE, 0);


                Node start = new Node(null, Agent.of(player), Color.WHITE, 0);

                Node lastNode = null;
                List<Node> path2 = path.subList(tick, path.size());//remove already executed nodes



                for (int i = 0;i < path2.size();) {
                    if (!running) return;

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

                    boolean finalSection = false;
                    if (i >= path2.size()) {
                        targetPos = Main.TARGET;
                        finalSection = true;
                    }


                    List<Node> l = PathFinder.findPathSync(world,targetPos ,start,40, Calculators.A_STAR,finalSection?1:5);
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

        }).start();

    }

}
