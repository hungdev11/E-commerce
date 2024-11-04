package com.pph.ecommerce.repository.specification;

import com.pph.ecommerce.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.pph.ecommerce.repository.specification.SearchOperation.*;

public class UserSpecificationsBuilder {
    private final List<SpecSearchCriteria> specs;

    public UserSpecificationsBuilder() {
        this.specs = new ArrayList<>();
    }
    public UserSpecificationsBuilder with(String key, String operation, String prefix, String value, String suffix) {
        return with(null, key, operation, prefix, value, suffix);
    }
    public UserSpecificationsBuilder with(String orPredicate, String key, String operation, String prefix, Object value, String suffix) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (Objects.nonNull(searchOperation)) {
            if (searchOperation == EQUALITY) {
                boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
                boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
                if (startWithAsterisk && endWithAsterisk) {
                    searchOperation = CONTAINS;
                } else if (startWithAsterisk) {
                    searchOperation = END_WITH;
                } else if (endWithAsterisk) {
                    searchOperation = START_WITH;
                }
            }
        }
        specs.add(new SpecSearchCriteria(orPredicate, key, searchOperation, value));
        return this;
    }
    public Specification<User> build() {
        if (specs.isEmpty()) return null;

        Specification<User> result = new UserSpecification(specs.get(0));

        for (int i = 1; i < specs.size(); i++) {
            result = specs.get(i).isOrPredicate()
                    ? Specification.where(result).or(new UserSpecification(specs.get(i)))
                    : Specification.where(result).and(new UserSpecification(specs.get(i)));
        }
        return result;
    }
}
