package com.mandeep.testfgservice.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandeep.testfgservice.R;
import com.mandeep.testfgservice.db.Places;

import java.util.ArrayList;
import java.util.List;

public class SavedPlacesAdapter extends RecyclerView.Adapter<SavedPlacesAdapter.SavedHolder> {

    setClickListener setClickListener;

    public void setSetClickListener(setClickListener setClickListener1) {
        setClickListener = setClickListener1;
    }

    public interface setClickListener {
        void savedItemClicked(Places places);
    }

    private List<Places> placesList = new ArrayList<>();

    public void setPlacesList(List<Places> placesList1) {
        placesList = placesList1;
    }

    @NonNull
    @Override
    public SavedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SavedHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.place_recycler_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SavedHolder holder, final int position) {
        final Places places = placesList.get(position);
        holder.address.setText(places.getAddress());
        holder.area.setText(places.getPlaceName());
        holder.mRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClickListener.savedItemClicked(places);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public static class SavedHolder extends RecyclerView.ViewHolder {
        private TextView address, area;
        private LinearLayout mRow;

        SavedHolder(View itemView) {
            super(itemView);
            area = itemView.findViewById(R.id.place_area);
            address = itemView.findViewById(R.id.place_address);
            mRow = itemView.findViewById(R.id.place_item_view);
        }
    }
}
