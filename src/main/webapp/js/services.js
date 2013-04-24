var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.service('AnalyticsService', [ '$state', function($state) {
	this.track = function(path) {
		path = path || $state.$current.url.prefix;
		if (!ga) {
			return;
		}
		ga('send', 'pageview', {
			page : path
		});
	};
} ]);


module.factory('ProfileService', ['$resource', function($resource) {
	return $resource('rest/user/profile/');
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
		rename : {
			method : 'POST',
			params : {
				_method : 'rename'
			}
		}
	};
	var res = $resource('rest/feed/:_method', {}, actions);
	return res;
}]);

module.factory('CategoryService', ['$resource', '$http', 
function($resource, $http) {
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
		rename : {
			method : 'POST',
			params : {
				_method : 'rename'
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

	res.init = function(callback) {
		res.get(function(data) {
			res.subscriptions = data;
			res.flatCategories = flatten(data);
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