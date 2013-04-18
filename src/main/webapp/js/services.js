var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('SessionService', function($resource) {
	var actions = {
			get : {
				method : 'GET',
				params : {
					_method : 'get'
				}
			},
			save : {
				method : 'POST',
				params : {
					_method : 'save'
				}
			}
		};
	return $resource('rest/session/:_method', {}, actions);
});

module.factory('SubscriptionService', function($resource, $http) {

	var flatten = function(category, parentName, array) {
		if (!array)
			array = [];
		var name = category.name;
		if (parentName) {
			name += (' (in ' + parentName + ')');
		}
		array.push({
			id : category.id,
			name : name
		});
		if (category.children) {
			for ( var i = 0; i < category.children.length; i++) {
				flatten(category.children[i], category.name, array);
			}
		}
		return array;
	};
	var actions = {
		fetch : {
			method : 'GET',
			params : {
				_type : 'feed',
				_method : 'fetch'
			}
		},
		subscribe : {
			method : 'POST',
			params : {
				_type : 'feed',
				_method : 'subscribe'
			}
		},
		unsubscribe : {
			method : 'POST',
			params : {
				_type : 'feed',
				_method : 'unsubscribe'
			}
		},
		rename : {
			method : 'POST',
			params : {
				_type : 'feed',
				_method : 'rename'
			}
		},
		collapse : {
			method : 'POST',
			params : {
				_type : 'category',
				_method : 'collapse'
			}
		},
		addCategory : {
			method : 'POST',
			params : {
				_type : 'category',
				_method : 'add'
			}
		},
		deleteCategory : {
			method : 'POST',
			params : {
				_type : 'category',
				_method : 'delete'
			}
		},
		renameCategory : {
			method : 'POST',
			params : {
				_type : 'category',
				_method : 'rename'
			}
		}
	};
	var s = {};
	s.get = $resource('rest/subscriptions/get').get;
	s.subscriptions = {};
	s.flatCategories = {};

	var res = $resource('rest/subscriptions/:_type/:_method', {}, actions);
	s.init = function(callback) {
		s.get(function(data) {
			s.subscriptions = data;
			s.flatCategories = flatten(s.subscriptions);
			if (callback)
				callback(data);
		});
	};
	s.fetch = res.fetch;
	s.rename = res.rename;
	s.subscribe = function(sub, callback) {
		res.subscribe(sub, function(data) {
			s.init();
			if (callback)
				callback(data);
		});
	};

	var removeSubscription = function(node, subId) {
		if (node.children) {
			$.each(node.children, function(k, v) {
				removeSubscription(v, subId);
			});
		}
		if (node.feeds) {
			var foundAtIndex = -1;
			$.each(node.feeds, function(k, v) {
				if (v.id == subId) {
					foundAtIndex = k;
				}
			});
			if (foundAtIndex > -1) {
				node.feeds.splice(foundAtIndex, 1);
			}
		}

	};
	s.unsubscribe = function(id) {
		removeSubscription(s.subscriptions, id);
		res.unsubscribe({
			id : id
		});
	};
	s.addCategory = function(cat, callback) {
		res.addCategory(cat, function(data) {
			s.init();
			if (callback)
				callback(data);
		});
	};
	s.deleteCategory = res.deleteCategory;
	s.collapse = res.collapse;
	s.init();
	return s;
});

module.factory('EntryService', function($resource, $http) {
	var actions = {
		get : {
			method : 'GET',
			params : {
				_method : 'get'
			}
		},
		mark : {
			method : 'POST',
			params : {
				_method : 'mark'
			}
		}
	};
	var res = $resource('rest/entries/:type/:_method', {}, actions);
	res.search = $resource('rest/entries/search', {}, actions).get;
	return res;
});

module.factory('SettingsService', function($resource) {
	var s = {};
	s.settings = {};
	s.save = function(callback) {
		$resource('rest/settings/save').save(s.settings, function(data) {
			if (callback) {
				callback(data);
			}
		});
	};
	s.init = function(callback) {
		$resource('rest/settings/get').get(function(data) {
			s.settings = data;
			if (callback) {
				callback(data);
			}
		});
	};
	s.init();
	return s;
});

module.factory('AdminUsersService', function($resource) {
	var actions = {
		get : {
			method : 'GET',
			params : {
				_method : 'get'
			}
		},
		getAll : {
			method : 'GET',
			params : {
				_method : 'getAll'
			},
			isArray : true
		},
		save : {
			method : 'POST',
			params : {
				_method : 'save'
			}
		},
		remove : {
			method : 'POST',
			params : {
				_method : 'delete'
			}
		}
	};
	var res = $resource('rest/admin/user/:_method', {}, actions);
	return res;
});

module.factory('AdminSettingsService', function($resource) {
	var actions = {
		get : {
			method : 'GET',
			params : {
				_method : 'get'
			}
		},
		save : {
			method : 'POST',
			params : {
				_method : 'save'
			}
		}
	};
	var res = $resource('rest/admin/settings/:_method', {}, actions);
	return res;
});