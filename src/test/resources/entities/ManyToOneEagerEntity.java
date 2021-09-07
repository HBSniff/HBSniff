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

package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Dummy")
public class ManyToOneEagerEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    public OneToManyEntity parent;

    @Id
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}