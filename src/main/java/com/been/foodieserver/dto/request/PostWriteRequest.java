package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.PostDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostWriteRequest {

    @Positive
    @NotNull
    private Long categoryId;

    @Size(max = 100)
    @NotBlank
    private String title;

    @Size(max = 10000)
    @NotBlank
    private String content;

    public PostDto toDto() {
        return PostDto.builder()
                .categoryId(categoryId)
                .title(title)
                .content(content)
                .build();
    }
}
