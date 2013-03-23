var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('CategoryService', [ '$resource', '$http',
		function($resource, $http) {
			var actions = {
				'get' : {
					method : 'GET'
				}
			}
			res = $resource('rest/subscriptions', {}, actions);
			return res
		} ]);

module.factory('EntryService', [ '$resource', '$http',
		function($resource, $http) {
			var actions = {
				'getUnread' : {
					method : 'GET',
					params : {
						_method : 'get'
					}
				}
			}
			res = $resource('rest/entries/:_method/:_type/:_id/:_readtype', {}, actions);
			return res
		} ]);