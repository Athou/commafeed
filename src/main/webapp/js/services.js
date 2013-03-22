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