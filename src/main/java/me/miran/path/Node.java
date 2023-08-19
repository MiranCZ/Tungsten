package me.miran.path;

import me.miran.agent.Agent;
import me.miran.render.Color;
import net.minecraft.world.WorldView;

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

    public Node(Node parent, WorldView world, PathInput input, Color color, int pathCost) {
        this.parent = parent;
        this.agent = Agent.of(parent.agent, input).tick(world);
        this.input = input;
        this.color = color;
        this.pathCost = pathCost;
    }

    public Node[] getChildren(WorldView world) {
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

        if (this.agent.onGround || this.agent.touchingWater) {
            return new Node[]{
                    new Node(this, world, new PathInput(true, false, false, false, true,
                            false, true, this.agent.pitch, this.agent.yaw), new Color(0, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, true,
                            false, false, this.agent.pitch, this.agent.yaw), new Color(0, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw), new Color(255, 0, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, false, this.agent.pitch, this.agent.yaw), new Color(255, 0, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw + 90.0F), new Color(255, 255, 0), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw - 90.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw + 45.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw - 45.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw + 20.0F), new Color(255, 0, 255), this.pathCost + 1),
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw - 20.0F), new Color(255, 0, 255), this.pathCost + 1),
            };
        } else {
            return new Node[]{
                    new Node(this, world, new PathInput(true, false, false, false, false,
                            false, true, this.agent.pitch, this.agent.yaw), new Color(0, 255, 255), this.pathCost + 1),
            };
        }
    }

}
