package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.SecurityCheck;

@SecurityCheck(Role.ADMIN)
@Path("admin/metrics")
public class AdminMetricsREST extends AbstractREST {

	@Inject
	MetricsBean metricsBean;

	@Path("/get")
	@GET
	public int[] get() {
		return new int[] { metricsBean.getFeedsRefreshedLastMinute(),
				metricsBean.getFeedsRefreshedLastHour() };
	}

}
