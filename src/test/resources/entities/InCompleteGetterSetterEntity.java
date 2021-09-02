package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;
import java.util.Objects;

public final class InCompleteGetterSetterEntity {

    @Id
    private String id;

    private String missingGetterSetterField;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


}