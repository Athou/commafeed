package com.commafeed.frontend.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

public class MapToListModel<K, V> extends
		LoadableDetachableModel<List<Map.Entry<K, V>>> {

	private static final long serialVersionUID = 1L;

	private IModel<Map<K, V>> model;

	public MapToListModel(Map<K, V> map) {
		this.model = Model.ofMap(map);
	}

	public MapToListModel(IModel<Map<K, V>> model) {
		this.model = model;
	}

	@Override
	protected List<Entry<K, V>> load() {
		Map<K, V> map = model.getObject();
		return map == null ? null : Lists.newArrayList(map.entrySet());
	}

	@Override
	public void detach() {
		super.detach();
		model.detach();
	}

}
