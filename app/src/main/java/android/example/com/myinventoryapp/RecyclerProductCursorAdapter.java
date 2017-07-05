package android.example.com.myinventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.example.com.myinventoryapp.databinding.ListItemBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by ByteTonight on 02.07.2017.
 *
 * http://emuneee.com/blog/2016/01/10/cursors-recyclerviews-and-itemanimators/
 * @Google : Why make things easy is you can make them complicated, right ?
 */

public class RecyclerProductCursorAdapter
        extends CursorRecyclerViewAdapter<RecyclerProductCursorAdapter.ViewHolder> {


    public RecyclerProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        ListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.list_item, parent, false);
        binding.setHandlers(new Handlers());
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {

        Product product = Product.fromCursor(cursor);
        holder.bind(product);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        /**
         * @param binding of type ViewDataBinding which is an
         *                abstract Base Class for generated binding classes
         */
        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(android.example.com.myinventoryapp.BR.product, obj);
            binding.executePendingBindings();
        }
    }
}
