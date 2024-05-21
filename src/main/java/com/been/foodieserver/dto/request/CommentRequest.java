package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.CommentDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @Size(max = 500)
    @NotBlank
    private String content;

    public CommentDto toDto() {
        return CommentDto.builder()
                .content(content)
                .build();
    }
}
