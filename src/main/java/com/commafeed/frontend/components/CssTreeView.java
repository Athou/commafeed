package com.commafeed.frontend.components;

import java.util.Collection;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.commafeed.frontend.references.csstree.CssTreeViewReference;

@SuppressWarnings("serial")
public abstract class CssTreeView<T, V> extends Panel {

	private ITreeProvider<T, V> provider;

	public CssTreeView(String id, final ITreeProvider<T, V> provider) {
		super(id);
		setRenderBodyOnly(true);
		this.provider = provider;

		RepeatingView view = new RepeatingView("tree");
		addNode(null, view);
		add(view);
	}

	private void addNode(T node, RepeatingView view) {
		for (T child : provider.getChildren(node)) {
			Fragment fragment = new Fragment(view.newChildId(), "node",
					CssTreeView.this);
			CheckBox checkBox = new CheckBox("checkbox");
			checkBox.add(new AttributeModifier("checked", "checked"));
			fragment.add(checkBox);
			fragment.add(new FormComponentLabel("label", checkBox)
					.add(new Label("content", provider.getChildLabel(child))
							.setRenderBodyOnly(true)));

			RepeatingView viewChild = new RepeatingView("node");
			addNode(child, viewChild);
			fragment.add(viewChild);
			view.add(fragment);
		}
		for (V leaf : provider.getLeaves(node)) {
			Fragment fragment = new Fragment(view.newChildId(), "leaf",
					CssTreeView.this);
			fragment.add(newLink("link", provider.model(leaf)));
			view.add(fragment);
		}
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		provider.detach();
	}

	protected abstract Component newLink(String markupId, IModel<V> model);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		CssTreeViewReference.render(response);
	}

	public static interface ITreeProvider<T, V> extends IDetachable {

		Collection<T> getChildren(T node);

		IModel<String> getChildLabel(T node);

		Collection<V> getLeaves(T node);

		IModel<V> model(V object);
	}

}
