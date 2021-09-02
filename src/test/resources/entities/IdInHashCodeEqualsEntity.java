package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;
import java.util.Objects;

public final class IdInHashCodeEqualsEntity {

    @Id
    private String id;

    private String extraField;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InCompleteGetterSetterEntity that = (InCompleteGetterSetterEntity) o;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}