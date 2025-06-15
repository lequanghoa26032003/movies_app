package com.example.movies_app.Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ListFilm {

    @SerializedName("data")
    @Expose
    private List<Datum> data;

    @SerializedName("metadata")
    @Expose
    private Metadata metadata;

    // Constructor mặc định luôn khởi tạo list rỗng
    public ListFilm() {
        this.data = new ArrayList<>();
    }

    // Getter an toàn
    public List<Datum> getData() {
        return data != null ? data : Collections.emptyList();
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}