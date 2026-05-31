package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewReplyRequest {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String replyComment;
}
