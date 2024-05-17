package com.example.kangarun.adapter;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * BaseAdapter is an abstract class that provides a base implementation for a RecyclerView adapter.
 * It includes methods to create and bind view holders, get item counts, and manage view types.
 * Subclasses should implement the abstract methods to define their own specific behavior.
 * @author @author Bingnan Zhao u6508459
 */
public abstract class BaseAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    protected abstract T createView(ViewGroup parent, int viewType);

    protected abstract void bindView(T holder, int position);

    protected abstract int getDataCount();

    protected int getViewType(int position) {
        return 0;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return createView(parent, viewType);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        bindView(holder, position);
    }

    @Override
    public int getItemCount() {
        return getDataCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getViewType(position);
    }

    protected int getLayoutId(int viewType) {
        return 0;
    }
}


