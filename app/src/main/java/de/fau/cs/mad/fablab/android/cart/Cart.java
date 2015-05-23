package de.fau.cs.mad.fablab.android.cart;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.db.DatabaseHelper;
import de.fau.cs.mad.fablab.rest.core.CartEntry;
import de.fau.cs.mad.fablab.rest.core.Product;

public enum Cart {
    MYCART;

    private List<CartEntry> products;
    private RuntimeExceptionDao<CartEntry, Long> dao;
    private RecyclerViewAdapter adapter;
    private Context context;
    private View view;
    private SlidingUpPanelLayout mLayout;

    Cart(){
    }

    // initialization of the db - getting all products that are in the cart
    public void init(Context context){
        dao = DatabaseHelper.getHelper(context).getCartEntryDao();
        products = dao.queryForAll();
    }

    // Setting up the view for every context c including the sliding up panel
    public void setSlidingUpPanel(Context c, View v, boolean slidingUp){
        this.context = c;
        this.view = v;
        // Set Layout and Recyclerview
        RecyclerView cart_rv = (RecyclerView) v.findViewById(R.id.cart_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        cart_rv.setLayoutManager(llm);
        cart_rv.setHasFixedSize(true);
        // Add Entries to view
        adapter = new RecyclerViewAdapter(products);
        cart_rv.setAdapter(adapter);

        // Set up listener to be able to swipe to left/right to remove items
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(cart_rv,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Cart.MYCART.removeEntry(products.get(position));
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();
                                refresh();
                                updateVisibility();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Cart.MYCART.removeEntry(products.get(position));
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();
                                refresh();
                                updateVisibility();
                            }
                        });

        cart_rv.addOnItemTouchListener(swipeTouchListener);

        // Set up listener to show more than just one line of the product name
        cart_rv.addOnItemTouchListener(new RecyclerItemClickListener(context, cart_rv, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                TextView product_title = (TextView) view.findViewById(R.id.cart_product_name);

                if(product_title.getLineCount() == 1){
                    product_title.setSingleLine(false);
                }else{
                    product_title.setSingleLine(true);
                }
            }

            @Override
            public void onItemLongClick(View view, final int position){

            }
        }));



        // set total price
        refresh();

        // Basket empty? -> show msg if slidinguppanel is not used
        if(products.size() == 0 && !slidingUp){
            Toast.makeText(context,  R.string.cart_empty, Toast.LENGTH_LONG).show();
        }

        // Set up Sliding up Panel
        if(slidingUp) {
            mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);

            // only the top should be draggable
            mLayout.setDragView(view.findViewById(R.id.dragPart));


            mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    View bg = view.findViewById(R.id.dragPart);
                    Drawable background = bg.getBackground();
                    background.setAlpha(255 - (int) (slideOffset * 255));

                    TextView cart_title_closed = (TextView) view.findViewById(R.id.cart_title_closed);
                    cart_title_closed.setAlpha(1 - slideOffset);
                    TextView cart_title_opened = (TextView) view.findViewById(R.id.cart_title_opened);
                    cart_title_opened.setAlpha(slideOffset);
                    TextView total_price_top = (TextView) view.findViewById(R.id.cart_total_price_preview);
                    total_price_top.setAlpha(1 - slideOffset);
                }

                @Override
                public void onPanelExpanded(View panel) {
                    //Log.i("app", "onPanelExpanded");

                }

                @Override
                public void onPanelCollapsed(View panel) {
                    //Log.i("app", "onPanelCollapsed");
                }

                @Override
                public void onPanelAnchored(View panel) {
                    //Log.i("app", "onPanelAnchored");
                }

                @Override
                public void onPanelHidden(View panel) {
                    //Log.i("app", "onPanelHidden");
                }
            });
            updateVisibility();
        }
    }

    // refresh TextView of the total price and #items in cart
    private void refresh(){
        TextView total_price = (TextView) view.findViewById(R.id.cart_total_price);
        total_price.setText(Cart.MYCART.totalPrice());
        String base = view.getResources().getString(R.string.bold_start) + products.size() +
                view.getResources().getString(R.string.cart_article_label) +
                view.getResources().getString(R.string.cart_preview_delimiter) +
                Cart.MYCART.totalPrice() +
                view.getResources().getString(R.string.bold_end);
        TextView total_price_top = (TextView) view.findViewById(R.id.cart_total_price_preview);
        total_price_top.setText(Html.fromHtml(base));
    }

    // panel only visible if cart is not empty
    private void updateVisibility(){
        if(products.size() == 0){
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mLayout.setPanelHeight((int) (view.getResources().getDimension(R.dimen.zero) / view.getResources().getDisplayMetrics().density));
        }else{
            mLayout.setPanelHeight((int) view.getResources().getDimension(R.dimen.slidinguppanel_panel_height));
        }
    }

    // returns all existing products of the cart
    public List<CartEntry> getProducts(){
        return products;
    }

    // update CartEntry
    public void updateEntry(CartEntry entry){
        for(CartEntry temp : products){
            if(temp.getProductId() == entry.getProductId()){
                temp.setAmount(entry.getAmount());
                dao.update(temp);
                adapter.notifyDataSetChanged();
                refresh();
                updateVisibility();
            }
        }
    }

    // remove CartEntry
    public boolean removeEntry(CartEntry entry){
        for(int i=0; i<products.size(); i++){
            if(products.get(i).getProductId() == entry.getProductId()){
                dao.delete(entry);
                products.remove(i);
                adapter.notifyDataSetChanged();
                refresh();
                updateVisibility();
                return true;
            }
        }

        return false;
    }

    public void removeAllEntries() {
        dao.delete(products);
        products.clear();
        refresh();
        updateVisibility();
    }

    // add product to cart
    public void addProduct(Product product){
        // update existing cart entry
        for(CartEntry temp : products){
            if (temp.getProductId() == product.getProductId()){
                temp.setAmount(temp.getAmount() + 1);
                dao.update(temp);
                adapter.notifyDataSetChanged();
                refresh();
                updateVisibility();
                return;
            }
        }

        // create new one
        CartEntry new_entry = new CartEntry(product,1);
        dao.create(new_entry);
        products.add(new_entry);
        adapter.notifyDataSetChanged();
        refresh();
        updateVisibility();
    }

    // return total price
    public String totalPrice(){
        double total = 0;
        for(int i=0;i<products.size();i++){
            total += products.get(i).getPrice()*products.get(i).getAmount();
        }

        return String.format( "%.2f", total ) + Html.fromHtml(view.getResources().getString(R.string.non_breaking_space)) +
                view.getResources().getString(R.string.currency);
    }

}