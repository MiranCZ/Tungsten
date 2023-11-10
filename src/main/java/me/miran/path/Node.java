package me.miran.path;

import me.miran.WorldMixin;
import me.miran.agent.Agent;
import me.miran.agent.AgentRaycastContext;
import me.miran.render.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.*;

public class Node {

    public Node parent;
    public Agent agent;
    public PathInput input;
    public int pathCost;
    public double heuristic;
    public Color color;

    public Node(Node parent, Agent agent, Color color, int pathCost) {
        this.parent = parent;
        this.agent = agent;
        this.color = color;
        this.pathCost = pathCost;
    }

    public Node(Node parent, World world, PathInput input, Color color, int pathCost) {
        this.parent = parent;
        this.agent = Agent.of(parent.agent, input).tick(world);
        this.input = input;
        this.color = color;
        this.pathCost = pathCost;
    }

    public Node[] getChildren(World world, List<BlockPos> toPlace) {
        Node n = this.parent;
     //   boolean mismatch = false;
     //   int i;


     //   for (i = 0; i < 4 && n != null; i++) {
        if (n != null && n.agent.posX == this.agent.posX && n.agent.posY == this.agent.posY && n.agent.posZ == this.agent.posZ) {
            return new Node[]{};
        }



      /*      n = n.parent;
        }

        if (!mismatch && i == 5) {
            return new Node[]{};
        }*/

        ((WorldMixin)world).tungsten$setCalcThread(Thread.currentThread());
        ((WorldMixin)world).tungsten$setBlockList(agent.placedBLocks);

        Node[] nodes = getIdk(world);
        Node[] placeNodes = getPlacesNodes(toPlace,world);
        if (placeNodes != null) {
           // Node[] returnNodes = new Node[placeNodes.length];
            List<Node> returnNodes = new ArrayList<>();

            for (Node placeNode : placeNodes) {
                Agent a = Agent.of(this.agent, placeNode.input);
                a.tick(world);

                Vec2f look = agentTest(placeNode.input.placedBlock, a);
                if (look == null || a.box.intersects(new Box(placeNode.input.placedBlock,placeNode.input.placedBlock.add(1.1,1.1,1.1)))) continue;

                placeNode.input.pitch = look.x;
                placeNode.input.yaw = look.y;
                returnNodes.add(new Node(this, world, placeNode.input, new Color(255,255,255), this.pathCost + 2));
            }

            if (!returnNodes.isEmpty()) {
                placeNodes= returnNodes.toArray(new Node[0]);
            } else {
                ((WorldMixin)world).tungsten$clear();
                return nodes;
            }
        } else {
            ((WorldMixin)world).tungsten$clear();
            return nodes;
        }


        ((WorldMixin)world).tungsten$clear();
        return concatWithArrayCopy(nodes,placeNodes);
      /*  if (placeRotation == null) return nodes;

        Node[] placeNodes = new Node[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            PathInput inp = node.input;
            placeNodes[i] = new Node(this,world,new PathInput(inp.forward,inp.back,inp.right,inp.left,inp.jump,inp.sneak,inp.sprint,placeRotation.x,placeRotation.y,placePos),new Color(0,0,0) ,this.pathCost+1);
        }

        Node[] result = Arrays.copyOf(nodes, nodes.length + placeNodes.length);
        System.arraycopy(placeNodes, 0, result, nodes.length, placeNodes.length);
        return placeNodes;*/
    }

    /*static Node[] concatWithCollection(Node[] array1, Node[] array2) {
        List<Node> resultList = new ArrayList<>(array1.length + array2.length);
        Collections.addAll(resultList, array1);
        Collections.addAll(resultList, array2);

        return resultList.toArray(new Node[0]);
    }*/
    static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private Node[] getPlacesNodes(List<BlockPos> toPlace, World world) {
        for (BlockPos pos : toPlace) {
            if (agent.box.intersects(new Box(pos,pos.add(1,1,1))) || parent == null) continue;

            pos = new BlockPos(pos.getX(),pos.getY(),pos.getZ());
            if (pos.isWithinDistance(agent.getPos(), 5) && (agent.placedBLocks == null || !agent.placedBLocks.containsKey(pos))) {
                Vec2f v = agentTest(pos,this.agent);
                if (v != null) {


                    return new Node[]{

                            new Node(this, world,new PathInput(true, false, false, false, true,
                                    false, false, v.x,v.y, pos), new Color(0, 255, 0) , this.pathCost + 1),
                            new Node(this, world, new PathInput(true, false, false, false, false,
                                    false, false, v.x,v.y, pos), new Color(0, 255, 0), this.pathCost + 1),
                            new Node(this, world,new PathInput(true, false, false, false, true,
                                    false, true, v.x,v.y, pos), new Color(0, 255, 0) , this.pathCost + 1),
                            new Node(this, world, new PathInput(true, false, false, false, false,
                                    false, true, v.x,v.y, pos), new Color(0, 255, 0), this.pathCost + 1),

                            /* new Node(this, world, new PathInput(false, false, false, false, false,
                                     true, false, v.x,v.y, pos), new Color(0, 255, 0), this.pathCost + 1)*/
                    };
                }
            }
        }
        return null;
    }

    private Node[] getIdk(World world) {
        if (this.agent.onGround || this.agent.touchingWater) {
            float pitch = this.agent.pitch;
            return new Node[]{
                    new Node(this, world, new PathInput(true, false, false, false, true,
                            false, true, pitch, this.agent.yaw), new Color(0, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, true,
                            false, false, pitch, this.agent.yaw), new Color(0, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw), new Color(255, 0, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, false, pitch, this.agent.yaw), new Color(255, 0, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw + 90.0F), new Color(255, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw - 90.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw + 45.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw - 45.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw + 20.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, pitch, this.agent.yaw - 20.0F), new Color(255, 0, 255), this.pathCost + 1),
            };
        } else {
            return new Node[]{
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw), new Color(0, 255, 255), this.pathCost + 1),
            };
        }
    }

    public static Vec2f agentTest(BlockPos pos, Agent agent) {
        MinecraftClient mc = MinecraftClient.getInstance();
       // Agent agent = Agent.of(mc.player);
        //  mc.world.setBlockState(mc.player.getBlockPos(), Blocks.DIAMOND_BLOCK.getDefaultState());


        for (Direction direction : Direction.values()) {
            BlockPos dirPos = pos.offset(direction);
            if (baritone.bx.c(PathFinder.bsi, dirPos.getX(),dirPos.getY(),dirPos.getZ())) {
                Vec3d vPos = new Vec3d(dirPos.getX(),dirPos.getY(),dirPos.getZ());

                Vec3i dirV = direction.getVector();
                vPos = vPos.add(0.5,0.5,0.5);
                vPos = vPos.add(new Vec3d(dirV.getX()*-0.4d,dirV.getY()*-0.4d,dirV.getZ()*-0.4d));

                agent.lookAt(vPos);

                if (raycast(agent,direction, vPos,mc.world)) {
                    //mc.player.sendMessage(Text.of(agent.yaw + " ; " + agent.pitch));
                    return new Vec2f(agent.pitch,agent.yaw);
                }
            }


        }
        return null;
    }

    private static boolean raycast(Agent player, Direction neededDir, Vec3d target, WorldView world) {
        float maxDistance = 4;
        Vec3d rot = player.getRotationVector();
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        BlockHitResult rayTraceResult = world.raycast(new AgentRaycastContext(start, end, RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE, player));//MinecraftClient.getInstance().player.raycast(10, 0, false);
        if (rayTraceResult.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos blockPos = rayTraceResult.getBlockPos();

            if (blockPos.getX() == Math.floor(target.x) && blockPos.getY() == Math.floor(target.y) && blockPos.getZ() == Math.floor(target.z)) {
                return rayTraceResult.getSide().equals(neededDir.getOpposite());
            } else {
                return false;
            }

        }
        return false;
    }

}
