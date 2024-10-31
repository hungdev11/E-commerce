package com.pph.ecommerce.repository;

import com.pph.ecommerce.dto.response.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
