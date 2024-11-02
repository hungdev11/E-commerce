package com.pph.ecommerce.repository.criteria;

import com.pph.ecommerce.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchQueryCriteriaConsumer implements Consumer<SearchCriteria> {
    CriteriaBuilder builder;
    Root<?> root;
    List<Predicate> predicates = new ArrayList<>();

    public UserSearchQueryCriteriaConsumer(CriteriaBuilder criteriaBuilder, Root<User> root) {
        builder = criteriaBuilder;
        this.root = root;
    }

    @Override
    public void accept(SearchCriteria param) {
        Predicate predicate = null;
        if (param.getOperation().equalsIgnoreCase(">")) {
            predicate = builder.greaterThan(root.get(param.getKey()), param.getValue().toString());
        } else if (param.getOperation().equalsIgnoreCase("<")) {
            predicate = builder.lessThan(root.get(param.getKey()), param.getValue().toString());
        } else if (param.getOperation().equalsIgnoreCase(":")) {
            if (root.get(param.getKey()).getJavaType() == String.class) {
                predicate = builder.like(root.get(param.getKey()), "%" + param.getValue() + "%");
            } else {
                predicate = builder.equal(root.get(param.getKey()), param.getValue());
            }
        }
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    public Predicate getCombinedPredicate() {
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}

