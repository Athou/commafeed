var app = angular.module('commafeed.services');

app.factory('$templateCache', ['$cacheFactory', '$http', '$injector', function($cacheFactory, $http, $injector) {
	var cache = $cacheFactory('templates');
	var allTplPromise = null;

	return {
		get : function(url) {
			var fromCache = cache.get(url);

			if (fromCache) {
				return fromCache;
			}

			if (!allTplPromise) {
				var lang = $('html').attr('lang');
				allTplPromise = $http.get('templates/all-templates.' + lang + '.html?${timestamp}').then(
						function(response) {
							$injector.get('$compile')(response.data);
							return response;
						});
			}

			return allTplPromise.then(function(response) {
				return {
					status : response.status,
					data : cache.get(url)
				};
			});
		},

		put : function(key, value) {
			cache.put(key, value);
		}
	};
}]);