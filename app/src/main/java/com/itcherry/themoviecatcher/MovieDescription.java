package com.itcherry.themoviecatcher;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieDescription implements Parcelable {
    private String overview;
    private String imageUrl;
    private String releaseDate;
    private String title;
    private double popularity;
    private double voteAverage;
    private int voteCount;
    private int id;

    public MovieDescription(int id, int voteCount, String title,
                            double voteAverage, double popularity, String releaseDate,
                            String imageUrl, String overview) {
        this.voteCount = voteCount;
        this.title = title;
        this.voteAverage = voteAverage;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.id = id;
    }

    protected MovieDescription(Parcel in) {
        overview = in.readString();
        imageUrl = in.readString();
        releaseDate = in.readString();
        title = in.readString();
        voteAverage = in.readDouble();
        popularity = in.readDouble();
        voteCount = in.readInt();
        id = in.readInt();
    }

    public static final Creator<MovieDescription> CREATOR = new Creator<MovieDescription>() {
        @Override
        public MovieDescription createFromParcel(Parcel in) {
            return new MovieDescription(in);
        }

        @Override
        public MovieDescription[] newArray(int size) {
            return new MovieDescription[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getOverview() {
        return overview;
    }

    public double getPopularity() {
        return popularity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(overview);
        dest.writeString(imageUrl);
        dest.writeString(releaseDate);
        dest.writeString(title);
        dest.writeDouble(voteAverage);
        dest.writeDouble(popularity);
        dest.writeInt(voteCount);
        dest.writeInt(id);
    }
}
