package com.pph.ecommerce.repository;

import com.pph.ecommerce.dto.response.PageResponse;
import com.pph.ecommerce.entity.Address;
import com.pph.ecommerce.entity.User;
import com.pph.ecommerce.repository.criteria.SearchCriteria;
import com.pph.ecommerce.repository.criteria.UserSearchQueryCriteriaConsumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pph.ecommerce.utils.AppConst.SEARCH_OPERATOR;
import static com.pph.ecommerce.utils.AppConst.SORT_BY;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchRepository {
    @PersistenceContext
    EntityManager entityManager;
    static String LIKE_FORMAT = "%%%s%%";

    public PageResponse<?> getAllUsersWithSortSingleColumnAndSearch(int offset, int limit, String search, String sortBy) {
        log.info("Execute search user with keyword = {}", search);
        StringBuilder query = new StringBuilder("SELECT new com.pph.ecommerce.dto.response.UserResponse(u.id, u.username, u.lastname) FROM User u WHERE 1=1");
        if (StringUtils.hasLength(search)) {
            query.append(" AND lower(u.username) LIKE lower(:username)");
            query.append(" OR lower(u.lastname) LIKE lower(:lastname)");
        }
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {query.append(String.format(" ORDER BY u.%s %s", matcher.group(1), matcher.group(3)));}
        }
        Query selectQuery = entityManager.createQuery(query.toString());
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("username", String.format(LIKE_FORMAT, search));
            selectQuery.setParameter("lastname", String.format(LIKE_FORMAT, search));
        }
        selectQuery.setMaxResults(limit);
        selectQuery.setFirstResult(offset);
        List<?> users = selectQuery.getResultList();
        StringBuilder sqlCountQuery = new StringBuilder("SELECT COUNT(*) FROM User u");
        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" WHERE lower(u.username) LIKE lower(?1)");
            sqlCountQuery.append(" OR lower(u.lastname) LIKE lower(?2)");
        }
        Query countQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            countQuery.setParameter(1, String.format(LIKE_FORMAT, search));
            countQuery.setParameter(2, String.format(LIKE_FORMAT, search));
            countQuery.getSingleResult();
        }
        Long totalElements = (Long) countQuery.getSingleResult();
        log.info("totalElements={}", totalElements);
        Pageable pageable = PageRequest.of(offset, limit);
        Page<?> page = new PageImpl<>(users, pageable, totalElements);
        return PageResponse.builder()
                .offset(offset)
                .limit(limit)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    public PageResponse<?> searchUserByCriteria(int offset, int limit, String address, String sortBy, String[] search) {
        log.info("Search user with search = {} and sortBy = {}", search, sortBy);

        List<SearchCriteria> criteriaList = parseSearchCriteria(search);
        List<User> users = getUsers(offset, limit, criteriaList, address, sortBy);
        Long totalElements = getTotalElements(criteriaList, address);

        Page<User> page = new PageImpl<>(users, PageRequest.of(offset, limit), totalElements);
        return PageResponse.builder()
                .offset(offset)
                .limit(limit)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    // Helper method to parse search criteria
    private List<SearchCriteria> parseSearchCriteria(String[] search) {
        List<SearchCriteria> criteriaList = new ArrayList<>();
        if (search.length > 0) {
            Pattern pattern = Pattern.compile(SEARCH_OPERATOR);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }
        return criteriaList;
    }

    private List<User> getUsers(int offset, int limit, List<SearchCriteria> criteriaList, String address, String sortBy) {
        log.info("-------------Get USERS--------------");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Predicate predicate = buildPredicate(criteriaBuilder, userRoot, criteriaList, address);
        query.where(predicate);

        applySorting(criteriaBuilder, query, userRoot, sortBy);

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList, String address) {
        log.info("-------------Get Total elements--------------");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = buildPredicate(criteriaBuilder, root, criteriaList, address);
        query.select(criteriaBuilder.count(root)).where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<User> root, List<SearchCriteria> criteriaList, String address) {
        List<Predicate> predicates = new ArrayList<>();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(criteriaBuilder, root);

        criteriaList.forEach(searchConsumer);
        predicates.add(searchConsumer.getCombinedPredicate());

        if (StringUtils.hasLength(address)) {
            Join<Address, User> addressJoin = root.join("addresses");
            predicates.add(criteriaBuilder.equal(addressJoin.get("city"), address));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void applySorting(CriteriaBuilder criteriaBuilder, CriteriaQuery<User> query, Root<User> userRoot, String sortBy) {
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String order = matcher.group(3);
                if ("desc".equalsIgnoreCase(order)) {
                    query.orderBy(criteriaBuilder.desc(userRoot.get(fieldName)));
                } else {
                    query.orderBy(criteriaBuilder.asc(userRoot.get(fieldName)));
                }
            }
        }
    }

}
