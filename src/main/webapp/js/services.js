var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('SubscriptionService', [ '$resource', '$http',
		function($resource, $http) {
	
		    var flatten = function(category, parentName, array) {
		    	if(!array) array = [];
				array.push({
					id : category.id,
					name : category.name + (parentName ? (' (in ' + parentName + ')') : '')
				});
				if (category.children) {
					for ( var i = 0; i < category.children.length; i++) {
						flatten(category.children[i], category.name, array);
					}
				}
				return array;
			}
			var actions = {
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
				}
			};
			var s = {};
			s.subscriptions = {};
			s.flatCategories = {};
			
			var res = $resource('rest/subscriptions/:_method', {}, actions);
			s.init = function(callback) {
				s.subscriptions = res.get(function(data) {
					s.flatCategories = flatten(s.subscriptions);
					callback(data);
				});
			};
			s.subscribe = function(sub, callback) {
				res.subscribe(sub, callback);
				s.init();
			};
			s.unsubscribe = function(id, callback) {
				res.unsubscribe({
					id : id
				}, callback);
				s.init();
			};
			s.init();
			return s;
		} ]);

module.factory('EntryService', [
		'$resource',
		'$http',
		function($resource, $http) {
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
			res = $resource('rest/entries/:_method', {},
					actions);
			return res;
		} ]);

module.service('SettingsService', function($resource) {
	var s = {}
	s.settings = $resource('rest/settings/get').get();
	s.save = function() {
		$resource('rest/settings/save').save(s.settings);
	};
	return s;
});