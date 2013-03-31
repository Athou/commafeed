var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('SessionService', function($resource) {
	return $resource('rest/session/get');
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
				_method : 'fetch'
			}
		},
		get : {
			method : 'GET',
			params : {
				_method : ''
			}
		},
		subscribe : {
			method : 'POST',
			params : {
				_method : 'subscribe'
			}
		},
		unsubscribe : {
			method : 'GET',
			params : {
				_method : 'unsubscribe'
			}
		},
		collapse : {
			method : 'GET',
			params : {
				_method : 'collapse'
			}
		},
		addCategory : {
			method : 'GET',
			params : {
				_method : 'addCategory'
			}
		},
		deleteCategory : {
			method : 'GET',
			params : {
				_method : 'deleteCategory'
			}
		}
	};
	var s = {};
	s.subscriptions = {};
	s.flatCategories = {};

	var res = $resource('rest/subscriptions/:_method', {}, actions);
	s.init = function(callback) {
		s.subscriptions = res.get(function(data) {
			s.flatCategories = flatten(s.subscriptions);
			if (callback)
				callback(data);
		});
	};
	s.fetch = res.fetch;
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
			method : 'GET',
			params : {
				_method : 'mark'
			}
		}
	};
	var res = $resource('rest/entries/:_method', {}, actions);
	return res;
});

module.factory('SettingsService', function($resource) {
	var s = {};
	s.settings = {};
	$resource('rest/settings/get').get(function(data) {
		s.settings = data;
	});
	s.save = function() {
		$resource('rest/settings/save').save(s.settings);
	};
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
			method : 'GET',
			params : {
				_method : 'delete'
			}
		}
	};
	var res = $resource('rest/admin/users/:_method', {}, actions);
	return res;
});