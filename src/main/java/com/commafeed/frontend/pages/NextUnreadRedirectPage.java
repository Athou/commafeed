package com.commafeed.frontend.pages;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.rest.resources.CategoryREST;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
@SecurityCheck(Role.USER)
public class NextUnreadRedirectPage extends WebPage {

	public static final String PARAM_CATEGORYID = "category";

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	public NextUnreadRedirectPage(PageParameters params) {
		String categoryId = params.get(PARAM_CATEGORYID).toString();
		User user = CommaFeedSession.get().getUser();

		List<FeedEntryStatus> statuses = null;
		if (StringUtils.isBlank(categoryId)
				|| CategoryREST.ALL.equals(categoryId)) {
			statuses = feedEntryStatusDAO.findAll(user, true, 0, 1,
					ReadingOrder.desc, true);
		} else {
			FeedCategory category = feedCategoryDAO.findById(user,
					Long.valueOf(categoryId));
			if (category != null) {
				List<FeedCategory> children = feedCategoryDAO
						.findAllChildrenCategories(user, category);
				statuses = feedEntryStatusDAO.findByCategories(children, user,
						true, 0, 1, ReadingOrder.desc, false);
			}
		}

		if (CollectionUtils.isEmpty(statuses)) {
			setResponsePage(HomePage.class);
		} else {
			FeedEntryStatus status = Iterables.getFirst(statuses, null);
			String url = status.getEntry().getUrl();
			status.setRead(true);
			feedEntryStatusDAO.saveOrUpdate(status);
			throw new RedirectToUrlException(url);
		}
	}

}
