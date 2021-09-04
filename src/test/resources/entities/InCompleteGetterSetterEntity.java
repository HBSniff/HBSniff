package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;

public final class InCompleteGetterSetterEntity {

    @Id
    private String id;

    private String missingGetterSetterField;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}