package com.pph.ecommerce.repository.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCriteria {
    String key;
    String operation;
    Object value;
}
