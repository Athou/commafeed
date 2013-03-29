var app = angular.module('commafeed', [ 'ui', 'ui.bootstrap', 'ui.state',
		'commafeed.directives', 'commafeed.controllers', 'commafeed.services',
		'ngSanitize', 'ngUpload', 'infinite-scroll', 'ngGrid' ]);

app.config(function($routeProvider, $stateProvider, $urlRouterProvider) {
	$stateProvider.state('feeds', {
		abstract : true,
		url : '/feeds',
		templateUrl : 'templates/feeds.html'
	});
	$stateProvider.state('feeds.view', {
		url : '/view/:_type/:_id',
		templateUrl : 'templates/feeds.view.html'
	});

	$stateProvider.state('admin', {
		abstract : true,
		url : '/admin',
		templateUrl : 'templates/admin.html'
	});
	$stateProvider.state('admin.users', {
		url : '/users',
		templateUrl : 'templates/admin.users.html'
	});

	$urlRouterProvider.when('/', '/feeds/view/category/all');
	$urlRouterProvider.when('/admin', '/admin/users');
	$urlRouterProvider.otherwise('/');

});