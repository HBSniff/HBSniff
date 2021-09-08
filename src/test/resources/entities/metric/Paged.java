/*
 * This file is part of HBSniff.
 *
 *     HBSniff is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     HBSniff is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with HBSniff.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.example.domain.fig1;

import io.github.hzjdev.hbsniff.example.mapping.domain.fig1.StudentFig1;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class Paged {

    public List<StudentFig1> findStudents(String name) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("io.github.hzjdev.hqlsniffer.example.domain.fig1.StudentFig1");
        String hql = " FROM StudentFig1 d WHERE name = : name ";
        Query q = emf.createEntityManager().createQuery(hql);
        return (List<StudentFig1>) q.getResultList();
    }

    public List<StudentFig1> students(String name, int page, int limit) {
        int fromIndex = (page - 1) * limit;
        List<StudentFig1> students = findStudents(name);
        return students.subList(fromIndex, Math.min(
                fromIndex + limit, students.size()));
    }

}
