var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('CategoryService', [ '$resource', '$http',
		function($resource, $http) {
			var actions = {
				'get' : {
					method : 'GET'
				}
			}
			res = $resource('subscriptions', {}, actions);
			return res
		} ]);

module.factory('EntryService', [ '$resource', '$http',
		function($resource, $http) {
			var actions = {
				'getUnread' : {
					method : 'GET',
					params : {
						_type : 'category',
						_id : '1',
						_readtype : 'unread',
					}
				}
			}
			res = $resource('entries/:_type/:_id/:_readtype', {}, actions);
			return res
		} ]);