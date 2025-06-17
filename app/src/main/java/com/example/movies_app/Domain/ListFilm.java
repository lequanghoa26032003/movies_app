package com.example.movies_app.Domain;

import com.example.movies_app.Database.entity.Movie; // THÊM IMPORT NÀY
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ListFilm {

    @SerializedName("data")
    @Expose
    private List<Movie> data; // ĐỔI TỪ Datum THÀNH Movie

    @SerializedName("metadata")
    @Expose
    private Metadata metadata;

    // Constructor mặc định luôn khởi tạo list rỗng
    public ListFilm() {
        this.data = new ArrayList<>();
    }

    // Getter an toàn
    public List<Movie> getData() { // ĐỔI TỪ Datum THÀNH Movie
        return data != null ? data : Collections.emptyList();
    }

    public void setData(List<Movie> data) { // ĐỔI TỪ Datum THÀNH Movie
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}