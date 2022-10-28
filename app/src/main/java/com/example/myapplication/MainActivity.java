package com.example.myapplication;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FavAdapter mFavAdapter;
    private FavouritesViewModel mFavViewModel;
    private List<Favourites> mFav;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fab);
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFavViewModel = ViewModelProviders.of(this).get(FavouritesViewModel.class);
        final Observer<List<Favourites>> favsObserver = updatedList -> {
            if (mFav == null) {
                mFav = updatedList;
                mFavAdapter = new FavAdapter();
                recyclerView.setAdapter(mFavAdapter);
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                    @Override
                    public int getOldListSize() {
                        return mFav.size();
                    }

                    @Override
                    public int getNewListSize() {
                        assert updatedList != null;
                        return updatedList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        assert updatedList != null;
                        return mFav.get(oldItemPosition).mId ==
                                updatedList.get(newItemPosition).mId;
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Favourites oldFav = mFav.get(oldItemPosition);
                        assert updatedList != null;
                        Favourites newFav = updatedList.get(newItemPosition);
                        return oldFav.equals(newFav);
                    }
                });
                result.dispatchUpdatesTo(mFavAdapter);
                mFav = updatedList;
            }
        };

        fab.setOnClickListener(view -> {
            final EditText inUrl = new EditText(MainActivity.this);
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("New favourite")
                    .setMessage("Add a url link below")
                    .setView(inUrl)
                    .setPositiveButton("Add", (dialog1, which) -> {
                        String url = String.valueOf(inUrl.getText());
                        long date = (new Date()).getTime();

                        mFavViewModel.addFav(url, date);
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        });

        mFavViewModel.getFavs().observe(this, favsObserver);
    }


    public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {

        @NonNull
        @Override
        public FavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_row, parent, false);
            return new FavViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(FavViewHolder holder, int position) {
            Favourites favourites = mFav.get(position);
            holder.mTxtUrl.setText(favourites.mUrl);
            holder.mTxtDate.setText((new Date(favourites.mDate).toString()));
        }

        @Override
        public int getItemCount() {
            return mFav.size();
        }

        class FavViewHolder extends RecyclerView.ViewHolder {

            TextView mTxtUrl;
            TextView mTxtDate;

            FavViewHolder(View itemView) {
                super(itemView);
                mTxtUrl = itemView.findViewById(R.id.tvUrl);
                mTxtDate = itemView.findViewById(R.id.tvDate);
                ImageButton btnDelete = itemView.findViewById(R.id.btnDelete);
                btnDelete.setOnClickListener(view -> {
                    int pos = getAdapterPosition();
                    Favourites favourites = mFav.get(pos);
                    mFavViewModel.removeFav(favourites.mId);
                });
            }
        }
    }
}