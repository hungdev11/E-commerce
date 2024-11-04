package com.pph.ecommerce.utils;

public interface AppConst {
    String SORT_BY = "(\\w+?)(:)(.*)";
    String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    String SEARCH_SPEC_OPERATOR = "(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)";
}
