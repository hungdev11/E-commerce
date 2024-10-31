package com.pph.ecommerce.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse <T> implements Serializable {
    int offset;
    int limit;
    int total;
    T items;
}
