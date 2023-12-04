package ru.Raingor.webAnimeSite.utils.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserErrorResponse {
    private String message;
    private Long timestamp;
}
