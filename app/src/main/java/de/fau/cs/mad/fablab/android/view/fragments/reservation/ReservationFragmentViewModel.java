package de.fau.cs.mad.fablab.android.view.fragments.reservation;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.pedrogomez.renderers.AdapteeCollection;
import com.pedrogomez.renderers.ListAdapteeCollection;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.model.ToolUsageModel;
import de.fau.cs.mad.fablab.android.model.events.ShowToastEvent;
import de.fau.cs.mad.fablab.android.viewmodel.common.BaseViewModel;
import de.fau.cs.mad.fablab.android.viewmodel.common.commands.Command;
import de.fau.cs.mad.fablab.rest.core.FabTool;
import de.fau.cs.mad.fablab.rest.core.ToolUsage;
import de.fau.cs.mad.fablab.rest.core.User;
import de.greenrobot.event.EventBus;

public class ReservationFragmentViewModel extends BaseViewModel<ToolUsage> implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    ToolUsageModel mToolUsageModel;

    private Listener mListener;
    private User mUser;
    private FabTool mTool;

    private ListAdapteeCollection<ToolUsageViewModel> mToolUsageViewModelCollection;
    private ToolUsage mLastRemovedEntry;

    EventBus mEventBus = EventBus.getDefault();

    private Command<Void> mAddCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            if (mListener != null) {
                mListener.onAdd();
            }
        }
    };

    private Command<Integer> mRemoveReservationCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) {
            // Comment for allowing users and admins to delete
            if (mToolUsageModel.isOwnUsage(mToolUsageViewModelCollection.get(parameter).getToolUsageEntry()) /*||
                    mUser != null && (mUser.hasRole(Roles.ADMIN) || mUser.getUsername().equals(mToolUsageViewModelCollection.get(parameter).getToolUsageEntry().getUser()))*/) {
                mLastRemovedEntry = mToolUsageViewModelCollection.get(parameter).getToolUsageEntry();
                mToolUsageViewModelCollection.remove((int) parameter);
                mToolUsageModel.removeEntry(mLastRemovedEntry);
                if (mListener != null) {
                    mListener.onToolListChanged();
                }
            } else {
                mEventBus.post(new ShowToastEvent("Es ist nur m\u00F6glich, eigene Reservierungen zu l\u00F6schen.", Toast.LENGTH_SHORT));
            }
        }
    };

    private Command<Integer> mToolChangedCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) {
            if (mListener != null) {
                mListener.onToolChanged(parameter);
                mListener.onToolListChanged();
            }
        }
    };

    public ReservationFragmentViewModel() {
        mToolUsageViewModelCollection = new ListAdapteeCollection<>();
    }

    public void setListener(Listener listener) {
        mListener = listener;
        mToolUsageModel.setObservableArrayListListener(this);
    }

    public Command<Void> getAddCommand() {
        return mAddCommand;
    }

    public Command<Integer> getToolChangedCommand() {
        return mToolChangedCommand;
    }

    public Command<Integer> getRemoveReservationCommand() {
        return mRemoveReservationCommand;
    }

    public List<FabTool> getTools() {
        return mToolUsageModel.getEnabledFabTools();
    }

    public List<ToolUsage> getToolUsages(long toolId) {
        return mToolUsageModel.getToolUsages(toolId);
    }


    @Override
    public void onAllItemsAdded(Collection<? extends ToolUsage> collection) {
        addItems(collection);
    }

    private void addItems(Collection<? extends ToolUsage> toolUsages) {
        for (ToolUsage toolUsage : toolUsages) {
            mToolUsageViewModelCollection.add(new ToolUsageViewModel(toolUsage, mToolUsageModel.isOwnUsage(toolUsage)));
        }
        if (mListener != null) {
            mListener.onToolListChanged();
        }
    }

    @Override
    public void onAllItemsRemoved(List<ToolUsage> removedItems) {
        mToolUsageViewModelCollection.clear();
        if (mListener != null) {
            mListener.onToolListChanged();
        }
    }

    public AdapteeCollection<ToolUsageViewModel> getToolUsageViewModelCollection() {
        return mToolUsageViewModelCollection;
    }


    public void setUser(User user)
    {
        mUser = user;
    }

    public User getUser()
    {
        return mUser;
    }

    public void setTool(FabTool fabTool)
    {
        mTool = fabTool;
    }

    public FabTool getTool()
    {
        return mTool;
    }

    @Override
    public void onRefresh() {
        if (mListener != null) {
            mListener.onToolChanged(0);
            mListener.onToolListChanged();
            mListener.onLoadTools();
        }
    }

    public interface Listener {
        void onAdd();
        void onToolChanged(int parameter);
        void onToolListChanged();
        void onLoadTools();
    }
}
