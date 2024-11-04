package com.pph.ecommerce.repository.specification;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public enum SearchOperation {
    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, START_WITH, END_WITH, CONTAINS;
    static final String[] SIMPLE_OPERATION_SET = {":", "!", ">", "<", "~"};
    static final String OR_PREDICATE_FLAG = "'";
    static final String ZERO_OR_MORE_REGEX = "*";
    static final String OR_OPERATOR = "OR";
    static final String AND_OPERATOR = "AND";
    static final String LEFT_PARENTHESIS = "(";
    static final String RIGHT_PARENTHESIS = ")";

    static SearchOperation getSimpleOperation(final char input) {
        return switch (input) {
            case ':' -> EQUALITY;
            case '!' -> NEGATION;
            case '>' -> GREATER_THAN;
            case '<' -> LESS_THAN;
            case '~' -> LIKE;
            default -> null;
        };
    }
}
