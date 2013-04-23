var app = angular.module('commafeed', [ 'ui', 'ui.bootstrap', 'ui.state',
		'commafeed.directives', 'commafeed.controllers', 'commafeed.services',
		'ngSanitize', 'ngUpload', 'infinite-scroll', 'ngGrid' ]);

app.config(['$routeProvider', '$stateProvider', '$urlRouterProvider',
function($routeProvider, $stateProvider, $urlRouterProvider) {
	$stateProvider.state('feeds', {
		abstract : true,
		url : '/feeds',
		templateUrl : 'templates/feeds.html'
	});
	$stateProvider.state('feeds.view', {
		url : '/view/:_type/:_id',
		templateUrl : 'templates/feeds.view.html',
		controller : 'FeedListCtrl'
	});
	$stateProvider.state('feeds.search', {
		url : '/search/:_keywords',
		templateUrl : 'templates/feeds.view.html',
		controller : 'FeedListCtrl'
	});
	$stateProvider.state('feeds.help', {
		url : '/help',
		templateUrl : 'templates/feeds.help.html'
	});

	$stateProvider.state('admin', {
		abstract : true,
		url : '/admin',
		templateUrl : 'templates/admin.html'
	});
	$stateProvider.state('admin.userlist', {
		url : '/user/list',
		templateUrl : 'templates/admin.userlist.html',
		controller : 'ManageUsersCtrl'
	});
	$stateProvider.state('admin.useradd', {
		url : '/user/add',
		templateUrl : 'templates/admin.useradd.html',
		controller : 'ManageUserCtrl'
	});
	$stateProvider.state('admin.useredit', {
		url : '/user/edit/:_id',
		templateUrl : 'templates/admin.useredit.html',
		controller : 'ManageUserCtrl'
	});
	$stateProvider.state('admin.settings', {
		url : '/settings',
		templateUrl : 'templates/admin.settings.html',
		controller : 'ManageSettingsCtrl'
	});

	$stateProvider.state('settings', {
		url : '/settings',
		templateUrl : 'templates/settings.html',
		controller : 'SettingsCtrl'
	});

	$stateProvider.state('profile', {
		url : '/profile',
		templateUrl : 'templates/profile.html',
		controller : 'ProfileCtrl'
	});

	$urlRouterProvider.when('/', '/feeds/view/category/all');
	$urlRouterProvider.when('/admin', '/admin/settings');
	$urlRouterProvider.otherwise('/');

}]);