package com.edu.journal.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.journal.databinding.ItemTextRvBinding;
import com.edu.journal.db.bean.Province;
import com.edu.journal.utils.AdministrativeType;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final List<Province.City> cities;

    private AdministrativeClickCallback administrativeClickCallback;//视图点击

    public CityAdapter(List<Province.City> cities) {
        this.cities = cities;
    }

    public void setAdministrativeClickCallback(AdministrativeClickCallback administrativeClickCallback) {
        this.administrativeClickCallback = administrativeClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTextRvBinding binding = ItemTextRvBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        //添加视图点击事件
        binding.getRoot().setOnClickListener(v -> {
            if (administrativeClickCallback != null) {
                administrativeClickCallback.onAdministrativeItemClick(v, viewHolder.getAdapterPosition(), AdministrativeType.CITY);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.tvText.setText(cities.get(position).getCityName());
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ItemTextRvBinding binding;

        public ViewHolder(@NonNull ItemTextRvBinding itemTextRvBinding) {
            super(itemTextRvBinding.getRoot());
            binding = itemTextRvBinding;
        }
    }
}


