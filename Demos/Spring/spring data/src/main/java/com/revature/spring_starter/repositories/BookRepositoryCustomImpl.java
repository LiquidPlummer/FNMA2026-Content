package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Book> search(String title, String lastName, Integer minYear) {

        StringBuilder jpql = new StringBuilder("SELECT b FROM Book b WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (title != null) {
            jpql.append(" AND LOWER(b.title) LIKE :title");
            params.put("title", "%" + title.toLowerCase() + "%");
        }
        if (lastName != null) {
            jpql.append(" AND b.author.lastName = :lastName");
            params.put("lastName", lastName);
        }
        if (minYear != null) {
            jpql.append(" AND b.publishedYear >= :minYear");
            params.put("minYear", minYear);
        }

        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }

}
