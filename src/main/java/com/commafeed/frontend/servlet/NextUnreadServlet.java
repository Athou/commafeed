package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.resource.CategoryREST;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class NextUnreadServlet extends HttpServlet {

	public static final String PARAM_CATEGORYID = "category";
	public static final String PARAM_READINGORDER = "order";
	public static final String PARAM_APIKEY = "apiKey";

	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String categoryId = req.getParameter(PARAM_CATEGORYID);
		String orderParam = req.getParameter(PARAM_READINGORDER);
		String apiKey = req.getParameter(PARAM_APIKEY);

		if (apiKey == null) {
			resp.getWriter().write("api key is required");
			return;
		}

		User user = userService.login(apiKey);
		if (user == null) {
			resp.getWriter().write("unknown user or api key not found");
			return;
		}

		ReadingOrder order = ReadingOrder.desc;

		if (StringUtils.equals(orderParam, "asc")) {
			order = ReadingOrder.asc;
		}

		List<FeedEntryStatus> statuses = null;
		if (StringUtils.isBlank(categoryId) || CategoryREST.ALL.equals(categoryId)) {
			List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
			statuses = feedEntryStatusDAO.findBySubscriptions(user, subs, true, null, null, 0, 1, order, true, false, null);
		} else {
			FeedCategory category = feedCategoryDAO.findById(user, Long.valueOf(categoryId));
			if (category != null) {
				List<FeedCategory> children = feedCategoryDAO.findAllChildrenCategories(user, category);
				List<FeedSubscription> subscriptions = feedSubscriptionDAO.findByCategories(user, children);
				statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, null, null, 0, 1, order, true, false, null);
			}
		}

		if (CollectionUtils.isEmpty(statuses)) {
			resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
		} else {
			FeedEntryStatus status = Iterables.getFirst(statuses, null);
			String url = status.getEntry().getUrl();
			status.setRead(true);
			feedEntryStatusDAO.saveOrUpdate(status);
			resp.sendRedirect(resp.encodeRedirectURL(url));
		}
	}

}
