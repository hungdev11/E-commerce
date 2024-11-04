package com.pph.ecommerce.repository;

import com.pph.ecommerce.dto.response.PageResponse;
import com.pph.ecommerce.entity.Address;
import com.pph.ecommerce.entity.User;
import com.pph.ecommerce.repository.criteria.SearchCriteria;
import com.pph.ecommerce.repository.criteria.UserSearchQueryCriteriaConsumer;
import com.pph.ecommerce.repository.specification.SpecSearchCriteria;
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

import static com.pph.ecommerce.utils.AppConst.*;

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

    public PageResponse<?> searchUserCriteriaWithJoin(Pageable pageable, String[] user, String[] address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        Join<Address, User> addressRoot = userRoot.join("addresses");

        List<Predicate> userPredicates = new ArrayList<>();
        Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
        for (String u : user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPredicates.add(toUserPredicate(userRoot, builder, searchCriteria));
            }
        }
        List<Predicate> addressPredicates = new ArrayList<>();
        for (String a : address) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addressPredicates.add(toAddressPredicate(addressRoot, builder, searchCriteria));
            }
        }
        Predicate userPre = builder.or(userPredicates.toArray(new Predicate[0]));
        Predicate addPre = builder.or(addressPredicates.toArray(new Predicate[0]));

        Predicate finalPre = builder.and(userPre, addPre);
        query.where(finalPre);

        List<User> users = entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        long count = countUserJoinAddress(user, address);
        return PageResponse.builder()
                .offset(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .total(count)
                .items(users)
                .build();
    }
    private Predicate toUserPredicate(Root<User> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toUserPredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case START_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case END_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    private Predicate toAddressPredicate(Join<Address, User> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toAddressPredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case START_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case END_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }
    private long countUserJoinAddress(String[] user, String[] address) {
        log.info("-------------- countUserJoinAddress --------------");

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> userRoot = query.from(User.class);
        Join<Address, User> addressRoot = userRoot.join("addresses");

        List<Predicate> userPreList = new ArrayList<>();

        Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
        for (String u : user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPreList.add(toUserPredicate(userRoot, builder, searchCriteria));
            }
        }

        List<Predicate> addressPreList = new ArrayList<>();
        for (String a : address) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addressPreList.add(toAddressPredicate(addressRoot, builder, searchCriteria));
            }
        }

        Predicate userPre = builder.or(userPreList.toArray(new Predicate[0]));
        Predicate addPre = builder.or(addressPreList.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(userPre, addPre);

        query.select(builder.count(userRoot));
        query.where(finalPre);

        return entityManager.createQuery(query).getSingleResult();
    }
}
