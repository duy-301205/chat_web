package com.example.chatWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
}
