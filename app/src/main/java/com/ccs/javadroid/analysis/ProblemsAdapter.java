package com.ccs.javadroid.analysis;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppTheme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProblemsAdapter extends RecyclerView.Adapter<ProblemsAdapter.VH> {

    public interface Listener {
        void onProblemClicked(ProblemItem item);
    }

    private final List<ProblemItem> items = new ArrayList<>();
    private Listener listener;
    private AppTheme theme;

    public void setTheme(AppTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProblemItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_problem, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProblemItem p = items.get(position);
        Context ctx = h.itemView.getContext();
        String sev;
        int color;
        switch (p.severity) {
            case ERROR:
                sev = ctx.getString(R.string.problem_error);
                color = theme != null ? theme.errorText : 0xFFFF6B6B;
                break;
            case WARNING:
                sev = ctx.getString(R.string.problem_warn);
                color = 0xFFFFB74D;
                break;
            default:
                sev = ctx.getString(R.string.problem_info);
                color = theme != null ? theme.accent : 0xFF64B5F6;
                break;
        }
        h.severity.setText(sev);
        h.severity.setTextColor(color);
        String loc = p.file != null ? p.file.getName() : ctx.getString(R.string.problem_location_unknown);
        if (p.line > 0) loc += ":" + p.line;
        h.location.setText(loc);
        h.location.setTextColor(theme != null ? theme.textDim : 0xFF808080);
        h.message.setText(p.message);
        h.message.setTextColor(theme != null ? theme.text : 0xFFA9B7C6);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProblemClicked(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView severity;
        final TextView location;
        final TextView message;

        VH(View v) {
            super(v);
            severity = v.findViewById(R.id.problemSeverity);
            location = v.findViewById(R.id.problemLocation);
            message = v.findViewById(R.id.problemMessage);
        }
    }
}
