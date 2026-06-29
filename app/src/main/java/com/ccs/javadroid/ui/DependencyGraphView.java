package com.ccs.javadroid.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.ccs.javadroid.tools.bytecode.DependencyModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DependencyGraphView extends View {

    private DependencyModel model;
    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    private final Map<String, GraphNode> nodeMap = new HashMap<>();

    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgeArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrowPath = new Path();
    private final RectF hitRect = new RectF();

    private float offsetX = 0;
    private float offsetY = 0;
    private float scale = 1f;
    private GraphNode selectedNode = null;
    private String filterPackage = null;
    private int nodeColorDefault = 0xFF3574F0;
    private int nodeColorSelected = 0xFFFFA500;
    private int nodeColorPackage = 0xFF4CAF50;
    private int edgeColor = 0xFF666666;
    private int textColor = 0xFFEEEEEE;
    private int bgColor = 0xFF1E1E1E;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private boolean isPanning = false;
    private float lastTouchX, lastTouchY;

    public DependencyGraphView(Context context) {
        super(context);
        init();
    }

    public DependencyGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DependencyGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        nodePaint.setStyle(Paint.Style.FILL);
        nodePaint.setColor(nodeColorDefault);

        nodeStrokePaint.setStyle(Paint.Style.STROKE);
        nodeStrokePaint.setStrokeWidth(2f);
        nodeStrokePaint.setColor(0xFF555555);

        textPaint.setColor(textColor);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2f);
        edgePaint.setColor(edgeColor);

        edgeArrowPaint.setStyle(Paint.Style.FILL);
        edgeArrowPaint.setColor(edgeColor);

        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(nodeColorSelected);

        bgPaint.setColor(bgColor);

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float newScale = scale * detector.getScaleFactor();
                newScale = Math.max(0.2f, Math.min(3f, newScale));
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                offsetX -= (focusX - offsetX) * (newScale / scale - 1);
                offsetY -= (focusY - offsetY) * (newScale / scale - 1);
                scale = newScale;
                invalidate();
                return true;
            }
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                offsetX -= distanceX;
                offsetY -= distanceY;
                invalidate();
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = (e.getX() - offsetX) / scale;
                float y = (e.getY() - offsetY) / scale;
                selectedNode = findNodeAt(x, y);
                invalidate();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (selectedNode != null) {
                    offsetX = getWidth() / 2f - selectedNode.x * scale;
                    offsetY = getHeight() / 2f - selectedNode.y * scale;
                    invalidate();
                }
                return true;
            }
        });
    }

    public void setModel(DependencyModel model) {
        this.model = model;
        buildGraph();
        invalidate();
    }

    public void setFilterPackage(String packageName) {
        this.filterPackage = packageName;
        buildGraph();
        invalidate();
    }

    public void setColors(int nodeColor, int selectedColor, int pkgColor, int edge, int text, int bg) {
        this.nodeColorDefault = nodeColor;
        this.nodeColorSelected = selectedColor;
        this.nodeColorPackage = pkgColor;
        this.edgeColor = edge;
        this.textColor = text;
        this.bgColor = bg;

        nodePaint.setColor(nodeColor);
        selectedPaint.setColor(selectedColor);
        edgePaint.setColor(edge);
        edgeArrowPaint.setColor(edge);
        textPaint.setColor(text);
        bgPaint.setColor(bg);
        invalidate();
    }

    public GraphNode getSelectedNode() { return selectedNode; }

    private void buildGraph() {
        nodes.clear();
        edges.clear();
        nodeMap.clear();

        if (model == null) return;

        List<DependencyModel.ClassNode> classes = model.getProjectClasses();
        if (classes.isEmpty()) return;

        Random rng = new Random(42);
        int cols = (int) Math.ceil(Math.sqrt(classes.size()));
        float spacing = 150f;
        float startX = 100f;
        float startY = 100f;

        for (int i = 0; i < classes.size(); i++) {
            DependencyModel.ClassNode cn = classes.get(i);
            if (filterPackage != null && !filterPackage.isEmpty()
                    && !cn.packageName.startsWith(filterPackage)) continue;

            int col = i % cols;
            int row = i / cols;
            float x = startX + col * spacing + (rng.nextFloat() - 0.5f) * 30;
            float y = startY + row * spacing + (rng.nextFloat() - 0.5f) * 30;

            GraphNode node = new GraphNode(cn.name, cn.simpleName, cn.packageName, x, y);
            nodes.add(node);
            nodeMap.put(cn.name, node);
        }

        for (DependencyModel.DependencyEdge de : model.getEdges()) {
            GraphNode from = nodeMap.get(de.from);
            GraphNode to = nodeMap.get(de.to);
            if (from != null && to != null) {
                edges.add(new GraphEdge(from, to, de.type));
            }
        }

        layoutNodes();
    }

    private void layoutNodes() {
        if (nodes.isEmpty()) return;

        for (int iter = 0; iter < 100; iter++) {
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    GraphNode a = nodes.get(i);
                    GraphNode b = nodes.get(j);
                    float dx = b.x - a.x;
                    float dy = b.y - a.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 1) dist = 1;

                    float repulsion = 5000f / (dist * dist);
                    float fx = (dx / dist) * repulsion;
                    float fy = (dy / dist) * repulsion;
                    a.x -= fx;
                    a.y -= fy;
                    b.x += fx;
                    b.y += fy;
                }
            }

            for (GraphEdge ge : edges) {
                GraphNode a = ge.from;
                GraphNode b = ge.to;
                float dx = b.x - a.x;
                float dy = b.y - a.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 1) dist = 1;

                float attraction = (dist - 120f) * 0.005f;
                float fx = (dx / dist) * attraction;
                float fy = (dy / dist) * attraction;
                a.x += fx;
                a.y += fy;
                b.x -= fx;
                b.y -= fy;
            }

            float centerX = 0, centerY = 0;
            for (GraphNode n : nodes) {
                centerX += n.x;
                centerY += n.y;
            }
            centerX /= nodes.size();
            centerY /= nodes.size();
            for (GraphNode n : nodes) {
                n.x -= centerX * 0.01f;
                n.y -= centerY * 0.01f;
            }
        }
    }

    private GraphNode findNodeAt(float x, float y) {
        for (GraphNode n : nodes) {
            float dx = x - n.x;
            float dy = y - n.y;
            if (dx * dx + dy * dy < 40 * 40) {
                return n;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);

        for (GraphEdge ge : edges) {
            drawEdge(canvas, ge);
        }

        for (GraphNode n : nodes) {
            drawNode(canvas, n);
        }

        canvas.restore();

        drawLegend(canvas);
    }

    private void drawNode(Canvas canvas, GraphNode node) {
        float radius = 30f;

        if (node == selectedNode) {
            canvas.drawCircle(node.x, node.y, radius + 4, selectedPaint);
            canvas.drawCircle(node.x, node.y, radius + 4, nodeStrokePaint);
        }

        if (node.isRoot) {
            nodePaint.setColor(nodeColorPackage);
        } else {
            nodePaint.setColor(nodeColorDefault);
        }
        canvas.drawCircle(node.x, node.y, radius, nodePaint);

        canvas.drawCircle(node.x, node.y, radius, nodeStrokePaint);

        String label = node.simpleName;
        if (label.length() > 12) label = label.substring(0, 10) + "..";

        textPaint.setTextSize(24f);
        textPaint.setColor(textColor);
        canvas.drawText(label, node.x, node.y + 8, textPaint);
    }

    private void drawEdge(Canvas canvas, GraphEdge edge) {
        float dx = edge.to.x - edge.from.x;
        float dy = edge.to.y - edge.from.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;

        float nx = dx / dist;
        float ny = dy / dist;

        float startX = edge.from.x + nx * 32;
        float startY = edge.from.y + ny * 32;
        float endX = edge.to.x - nx * 32;
        float endY = edge.to.y - ny * 32;

        switch (edge.type) {
            case EXTENDS:
                edgePaint.setColor(0xFF569CD6);
                edgePaint.setStrokeWidth(3f);
                break;
            case IMPLEMENTS:
                edgePaint.setColor(0xFF6A9955);
                edgePaint.setStrokeWidth(2f);
                edgePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 5}, 0));
                break;
            case USES:
                edgePaint.setColor(0xFF888888);
                edgePaint.setStrokeWidth(1.5f);
                edgePaint.setPathEffect(null);
                break;
            default:
                edgePaint.setColor(0xFF666666);
                edgePaint.setStrokeWidth(1f);
                edgePaint.setPathEffect(null);
                break;
        }

        canvas.drawLine(startX, startY, endX, endY, edgePaint);
        edgePaint.setPathEffect(null);

        float arrowLen = 12f;
        float arrowAngle = 0.4f;
        arrowPath.reset();
        arrowPath.moveTo(endX, endY);
        arrowPath.lineTo(
                endX - arrowLen * (float) Math.cos(Math.atan2(ny, nx) - arrowAngle),
                endY - arrowLen * (float) Math.sin(Math.atan2(ny, nx) - arrowAngle)
        );
        arrowPath.lineTo(
                endX - arrowLen * (float) Math.cos(Math.atan2(ny, nx) + arrowAngle),
                endY - arrowLen * (float) Math.sin(Math.atan2(ny, nx) + arrowAngle)
        );
        arrowPath.close();

        edgeArrowPaint.setColor(edgePaint.getColor());
        canvas.drawPath(arrowPath, edgeArrowPaint);
    }

    private void drawLegend(Canvas canvas) {
        float x = 16;
        float y = getHeight() - 80;
        float boxSize = 12;

        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.LEFT);

        nodePaint.setColor(nodeColorDefault);
        canvas.drawCircle(x + 8, y + 8, 8, nodePaint);
        textPaint.setColor(textColor);
        canvas.drawText("Class", x + 22, y + 14, textPaint);

        y += 24;
        edgePaint.setColor(0xFF569CD6);
        edgePaint.setStrokeWidth(3f);
        canvas.drawLine(x, y + 4, x + 20, y + 4, edgePaint);
        textPaint.setColor(textColor);
        canvas.drawText("Extends", x + 28, y + 10, textPaint);

        y += 24;
        edgePaint.setColor(0xFF6A9955);
        edgePaint.setStrokeWidth(2f);
        edgePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{6, 3}, 0));
        canvas.drawLine(x, y + 4, x + 20, y + 4, edgePaint);
        edgePaint.setPathEffect(null);
        textPaint.setColor(textColor);
        canvas.drawText("Implements", x + 28, y + 10, textPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleDetector.onTouchEvent(event);
        handled |= gestureDetector.onTouchEvent(event);
        return handled || super.onTouchEvent(event);
    }

    public void zoomIn() {
        scale = Math.min(3f, scale * 1.3f);
        invalidate();
    }

    public void zoomOut() {
        scale = Math.max(0.2f, scale / 1.3f);
        invalidate();
    }

    public void fitToScreen() {
        if (nodes.isEmpty()) return;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (GraphNode n : nodes) {
            minX = Math.min(minX, n.x);
            minY = Math.min(minY, n.y);
            maxX = Math.max(maxX, n.x);
            maxY = Math.max(maxY, n.y);
        }

        float graphW = maxX - minX + 100;
        float graphH = maxY - minY + 100;
        float scaleX = getWidth() / graphW;
        float scaleY = getHeight() / graphH;
        scale = Math.min(scaleX, scaleY) * 0.85f;
        scale = Math.max(0.2f, Math.min(3f, scale));

        offsetX = (getWidth() - (minX + maxX) * scale) / 2;
        offsetY = (getHeight() - (minY + maxY) * scale) / 2;
        invalidate();
    }

    public static class GraphNode {
        public final String fullName;
        public final String simpleName;
        public final String packageName;
        public float x, y;
        public boolean isRoot;

        public GraphNode(String fullName, String simpleName, String packageName, float x, float y) {
            this.fullName = fullName;
            this.simpleName = simpleName;
            this.packageName = packageName;
            this.x = x;
            this.y = y;
        }
    }

    public static class GraphEdge {
        public final GraphNode from;
        public final GraphNode to;
        public final DependencyModel.DependencyType type;

        public GraphEdge(GraphNode from, GraphNode to, DependencyModel.DependencyType type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }
    }
}
