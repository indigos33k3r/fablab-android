package de.fau.cs.mad.fablab.android.view.cartpanel;

import de.fau.cs.mad.fablab.android.model.entities.CartEntry;

public class CartEntryUpdatedEvent {
    private final CartEntry mCartEntry;

    public CartEntryUpdatedEvent(CartEntry cartEntry) {
        mCartEntry = cartEntry;
    }

    public CartEntry getCartEntry() {
        return mCartEntry;
    }
}
