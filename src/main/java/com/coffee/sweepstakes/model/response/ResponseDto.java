package com.coffee.sweepstakes.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private Integer status;
    private List<T> data;
    private Long count;
    private Long total;
    private List<ErrorDto> errors;

    @Override
    public String toString() {
        return "ResponseDto{" +
                "status=" + status +
                ", data=" + data +
                ", count=" + count +
                ", total=" + total +
                ", errors=" + errors +
                '}';
    }
}
