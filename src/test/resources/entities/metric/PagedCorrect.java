package io.github.hzjdev.hqlsniffer.example.domain.fig1;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class Paged {

    public List<StudentFig1> findStudentsPaged(String name, int fromIndex, int limit) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("io.github.hzjdev.hqlsniffer.example.domain.fig1.StudentFig1");
        String hql = " FROM StudentFig1 d WHERE name = : name ";
        Query q = emf.createEntityManager().createQuery(hql);
        q.setFirstResult(fromIndex);
        q.setMaxResults(limit);
        return (List<StudentFig1>) q.getResultList();
    }

    public List<StudentFig1> students(String name, int page, int limit) {
        int fromIndex = (page - 1) * limit;
        List<StudentFig1> students = findStudentsPaged(name, fromIndex, limit);
        return students.subList(fromIndex, Math.min(
                fromIndex + limit, students.size()));
    }

}
