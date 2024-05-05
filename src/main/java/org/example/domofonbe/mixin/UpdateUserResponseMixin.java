package org.example.domofonbe.mixin;

import com.fasterxml.jackson.databind.annotation.JsonAppend;

@JsonAppend(
        attrs = {
                @JsonAppend.Attr(value = "ApprovalState")
        }
)
public class UpdateUserResponseMixin {}
