/*
 * This file is part of hqlSniffer.
 *
 *     hqlSniffer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     hqlSniffer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hqlsniffer.example.domain.fig1;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class UsedJoinFetch {

    public List<ManyToOneEagerEntity> findStudentsJoinFetch(Integer id) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tests.io.github.hzjdev.hqlsniffer.entities.ManyToOneEagerEntity");
        String hql = " FROM ManyToOneEagerEntity d";
        hql.append ("JOIN FETCH d.parent p ");
        hql.append ("WHERE name = : name ");

        Query q = emf.createEntityManager().createQuery(hql);
        return (List<ManyToOneEagerEntity>) q.getResultList();
    }

    public List<ManyToOneEagerEntity> students() {
        findStudentsJoinFetch(200);
    }

}
