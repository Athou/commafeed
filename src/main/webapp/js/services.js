var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.service('AnalyticsService', [ '$state', function($state) {
	this.track = function(path) {
		if (typeof ga === 'undefined') {
			return;
		}
		path = path || $state.$current.url.prefix;
		ga('send', 'pageview', {
			page : path
		});
	};
} ]);

module.service('MobileService', [ '$state', function($state) {
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
	var width = (window.innerWidth > 0) ? window.innerWidth : screen.width;
	this.mobile = width < 979;
}]);


module.factory('ProfileService', ['$resource', function($resource) {
	var res = $resource('rest/user/profile/');
	res.deleteAccount = $resource('rest/user/profile/deleteAccount').save;
	return res;
}]);

module.factory('SettingsService', ['$resource', function($resource) {
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
			if (lang === 'zh') {
				lang = 'zh-cn';
			} else if (lang === 'ms') {
				lang = 'ms-my';
			}
			moment.lang(lang, {});
			if (callback) {
				callback(data);
			}
		});
	};
	s.init();
	return s;
}]);

module.factory('FeedService', ['$resource', '$http', 
function($resource, $http) {
	var actions = {
		entries : {
			method : 'GET',
			params : {
				_method : 'entries'
			}
		},
		fetch : {
			method : 'GET',
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

module.factory('CategoryService', ['$resource', '$http', 
function($resource, $http) {
	
	var traverse = function(callback, category, parentName) {
		callback(category, parentName);
		var children = category.children;
		if (children) {
			for ( var c = 0; c < children.length; c++) {
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
				orig: category
			});
		};
		traverse(callback, category);
		return array;
	};

	// flatten feeds
	var flatfeeds = function(category) {
		var subs = [];
		var callback = function(category) {
			subs.push.apply(subs, category.feeds);
		};
		traverse(callback, category);
		return subs;
	};
	var actions = {
		get : {
			method : 'GET',
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
			res.feeds = flatfeeds(data);
			if (callback)
				callback(data);
		});
	};
	res.refresh = function(callback) {
		res.get(function(data) {
			_.merge(res.subscriptions, data);
			if (callback)
				callback(data);
		});
	};

	res.init();
	return res;
}]);

module.factory('EntryService', ['$resource', '$http',
function($resource, $http) {
	var actions = {
		search : {
			method : 'GET',
			params : {
				_method : 'search'
			}
		},
		mark : {
			method : 'POST',
			params : {
				_method : 'mark'
			}
		},
		star : {
			method : 'POST',
			params : {
				_method : 'star'
			}
		}
	};
	var res = $resource('rest/entry/:_method', {}, actions);
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

module.factory('ServerService', ['$resource', function($resource) {
	var res =  $resource('rest/server/get');
	return res;
}]);