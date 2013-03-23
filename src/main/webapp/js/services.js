var module = angular.module('commafeed.services', [ 'ngResource' ]);

module.factory('CategoryService', [ '$resource', '$http',
		function($resource, $http) {
			return $resource('rest/subscriptions');
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
			res = $resource('rest/entries/:_method/:_type/:_id/:_readtype', {},
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