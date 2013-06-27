package liquibase.integration.cdi;

import java.sql.SQLException;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.sql.DataSource;

import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ResourceAccessor;

/**
 * temporary fix until https://liquibase.jira.com/browse/CORE-1325 is fixed
 */
public class CDIBootstrap implements Extension {

	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
	}

	void afterDeploymentValidation(@Observes AfterDeploymentValidation event,
			BeanManager manager) {
	}

	@Produces
	@LiquibaseType
	public CDILiquibaseConfig createConfig() {
		return null;
	}

	@Produces
	@LiquibaseType
	public DataSource createDataSource() throws SQLException {
		return null;
	}

	@Produces
	@LiquibaseType
	public ResourceAccessor create() {
		return null;
	}

}
