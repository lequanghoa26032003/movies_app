package com.example.movies_app.Helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenreHelper {
    
    // ✅ XỬ LÝ CHUỖI GENRES THÀNH LIST
    public static List<String> extractGenresFromDatabase(List<String> genreStrings) {
        Set<String> uniqueGenres = new HashSet<>();
        
        for (String genreString : genreStrings) {
            if (genreString != null && !genreString.trim().isEmpty()) {
                // Split by comma và clean up
                String[] genres = genreString.split(",");
                for (String genre : genres) {
                    String cleanGenre = genre.trim();
                    if (!cleanGenre.isEmpty()) {
                        uniqueGenres.add(cleanGenre);
                    }
                }
            }
        }
        
        return new ArrayList<>(uniqueGenres);
    }
    
    // ✅ TẠO GENRE DISPLAY NAME
    public static String getGenreDisplayName(String genre) {
        // Map genre codes to Vietnamese names (if needed)
        switch (genre.toLowerCase()) {
            case "action": return "Hành Động";
            case "adventure": return "Phiêu Lưu";
            case "animation": return "Hoạt Hình";
            case "comedy": return "Hài";
            case "crime": return "Tội Phạm";
            case "drama": return "Chính Kịch";
            case "fantasy": return "Giả Tưởng";
            case "horror": return "Kinh Dị";
            case "romance": return "Lãng Mạn";
            case "sci-fi": 
            case "science fiction": return "Khoa Học Viễn Tưởng";
            case "thriller": return "Ly Kỳ";
            case "western": return "Miền Tây";
            case "war": return "Chiến Tranh";
            case "documentary": return "Tài Liệu";
            case "family": return "Gia Đình";
            case "mystery": return "Bí Ẩn";
            case "biography": return "Tiểu Sử";
            case "history": return "Lịch Sử";
            case "music": return "Âm Nhạc";
            case "sport": return "Thể Thao";
            default: return genre; // Return original if no mapping
        }
    }
}