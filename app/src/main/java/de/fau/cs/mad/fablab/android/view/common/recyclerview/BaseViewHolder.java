package de.fau.cs.mad.fablab.android.view.common.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/***
 * Base class for ViewHolders. Extends RecyclerView ViewHolder
 * @param <ViewModelType>
 */
public abstract class BaseViewHolder<ViewModelType> extends RecyclerView.ViewHolder {

    /***
     * Protected constructor since we only want derived classes
     * @param itemView
     */
    protected BaseViewHolder(View itemView) {
        super(itemView);
    }

    /***
     * Sets the data for a ViewHolder
     * @param data
     */
    public abstract void setViewModel(ViewModelType data);
}