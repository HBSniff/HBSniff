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

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "student_fig1")
public class StudentFig1 extends PersonPerClassFig1 {

    public String university;

    public StudentFig1() {
        super();
        university = "ECUST";
        name = "studentName";
    }
}