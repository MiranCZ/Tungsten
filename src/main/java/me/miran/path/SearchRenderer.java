package me.miran.path;

import me.miran.Main;
import me.miran.render.Line;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SearchRenderer extends Thread{


    private boolean running = true;
    private final ConcurrentLinkedQueue<Node> renderQueue = new ConcurrentLinkedQueue<>();


    @Override
    public void run() {
        Main.RENDERERS.clear();

        while (running) {
            for (Node n : renderQueue) {
                addRenderNode(n);
            }
            renderQueue.clear();
        }

        Main.RENDERERS.clear();
    }

    private void addRenderNode(Node node) {
        if(Main.RENDERERS.size() > 5000) {
            Main.RENDERERS.clear();
        }

        Main.RENDERERS.add(new Line(node.agent.getPos(), node.parent.agent.getPos(), node.color));
    }


    public void addNode(Node node) {
        if (node.parent == null) return;
        renderQueue.add(node);
    }


    public void endRender() {
        running = false;
    }

}
