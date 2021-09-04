package tests.io.github.hzjdev.hqlsniffer.entities;


import javax.persistence.Id;
import java.io.Serializable;

public class SerializableEntity implements Serializable {

    private String id;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}