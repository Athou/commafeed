package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.resource.CategoryREST;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class NextUnreadServlet extends HttpServlet {

	private static final String PARAM_CATEGORYID = "category";
	private static final String PARAM_READINGORDER = "order";

	private final SessionFactory sessionFactory;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String categoryId = req.getParameter(PARAM_CATEGORYID);
		String orderParam = req.getParameter(PARAM_READINGORDER);

		final User user = (User) req.getSession().getAttribute(CommaFeedApplication.SESSION_USER);
		if (user == null) {
			resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
			return;
		}

		final ReadingOrder order = StringUtils.equals(orderParam, "asc") ? ReadingOrder.asc : ReadingOrder.desc;

		FeedEntryStatus status = new UnitOfWork<FeedEntryStatus>(sessionFactory) {
			@Override
			protected FeedEntryStatus runInSession() throws Exception {
				FeedEntryStatus status = null;
				if (StringUtils.isBlank(categoryId) || CategoryREST.ALL.equals(categoryId)) {
					List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
					List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subs, true, null, null, 0, 1, order,
							true, false, null);
					status = Iterables.getFirst(statuses, null);
				} else {
					FeedCategory category = feedCategoryDAO.findById(user, Long.valueOf(categoryId));
					if (category != null) {
						List<FeedCategory> children = feedCategoryDAO.findAllChildrenCategories(user, category);
						List<FeedSubscription> subscriptions = feedSubscriptionDAO.findByCategories(user, children);
						List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, null, null, 0,
								1, order, true, false, null);
						status = Iterables.getFirst(statuses, null);
					}
				}
				if (status != null) {
					status.setRead(true);
					feedEntryStatusDAO.saveOrUpdate(status);
				}
				return status;
			}
		}.run();

		if (status == null) {
			resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
		} else {
			String url = status.getEntry().getUrl();
			resp.sendRedirect(resp.encodeRedirectURL(url));
		}
	}

}
