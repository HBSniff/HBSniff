package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;
import java.util.Objects;

public final class IdNotInHashCodeEqualsEntity {

    @Id
    private String id;

    private String extraField;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getExtraField() {
        return extraField;
    }

    public void setExtraField(String extraField) {
        this.extraField = extraField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdNotInHashCodeEqualsEntity that = (IdNotInHashCodeEqualsEntity) o;
        return Objects.equals(getExtraField(), that.getExtraField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtraField());
    }
}