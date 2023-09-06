package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class EndpointHitDto {
    private long id;

    @NotEmpty
    @NotBlank
    private String app;

    @NotEmpty
    @NotBlank
    private String uri;

    @NotEmpty
    @NotBlank
    private String ip;

    @NotEmpty
    @NotBlank
    private String timestamp;
}
