/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.swinggamelibrary;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dkrou
 */
public class Node implements INode {

    private boolean visible;
    private boolean removedFromParent;
    private INode parent;
    private final Rectangle2D.Double rectangle;
    private final List<INode> nodes = Collections.synchronizedList(new ArrayList<>());
    private int zOrder = 0;
    private boolean hasRendered;

    public Node() {
        visible = true;
        rectangle = new Rectangle2D.Double(0, 0, 0, 0);
    }

    public Node(int width, int height) {
        visible = true;
        rectangle = new Rectangle2D.Double(0, 0, width, height);
    }

    public Node(int worldX, int worldY, int width, int height) {
        visible = true;
        rectangle = new Rectangle2D.Double(worldX, worldY, width, height);
    }

    @Override
    public void update(long elapsedTime) {
        getNodes().stream().filter((node) -> (node.isVisible())).forEachOrdered((node) -> {
            node.update(elapsedTime);
        });
    }

    @Override
    public void render(Graphics2D g2d) {
        // draw all Sprites to the screen which are visible and or havent been removed from the scene
        Iterator<INode> spriteIterator = getNodes().iterator();
        while (spriteIterator.hasNext()) {
            INode node = (INode) spriteIterator.next();
            // draw the object to JPanel
            if (node.isRemovedFromParent()) {
                node.onExit();
                remove(node);
            } else {
                if (node.isVisible()) {
                    if (!node.hasRendered()) {
                        node.onEnter();
                    }
                    node.render(g2d);
                }
            }
        }
        hasRendered = true;
    }

    @Override
    public void add(INode node) {
        synchronized (nodes) {
            // TODO perhaps throw an exception
            if (node.getParent() != null) {
                return;
            }

            node.setParent(this);
            nodes.add(node);

            // sort nodes by z order
            List<INode> unsortedNodes = new ArrayList<>(nodes);
            nodes.clear();
            nodes.addAll(unsortedNodes.stream().sorted(Comparator.comparingInt(n -> n.getZOrder())).collect(Collectors.toList()));
        }
    }

    @Override
    public void remove(INode node) {
        synchronized (nodes) {
            Iterator<INode> nodeIterator = nodes.iterator();
            while (nodeIterator.hasNext()) {
                if (nodeIterator.next().equals(node)) {
                    node.setParent(null);
                    node.removeFromParent();
                    nodeIterator.remove();
                }
            }
        }
    }

    @Override
    public void removeAll() {
        synchronized (nodes) {
            Iterator<INode> nodeIterator = nodes.iterator();
            while (nodeIterator.hasNext()) {
                INode node = nodeIterator.next();
                node.setParent(null);
                node.removeFromParent();
                nodeIterator.remove();
            }
        }
    }

    @Override
    public List<INode> getNodes() {
        synchronized (nodes) {
            return new ArrayList<>(nodes);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void removeFromParent() {
        removedFromParent = true;
        visible = false;
    }

    @Override
    public boolean isRemovedFromParent() {
        return removedFromParent;
    }

    @Override
    public INode getParent() {
        return parent;
    }

    @Override
    public void setParent(INode parent) {
        this.parent = parent;
    }

    @Override
    public void setWorldX(int x) {
        rectangle.x = x;
    }

    @Override
    public void setWorldY(int y) {
        rectangle.y = y;
    }

    @Override
    public int getWorldX() {
        return getParent() != null ? getParent().getWorldX() + (int) rectangle.x : (int) rectangle.x;
    }

    @Override
    public int getWorldY() {
        return getParent() != null ? getParent().getWorldY() + (int) rectangle.y : (int) rectangle.y;
    }

    @Override
    public int getScreenX() {
        return (int) (ImageScaler.getInstance().getWidthScaleFactor() * getWorldX());
    }

    @Override
    public int getScreenY() {
        return (int) (ImageScaler.getInstance().getHeightScaleFactor() * getWorldY());
    }

    @Override
    public void setScreenX(int x) {
        rectangle.x = x / ImageScaler.getInstance().getWidthScaleFactor();
    }

    @Override
    public void setScreenY(int y) {
        rectangle.y = y / ImageScaler.getInstance().getHeightScaleFactor();
    }

    @Override
    public void setWidth(int width) {
        rectangle.width = width;
    }

    @Override
    public void setHeight(int height) {
        rectangle.height = height;
    }

    @Override
    public double getWidth() {
        return rectangle.width;
    }

    @Override
    public double getHeight() {
        return rectangle.height;
    }

    @Override
    public boolean intersects(INode node) {
        if ((getWidth() <= 0.0 || getHeight() <= 0.0) || node.getWidth() <= 0 || node.getHeight() <= 0) {
            return false;
        }
        double x = node.getScreenX();
        double y = node.getScreenY();
        double x0 = getScreenX();
        double y0 = getScreenY();
        return (x + node.getWidth() > x0
                && y + node.getHeight() > y0
                && x < x0 + getWidth()
                && y < y0 + getHeight());
    }

    @Override
    public int getChildCount() {
        int childCount = nodes.size();
        Iterator<INode> nodeIterator = nodes.iterator();
        while (nodeIterator.hasNext()) {
            INode node = nodeIterator.next();
            childCount += node.getChildCount();
        }

        return childCount;
    }

    @Override
    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    @Override
    public int getZOrder() {
        return zOrder;
    }

    @Override
    public void onEnter() {
    }

    @Override
    public void onExit() {
    }

    @Override
    public boolean hasRendered() {
        return hasRendered;
    }
}
