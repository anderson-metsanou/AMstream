package com.example.amstream.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.amstream.R;
import com.example.amstream.model.Movie;

import java.util.List;

/**
 * Adaptateur pour le RecyclerView de la Collection locale.
 * Répond à l'exigence du TP : Utilisation de RecyclerView + CardView + Glide pour le chargement réseau asynchrone des images.
 */
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> movieList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
        void onMenuClick(View view, Movie movie);
    }

    public CollectionAdapter(Context context, List<Movie> movieList, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collection_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        
        holder.titleText.setText(movie.getTitle());
        holder.ratingBar.setRating((float) movie.getPersonalRating());
        holder.ratingText.setText(String.valueOf(movie.getPersonalRating()));
        
        // Date de visionnage
        if (movie.getWatchDate().trim().isEmpty()) {
            holder.dateText.setVisibility(View.GONE);
        } else {
            holder.dateText.setVisibility(View.VISIBLE);
            holder.dateText.setText("Vu le " + movie.getWatchDate());
        }

        // Configuration dynamique du badge de statut personnel
        String status = movie.getPersonalStatus();
        holder.statusText.setText(status.toUpperCase());
        
        // Application d'un fond arrondi (badge) de couleur thématique
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(16); 
        
        if (context.getString(R.string.collection_status_watched).equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#4D2E7D6B")); // Transparent Teal
        } else if (context.getString(R.string.collection_status_watching).equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#4DFFD700")); // Transparent Gold
        } else {
            drawable.setColor(Color.parseColor("#4D6A0DAD")); // Transparent Purple
        }
        holder.statusText.setBackground(drawable);

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(context)
                    .load(movie.getPosterPath())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.posterImage);
        } else {
            holder.posterImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Clic sur l'élément entier
        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));

        // Clic sur les 3 points du menu contextuel
        holder.menuButton.setOnClickListener(v -> listener.onMenuClick(v, movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText;
        TextView statusText;
        RatingBar ratingBar;
        TextView ratingText;
        TextView dateText;
        View menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.item_coll_poster);
            titleText = itemView.findViewById(R.id.item_coll_title);
            statusText = itemView.findViewById(R.id.item_coll_status);
            ratingBar = itemView.findViewById(R.id.item_coll_rating);
            ratingText = itemView.findViewById(R.id.item_coll_rating_text);
            dateText = itemView.findViewById(R.id.item_coll_date);
            menuButton = itemView.findViewById(R.id.item_coll_menu);
        }
    }
}
