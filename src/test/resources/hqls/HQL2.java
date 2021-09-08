package dummy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.setsoft.crud.CrudGenerico;
import br.com.setsoft.modelo.Feedback;
import br.com.setsoft.utilidade.Fabrica;
import br.com.setsoft.utilidade.StringUtil;

public class HQL2 extends CrudGenerico<Feedback, Integer> {

    @Override
    protected Class<Feedback> getClassePersistente() {

        return Feedback.class;
    }

    @Override
    protected EntityManager getEntityManager() {

        return Fabrica.getEntityManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Feedback> buscarPorFiltro(final Feedback filtro) {

        final StringBuilder JPQL = new StringBuilder("SELECT x FROM Feedback x WHERE 1=1");

        final Map<String, Object> parametros = new HashMap<String, Object>();

        if (StringUtil.isPreenchida(filtro.getDescricao())) {
            JPQL.append(" AND UPPER(x.descricao) Like :descricaoFiltro");
            parametros.put("descricaoFiltro", "%" + filtro.getDescricao().trim().toUpperCase() + "%");
        }

        if (this.isNotNull(filtro.getTipoFeedback())) {
            JPQL.append(" AND x.tipoFeedback = :tipoFeedbackFiltro");
            parametros.put("tipoFeedbackFiltro", filtro.getTipoFeedback());
        }

        if (this.isNotNull(filtro.getColaborador())) {
            JPQL.append(" AND x.colaborador = :colaboradorFiltro");
            parametros.put("colaboradorFiltro", filtro.getColaborador());
        }

        final Query query = this.getEntityManager().createQuery(JPQL.toString());

        for (final String parametro : parametros.keySet()) {
            query.setParameter(parametro, parametros.get(parametro));
        }

        return query.getResultList();
    }

}
