package com.example.amstream.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.amstream.R;
import com.example.amstream.model.Movie;

import java.util.List;

/**
 * Adaptateur pour le RecyclerView de l'écran Recherche TMDB.
 * Gère l'affichage en grille d'affiches de films avec leur titre et leur année de sortie.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> resultsList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public SearchAdapter(Context context, List<Movie> resultsList, OnItemClickListener listener) {
        this.context = context;
        this.resultsList = resultsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = resultsList.get(position);
        
        holder.titleText.setText(movie.getTitle());

        // Extraire l'année de la date de sortie (format TMDB : AAAA-MM-JJ)
        String releaseDate = movie.getReleaseDate();
        if (releaseDate != null && releaseDate.length() >= 4) {
            String year = releaseDate.substring(0, 4);
            holder.yearText.setText(year);
        } else {
            holder.yearText.setText("");
        }

        // Chargement asynchrone de l'image de l'affiche via Glide
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(context)
                    .load(movie.getPosterPath())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(12))) // Coins de type ROUND_TWELVE
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.posterImage);
        } else {
            holder.posterImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Clic sur l'affiche
        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText;
        TextView yearText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.item_search_poster);
            titleText = itemView.findViewById(R.id.item_search_title);
            yearText = itemView.findViewById(R.id.item_search_year);
        }
    }
}
