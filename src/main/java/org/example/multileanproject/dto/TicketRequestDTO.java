package org.example.multileanproject.dto;

import lombok.Data;
import org.example.multileanproject.entity.TicketStatus;

public class TicketRequestDTO {

    @Data
    public static class Create {
        private String email;
        private String title;
        private String content;
    }

    @Data
    public static class Reply {
        private String adminReply;
        private TicketStatus status;
    }
}
