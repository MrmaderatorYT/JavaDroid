package com.ccs.javadroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.TabViewHolder> {

    public interface TabListener {
        void onTabSelected(int index);
        void onTabClosed(int index);
    }

    private final List<FileTab> tabs = new ArrayList<>();
    private int activeIndex = -1;
    private TabListener listener;

    public void setTabListener(TabListener listener) {
        this.listener = listener;
    }

    public void addTab(FileTab tab) {
        tabs.add(tab);
        notifyItemInserted(tabs.size() - 1);
    }

    public void removeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        tabs.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, tabs.size() - index);
    }

    public void setActiveIndex(int index) {
        int old = activeIndex;
        activeIndex = index;
        if (old >= 0 && old < tabs.size()) notifyItemChanged(old);
        if (index >= 0 && index < tabs.size()) notifyItemChanged(index);
    }

    public int getActiveIndex() { return activeIndex; }

    public FileTab getActiveTab() {
        if (activeIndex >= 0 && activeIndex < tabs.size()) return tabs.get(activeIndex);
        return null;
    }

    public List<FileTab> getTabs() { return tabs; }

    public int indexOfFile(File file) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).file.equals(file)) return i;
        }
        return -1;
    }

    public void markModified(int index, boolean modified) {
        if (index >= 0 && index < tabs.size()) {
            tabs.get(index).isModified = modified;
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_tab, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        FileTab tab = tabs.get(position);
        holder.tabName.setText(tab.getDisplayName());

        boolean isActive = position == activeIndex;
        holder.itemView.setBackgroundColor(isActive ? 0xFF4E5254 : 0xFF3C3F41);
        holder.tabName.setTextColor(isActive ? 0xFFBBBBBB : 0xFF808080);
        holder.tabClose.setTextColor(isActive ? 0xFF808080 : 0xFF606060);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTabSelected(holder.getAdapterPosition());
        });
        holder.tabClose.setOnClickListener(v -> {
            if (listener != null) listener.onTabClosed(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return tabs.size(); }

    static class TabViewHolder extends RecyclerView.ViewHolder {
        final TextView tabName;
        final TextView tabClose;

        TabViewHolder(View view) {
            super(view);
            tabName = view.findViewById(R.id.tabName);
            tabClose = view.findViewById(R.id.tabClose);
        }
    }
}
