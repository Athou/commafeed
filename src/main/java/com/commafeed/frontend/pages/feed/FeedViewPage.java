package com.commafeed.frontend.pages.feed;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.components.CssTreeView;
import com.commafeed.frontend.components.CssTreeView.ITreeProvider;
import com.commafeed.frontend.pages.BasePage;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.frontend.utils.stateless.StatelessAjaxLink;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class FeedViewPage extends BasePage {

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService feedCategoryService;

	public FeedViewPage() {
		add(newTree("tree"));
		add(new Label("entries", Model.of("")));
	}

	private Component newTree(String markupId) {
		ITreeProvider<FeedCategory, FeedSubscription> provider = new ITreeProvider<FeedCategory, FeedSubscription>() {

			private List<FeedCategory> cats;
			private List<FeedSubscription> subsWithoutCategory;

			private void init() {
				if (cats == null) {
					cats = feedCategoryService.findAll(CommaFeedSession.get()
							.getUser());
				}
				if (subsWithoutCategory == null) {
					subsWithoutCategory = feedSubscriptionService.findByField(
							MF.i(MF.p(FeedSubscription.class).getCategory()),
							null);
				}

			}

			@Override
			public Collection<FeedCategory> getChildren(final FeedCategory node) {
				init();
				return Lists.newArrayList(Collections2.filter(cats,
						new Predicate<FeedCategory>() {
							@Override
							public boolean apply(FeedCategory cat) {
								return (node == null && cat.getParent() == null)
										|| (cat.getParent() != null
												&& node != null && cat
												.getParent().getId() == node
												.getId());
							}
						}));
			}

			@Override
			public Collection<FeedSubscription> getLeaves(FeedCategory node) {
				init();
				if (node == null) {
					return subsWithoutCategory;
				}
				return node.getSubscriptions();
			}

			@Override
			public IModel<String> getChildLabel(FeedCategory node) {
				return Model.of(node.getName());
			}

			@Override
			public IModel<FeedSubscription> model(FeedSubscription object) {
				return Model.of(object);
			}

			@Override
			public void detach() {
				cats = null;
			}

		};
		return new CssTreeView<FeedCategory, FeedSubscription>(markupId,
				provider) {
			@Override
			protected Component newLink(String markupId,
					final IModel<FeedSubscription> model) {
				return new StatelessAjaxLink<Void>(markupId) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						System.out.println(model.getObject().getId());
					}
				}.setBody(Model.of(model.getObject().getTitle()));
			}
		};
	}
}
