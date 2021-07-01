package com.amsavarthan.social.hify.ui.activities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.social.hify.adapters.DrawerAdapter;
import com.amsavarthan.social.hify.models.DrawerItem;

/**
 * Created by amsavarthan on 29/3/18.
 */


public class SpaceItem extends DrawerItem<SpaceItem.ViewHolder> {

    private int spaceDp;

    public SpaceItem(int spaceDp) {
        this.spaceDp = spaceDp;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        Context c = parent.getContext();
        View view = new View(c);
        int height = (int) (c.getResources().getDisplayMetrics().density * spaceDp);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height));
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {

    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    static class ViewHolder extends DrawerAdapter.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}