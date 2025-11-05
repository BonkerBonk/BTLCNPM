package com.btlcnpm.TicketService.dto;

import com.google.cloud.Timestamp;
import lombok.Data;

public class DenormalizationDTOs {

    /**
     * Hứng dữ liệu từ ShowtimeService (Port 8089)
     */
    @Data
    public static class ShowtimeDTO {
        private String movieId;
        private String theaterId;
        private String roomId;
        private String startTime; // <-- ĐÃ SỬA: Từ Timestamp sang String
    }

    /**
     * Hứng dữ liệu từ MovieCatalogService (Port 8085)
     */
    @Data
    public static class MovieDTO {
        private String title;
    }

    /**
     * Hứng dữ liệu từ TheaterService (Port 8086)
     */
    @Data
    public static class TheaterDTO {
        private String name;
    }

    /**
     * Hứng dữ liệu từ RoomService (Port 8088)
     */
    @Data
    public static class RoomDTO {
        private String name;
    }
}

