package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name = "Dummy")
public class OneToManyLazyBatchSizeEntity {

    @BatchSize(size = 4)
    @OneToMany(fetch = FetchType.LAZY)
    public Collection<ManyToOneEntity> lst;
    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}