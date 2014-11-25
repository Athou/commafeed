var module = angular.module('commafeed.services', ['ngResource']);

module.service('AnalyticsService', ['$state', function($state) {
	this.track = function(path) {
		if (typeof ga === 'undefined') {
			return;
		}
		path = path || $state.$current.url.prefix;
		ga('send', 'pageview', {
			page : path
		});
	};
}]);

module.service('MobileService', ['$state', function($state) {
	this.leftMenu = false;
	this.rightMenu = false;
	this.toggleLeftMenu = function() {
		this.leftMenu = !this.leftMenu;
		$('body').toggleClass('left-menu-active');
	};
	this.toggleRightMenu = function() {
		this.rightMenu = !this.rightMenu;
		$('body').toggleClass('right-menu-active');
	};
	this.mobile = device.mobile() || device.tablet();
}]);

module.factory('ProfileService', ['$resource', function($resource) {
	var res = $resource('rest/user/profile/');
	res.deleteAccount = $resource('rest/user/profile/deleteAccount').save;
	return res;
}]);

module.factory('SessionService', ['$resource', function($resource) {
	var res = {};
	res.login = $resource('rest/user/login').save;
	res.register = $resource('rest/user/register').save;
	res.passwordReset = $resource('rest/user/passwordReset').save;
	return res;
}]);

module.factory('SettingsService', ['$resource', '$translate', function($resource, $translate) {
	var res = $resource('rest/user/settings');

	var s = {};
	s.settings = {};
	s.save = function(callback) {
		res.save(s.settings, function(data) {
			if (callback) {
				callback(data);
			}
		});
	};
	s.init = function(callback) {
		res.get(function(data) {
			s.settings = data;
			var lang = s.settings.language || 'en';
			$translate.use(lang);
			if (lang === 'zh') {
				lang = 'zh-cn';
			} else if (lang === 'ms') {
				lang = 'ms-my';
			}
			moment.locale(lang, {});
			if (callback) {
				callback(data);
			}
		});
	};
	s.init();
	return s;
}]);

module.factory('FeedService', ['$resource', '$http', function($resource, $http) {
	var actions = {
		entries : {
			method : 'GET',
			params : {
				_method : 'entries'
			}
		},
		fetch : {
			method : 'POST',
			params : {
				_method : 'fetch'
			}
		},
		mark : {
			method : 'POST',
			params : {
				_method : 'mark'
			}
		},
		refresh : {
			method : 'POST',
			params : {
				_method : 'refresh'
			}
		},
		refreshAll : {
			method : 'GET',
			params : {
				_method : 'refreshAll'
			}
		},
		subscribe : {
			method : 'POST',
			params : {
				_method : 'subscribe'
			}
		},
		unsubscribe : {
			method : 'POST',
			params : {
				_method : 'unsubscribe'
			}
		},
		modify : {
			method : 'POST',
			params : {
				_method : 'modify'
			}
		}
	};
	var res = $resource('rest/feed/:_method', {}, actions);
	res.get = $resource('rest/feed/get/:id').get;
	return res;
}]);

module.factory('CategoryService', ['$resource', '$http', function($resource, $http) {

	var traverse = function(callback, category, parentName) {
		callback(category, parentName);
		var children = category.children;
		if (children) {
			for (var c = 0; c < children.length; c++) {
				traverse(callback, children[c], category.name);
			}
		}
	};

	// flatten categories
	var flatten = function(category) {
		var array = [];
		var callback = function(category, parentName) {
			var name = category.name;
			if (parentName) {
				name += (' (in ' + parentName + ')');
			}
			array.push({
				id : category.id,
				name : name,
				orig : category
			});
		};
		traverse(callback, category);
		return array;
	};

	// flatten feeds
	var flatFeeds = function(category) {
		var subs = [];
		var callback = function(category) {
			subs.push.apply(subs, category.feeds);
		};
		traverse(callback, category);
		return subs;
	};

	// flatten everything
	var flatAll = function(category, a) {
		a.push([category.id, 'category']);
		_.each(category.children, function(child) {
			flatAll(child, a);
		});
		_.each(category.feeds, function(feed) {
			a.push([feed.id, 'feed']);
		});
	};

	var actions = {
		get : {
			method : 'GET',
			ignoreLoadingBar : true,
			params : {
				_method : 'get'
			}
		},
		entries : {
			method : 'GET',
			params : {
				_method : 'entries'
			}
		},
		mark : {
			method : 'POST',
			params : {
				_method : 'mark'
			}
		},
		add : {
			method : 'POST',
			params : {
				_method : 'add'
			}
		},
		remove : {
			method : 'POST',
			params : {
				_method : 'delete'
			}
		},
		modify : {
			method : 'POST',
			params : {
				_method : 'modify'
			}
		},
		collapse : {
			method : 'POST',
			params : {
				_method : 'collapse'
			}
		}
	};
	var res = $resource('rest/category/:_method', {}, actions);
	res.subscriptions = {};
	res.flatCategories = {};
	res.feeds = [];

	res.init = function(callback) {
		res.get(function(data) {
			res.subscriptions = data;
			res.flatCategories = flatten(data);
			res.feeds = flatFeeds(data);

			res.flatAll = [];
			flatAll(data, res.flatAll);
			res.flatAll.splice(1, 0, ['starred', 'category']);

			if (callback)
				callback(data);
		});
	};
	res.refresh = function(success, error) {
		res.get(function(data) {
			_.merge(res.subscriptions, data);
			if (success)
				success(data);
		}, function(data) {
			if (error)
				error(data);
		});
	};

	res.init();
	return res;
}]);

module.factory('EntryService', ['$resource', '$http', function($resource, $http) {
	var actions = {
		search : {
			method : 'GET',
			params : {
				_method : 'search'
			}
		},
		mark : {
			method : 'POST',
			ignoreLoadingBar : true,
			params : {
				_method : 'mark'
			}
		},
		markMultiple : {
			method : 'POST',
			params : {
				_method : 'markMultiple'
			}
		},
		star : {
			method : 'POST',
			params : {
				_method : 'star'
			}
		},
		tag : {
			method : 'POST',
			params : {
				_method : 'tag'
			}
		}
	};
	var res = $resource('rest/entry/:_method', {}, actions);
	res.tags = [];
	var initTags = function() {
		$http.get('rest/entry/tags').success(function(data) {
			res.tags = [];
			res.tags.push.apply(res.tags, data);
			res.tags.sort();
		});
	};
	var oldTag = res.tag;
	res.tag = function(data) {
		oldTag(data, function() {
			initTags();
		});
	};
	initTags();
	return res;
}]);

module.factory('AdminUsersService', ['$resource', function($resource) {
	var res = {};
	res.get = $resource('rest/admin/user/get/:id').get;
	res.getAll = $resource('rest/admin/user/getAll').query;
	res.save = $resource('rest/admin/user/save').save;
	res.remove = $resource('rest/admin/user/delete').save;
	return res;
}]);

module.factory('AdminSettingsService', ['$resource', function($resource) {
	var res = $resource('rest/admin/settings/');
	return res;
}]);

module.factory('AdminMetricsService', ['$resource', function($resource) {
	var res = $resource('rest/admin/metrics/');
	return res;
}]);

module.factory('ServerService', ['$resource', function($resource) {
	var res = $resource('rest/server/get');
	return res;
}]);