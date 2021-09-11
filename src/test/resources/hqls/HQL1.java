package dummy;

import com.adserver.entities.AdCategory;

import javax.persistence.Query;
import java.util.List;

public class HQL1{

    private static final String CATEGORY_FETCH_QUERY = "SELECT ac from AdCategory ac";

    @SuppressWarnings("unchecked")
    @Override
    public List<AdCategory> fetchAllCategory() {
        final Query query = getEntityManager().createQuery(CATEGORY_FETCH_QUERY, AdCategory.class);
        return query.getResultList();
    }
}
