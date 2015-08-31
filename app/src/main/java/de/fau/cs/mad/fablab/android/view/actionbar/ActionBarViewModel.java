package de.fau.cs.mad.fablab.android.view.actionbar;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.model.SpaceApiModel;
import de.fau.cs.mad.fablab.android.model.events.AppBarShowDoorStateEvent;
import de.fau.cs.mad.fablab.android.model.events.AppBarShowTitleEvent;
import de.fau.cs.mad.fablab.android.model.events.SpaceApiStateChangedEvent;
import de.fau.cs.mad.fablab.android.util.Formatter;
import de.fau.cs.mad.fablab.android.viewmodel.common.commands.Command;
import de.greenrobot.event.EventBus;

public class ActionBarViewModel {

    @Inject
    SpaceApiModel mSpaceApiModel;
    private Listener mListener;
    private EventBus mEventBus = EventBus.getDefault();

    private final Command<Void> mShowDoorStateToastCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            if (mListener != null) {
                mListener.onShowDoorStateToast(mSpaceApiModel.getOpen(), Formatter.formatTime(
                        mSpaceApiModel.getTime()));
            }
        }
    };

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public Command<Void> getShowDoorStateToastCommand() {
        return mShowDoorStateToastCommand;
    }

    public void initialize() {
        if (mListener != null) {
            mListener.onStateUpdated(mSpaceApiModel.getOpen(), Formatter.formatTime(
                    mSpaceApiModel.getTime()));
        }
    }

    public void pause() {
        mEventBus.unregister(this);
    }

    public void resume() {
        mEventBus.register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(SpaceApiStateChangedEvent event) {
        if (mListener != null) {
            mListener.onStateUpdated(event.getOpen(), Formatter.formatTime(event.getTime()));
        }
    }

    public void onEvent(AppBarShowDoorStateEvent event) {
        if (mListener != null) {
            mListener.onShowDoorState(event.getState());
        }
    }

    public void onEvent(AppBarShowTitleEvent event) {
        if (mListener != null) {
            mListener.onShowTitle(event.getState());
        }
    }

    public interface Listener {
        void onStateUpdated(boolean open, String time);

        void onShowDoorState(boolean state);

        void onShowTitle(boolean state);

        void onShowDoorStateToast(boolean state, String time);
    }
}
