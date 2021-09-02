package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import java.util.Collection;

@Entity
@Table(name="Dummy")
public class NonListOrSetCollectionEntity{

    @Id
    private String id;

    @OneToMany
    public Collection<NoArgConstructorEntity> lst;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}