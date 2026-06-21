package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppTheme;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.FileViewHolder> {

    public interface NodeListener {
        void onNodeClicked(FileTreeNode node);
        void onNodeLongClicked(FileTreeNode node);
    }

    private final List<FileTreeNode> nodes = new ArrayList<>();
    private NodeListener listener;
    private File activeFile;
    private AppTheme theme;

    public void setTheme(AppTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    public void setNodeListener(NodeListener listener) {
        this.listener = listener;
    }

    public void setNodes(List<FileTreeNode> newNodes) {
        nodes.clear();
        if (newNodes != null) nodes.addAll(newNodes);
        notifyDataSetChanged();
    }

    public void setActiveFile(File file) {
        activeFile = file;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_tree, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileTreeNode node = nodes.get(position);
        String name = node.shortName();

        if (node.directory) {
            holder.itemView.setBackgroundColor(0x00000000);
            holder.icon.setText("📁");
            holder.fileName.setText(name);
            holder.fileName.setTextColor(theme != null ? theme.textDim : 0xFF9E9E9E);
        } else {
            String lower = name.toLowerCase();
            if (lower.endsWith(".java")) holder.icon.setText("☕");
            else if (lower.endsWith(".class")) holder.icon.setText("🅒");
            else if (lower.endsWith(".xml")) holder.icon.setText("📜");
            else if (lower.endsWith(".properties")) holder.icon.setText("⚙");
            else if (lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h") || lower.endsWith(".hpp")) holder.icon.setText("🛠️");
            else holder.icon.setText("📄");
            holder.fileName.setText(name);
            boolean active = node.path.equals(activeFile);
            if (theme != null) {
                holder.fileName.setTextColor(active ? theme.text : theme.textDim);
            } else {
                holder.fileName.setTextColor(active ? 0xFFBBBBBB : 0xFF9E9E9E);
            }
        }

        int depthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12f * node.depth,
                holder.itemView.getResources().getDisplayMetrics());
        holder.itemView.setPaddingRelative(depthPx + (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, holder.itemView.getResources().getDisplayMetrics()),
                holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingEnd(),
                holder.itemView.getPaddingBottom());

        boolean activeRow = !node.directory && node.path.equals(activeFile);
        int activeRowBg = theme != null ? (0x33000000 | (theme.accent & 0x00FFFFFF)) : 0x334A86C8;
        holder.itemView.setBackgroundColor(activeRow ? activeRowBg : 0x00000000);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNodeClicked(node);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNodeLongClicked(node);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView fileName;

        FileViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.nodeIcon);
            fileName = view.findViewById(R.id.fileName);
        }
    }
}
