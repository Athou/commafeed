package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;

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
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.resource.CategoryREST;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class NextUnreadServlet extends HttpServlet {

	private static final String PARAM_CATEGORYID = "category";
	private static final String PARAM_READINGORDER = "order";

	private final SessionFactory sessionFactory;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String categoryId = req.getParameter(PARAM_CATEGORYID);
		String orderParam = req.getParameter(PARAM_READINGORDER);

		final Optional<User> user = new UnitOfWork<Optional<User>>(sessionFactory) {
			@Override
			protected Optional<User> runInSession() throws Exception {
				return userService.login(req.getSession());
			}
		}.run();
		if (!user.isPresent()) {
			resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
			return;
		}

		final ReadingOrder order = StringUtils.equals(orderParam, "asc") ? ReadingOrder.asc : ReadingOrder.desc;

		FeedEntryStatus status = new UnitOfWork<FeedEntryStatus>(sessionFactory) {
			@Override
			protected FeedEntryStatus runInSession() throws Exception {
				FeedEntryStatus status = null;
				if (StringUtils.isBlank(categoryId) || CategoryREST.ALL.equals(categoryId)) {
					List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user.get());
					List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user.get(), subs, true, null, null, 0, 1,
							order, true, false, null);
					status = Iterables.getFirst(statuses, null);
				} else {
					FeedCategory category = feedCategoryDAO.findById(user.get(), Long.valueOf(categoryId));
					if (category != null) {
						List<FeedCategory> children = feedCategoryDAO.findAllChildrenCategories(user.get(), category);
						List<FeedSubscription> subscriptions = feedSubscriptionDAO.findByCategories(user.get(), children);
						List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user.get(), subscriptions, true, null,
								null, 0, 1, order, true, false, null);
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
