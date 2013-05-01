var module = angular.module('commafeed.controllers', []);

module.run(['$rootScope', function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
	$rootScope.$on('emitMarkAll', function(event, args) {
		// args.type
		// args.id
		// args.read
		$rootScope.$broadcast('markAll', args);
	});
	$rootScope.$on('emitReload', function(event, args) {
		$rootScope.$broadcast('reload');
	});
}]);

module.controller('SubscribeCtrl', ['$scope', 'FeedService', 'CategoryService', 
function($scope, FeedService, CategoryService) {

	$scope.opts = {
		backdropFade : true,
		dialogFade : true
	};

	$scope.isOpen = false;
	$scope.isOpenImport = false;
	$scope.sub = {};

	$scope.CategoryService = CategoryService;

	$scope.open = function() {
		$scope.sub = {
			categoryId: 'all'
		};
		$scope.isOpen = true;
	};

	$scope.close = function() {
		$scope.isOpen = false;
	};

	// 'ok', 'loading' or 'failed'
	$scope.state = 'ok';
	$scope.urlChanged = function() {
		if ($scope.sub.url) {
			$scope.state = 'loading';
			$scope.sub.title = 'Loading...';
			FeedService.fetch({
				url : $scope.sub.url
			}, function(data) {
				$scope.state = 'ok';
				$scope.sub.title = data.title;
				$scope.sub.url = data.url;
			}, function(data) {
				$scope.state = 'failed';
				$scope.sub.title = 'Loading failed. Invalid feed?';
			});
		}
	};

	$scope.save = function() {
		if ($scope.state != 'ok') {
			return;
		}
		if (!$scope.sub.categoryId) {
			return;
		}
		FeedService.subscribe($scope.sub, function() {
			CategoryService.init();
		});
		$scope.close();
	};

	$scope.openImport = function() {
		$scope.isOpenImport = true;
	};

	$scope.closeImport = function() {
		$scope.isOpenImport = false;
	};

	$scope.uploadComplete = function(contents, completed) {
		CategoryService.init();
		$scope.closeImport();
	};

	$scope.cat = {};

	$scope.openCategory = function() {
		$scope.isOpenCategory = true;
		$scope.cat = {
			parentId: 'all'
		};
	};

	$scope.closeCategory = function() {
		$scope.isOpenCategory = false;
	};

	$scope.saveCategory = function() {
		CategoryService.add($scope.cat, function() {
			CategoryService.init();
		});
		$scope.closeCategory();
	};
}]);

module.controller('CategoryTreeCtrl', ['$scope', '$timeout', '$stateParams', '$window', 
	'$location', '$state', '$route', 'CategoryService',
function($scope, $timeout, $stateParams, $window, $location, $state, $route, CategoryService) {

	$scope.selectedType = $stateParams._type;
	$scope.selectedId = $stateParams._id;

	$scope.starred = {
		id: 'starred',
		name: 'Starred'
	};
	
	$scope.$on('$stateChangeSuccess', function() {
		$scope.selectedType = $stateParams._type;
		$scope.selectedId = $stateParams._id;
	});

	$timeout(function refreshTree() {
		CategoryService.init(function() {
			$timeout(refreshTree, 15000);
		});
	}, 15000);

	$scope.CategoryService = CategoryService;

	$scope.unreadCount = function(category) {
		var count = 0;
		var i;
		if (category.children) {
			for (i = 0; i < category.children.length; i++) {
				count = count + $scope.unreadCount(category.children[i]);
			}
		}
		if (category.feeds) {
			for (i = 0; i < category.feeds.length; i++) {
				var feed = category.feeds[i];
				count = count + feed.unread;
			}
		}
		return count;
	};

	var rootUnreadCount = function() {
		return $scope.unreadCount($scope.CategoryService.subscriptions);
	};

	$scope.$watch(rootUnreadCount, function(value) {
		var label = 'CommaFeed';
		if (value > 0) {
			label = value + ' - ' + label;
		}
		$window.document.title = label;
	});

	var mark = function(node, entry) {
		var i;
		if (node.children) {
			for (i = 0; i < node.children.length; i++) {
				mark(node.children[i], entry);
			}
		}
		if (node.feeds) {
			for (i = 0; i < node.feeds.length; i++) {
				var feed = node.feeds[i];
				if (feed.id == entry.feedId) {
					var c = entry.read ? -1 : 1;
					feed.unread = feed.unread + c;
				}
			}
		}
	};

	$scope.$on('mark', function(event, args) {
		mark($scope.CategoryService.subscriptions, args.entry);
	});
}]);

module.controller('FeedDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', '$dialog', 
    function($scope, $state, $stateParams, FeedService, CategoryService, $dialog) {
	
	$scope.CategoryService = CategoryService;
	
	$scope.sub = FeedService.get({
		id : $stateParams._id
	}, function(data) {
		if (!data.categoryId)
			data.categoryId = 'all';
	});
	
	$scope.back = function() {
		$state.transitionTo('feeds.view', {
			_id: $stateParams._id,
			_type: 'feed'
		});
	};
	
	$scope.unsubscribe = function() {
		var sub = $scope.sub;
		var title = 'Unsubscribe';
		var msg = 'Unsubscribe from ' + sub.name + ' ?';
		var btns = [ {
			result : 'cancel',
			label : 'Cancel'
		}, {
			result : 'ok',
			label : 'OK',
			cssClass : 'btn-primary'
		} ];

		$dialog.messageBox(title, msg, btns).open().then(
				function(result) {
					if (result == 'ok') {
						var data = {
							id : sub.id
						};
						FeedService.unsubscribe(data,
								function() {
									CategoryService.init();
								});
						$state.transitionTo('feeds.view', {
							_id: 'all',
							_type: 'category'
						});
					}
				});
	};
	
	$scope.save = function() {
		var sub = $scope.sub;
		FeedService.modify({
			id : sub.id,
			name : sub.name,
			categoryId : sub.categoryId
		}, function() {
			CategoryService.init();
			$state.transitionTo('feeds.view', {
				_id: 'all',
				_type: 'category'
			});
		});
	};
}]);

module.controller('CategoryDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', '$dialog', 
                                      function($scope, $state, $stateParams, FeedService, CategoryService, $dialog) {
	$scope.CategoryService = CategoryService;
	
	$scope.isMeta = function() {
		return parseInt($stateParams._id, 10) != $stateParams._id;
	};
	
	$scope.filterCurrent = function(elem) {
		if (!$scope.category)
			return true;
		return elem.id != $scope.category.id;
	}; 
	
	CategoryService.get(function() {
		if ($scope.isMeta()) {
			$scope.category = {
				id : $stateParams._id,
				name : $stateParams._id
			};
			return;
		}
		for (var i = 0; i < CategoryService.flatCategories.length; i++) {
			var cat = CategoryService.flatCategories[i];
			if (cat.id == $stateParams._id) {
				$scope.category = {
					id: cat.id,
					name: cat.orig.name,
					parentId: cat.orig.parentId
				};
				break;
			}
		}
		if (!$scope.category.parentId)
			$scope.category.parentId = 'all';
	});
	
	$scope.back = function() {
		$state.transitionTo('feeds.view', {
			_id: $stateParams._id,
			_type: 'category'
		});
	};
	
	$scope.deleteCategory = function() {
		var category = $scope.category;
		var title = 'Delete category';
		var msg = 'Delete category ' + category.name + ' ?';
		var btns = [ {
			result : 'cancel',
			label : 'Cancel'
		}, {
			result : 'ok',
			label : 'OK',
			cssClass : 'btn-primary'
		} ];

		$dialog.messageBox(title, msg, btns).open().then(
				function(result) {
					if (result == 'ok') {
						CategoryService.remove({
							id : category.id
						}, function() {
							CategoryService.init();
						});
						$state.transitionTo('feeds.view', {
							_id: 'all',
							_type: 'category'
						});
					}
				});
	};
	
	$scope.save = function() {
		var cat = $scope.category;
		CategoryService.modify({
			id : cat.id,
			name : cat.name,
			parentId : cat.parentId
		}, function() {
			CategoryService.init();
			$state.transitionTo('feeds.view', {
				_id: 'all',
				_type: 'category'
			});
		});
	};
}]);

module.controller('ToolbarCtrl', ['$scope', '$http', '$state', '$stateParams', 
	'$route', '$location', 'SettingsService', 'EntryService', 'ProfileService', 'AnalyticsService', 'ServerService', 'FeedService',
function($scope, $http, $state, $stateParams, $route, $location,
		SettingsService, EntryService, ProfileService, AnalyticsService, ServerService, FeedService) {

	function totalActiveAjaxRequests() {
		return ($http.pendingRequests.length + $.active);
	}

	$scope.session = ProfileService.get();
	$scope.ServerService = ServerService.get();

	$scope.loading = true;
	$scope.$watch(totalActiveAjaxRequests, function() {
		$scope.loading = (totalActiveAjaxRequests() !== 0);
	});

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(
			newValue, oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			SettingsService.save();
		}
	});
	$scope.$watch('settingsService.settings.readingOrder', function(
			newValue, oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			SettingsService.save();
		}
	});
	$scope.refresh = function() {
		if($stateParams._type == 'feed'){
			FeedService.refresh({
				id : $stateParams._id
			});
		}
		$scope.$emit('emitReload');
		
	};
	$scope.markAllAsRead = function() {
		$scope.$emit('emitMarkAll', {
			type : $stateParams._type,
			id : $stateParams._id,
			read : true
		});
	};

	$scope.keywords = $stateParams._keywords;
	$scope.search = function() {
		if ($scope.keywords == $stateParams._keywords) {
			$scope.refresh();
		} else {
			$state.transitionTo('feeds.search', {
				_keywords : $scope.keywords
			});
		}
	};
	$scope.showButtons = function() {
		return !$stateParams._keywords;
	};

	$scope.toggleOrder = function() {
		var settings = $scope.settingsService.settings;
		settings.readingOrder = settings.readingOrder == 'asc' ? 'desc'
				: 'asc';
	};

	$scope.toAdmin = function() {
		$location.path('admin');
	};
	$scope.toSettings = function() {
		$location.path('settings');
	};
	$scope.toProfile = function() {
		$location.path('profile');
	};
	$scope.toHelp = function() {
		$state.transitionTo('feeds.help');
	};
	$scope.toDonate = function() {
		AnalyticsService.track('/donate');
		$state.transitionTo('feeds.help');
	};
}]);

module.controller('FeedListCtrl', ['$scope', '$stateParams', '$http', '$route', 
	'$window', 'EntryService', 'SettingsService', 'FeedService', 'CategoryService', 'AnalyticsService',
function($scope, $stateParams, $http, $route, $window, EntryService, SettingsService, FeedService, CategoryService, AnalyticsService) {
	
	AnalyticsService.track();

	$scope.selectedType = $stateParams._type;
	$scope.selectedId = $stateParams._id;
	$scope.keywords = $stateParams._keywords;

	$scope.name = null;
	$scope.message = null;
	$scope.errorCount = 0;
	$scope.timestamp = 0;
	$scope.entries = [];

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(newValue,
			oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			$scope.$emit('emitReload');
		}
	});
	$scope.$watch('settingsService.settings.readingOrder', function(newValue,
			oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			$scope.$emit('emitReload');
		}
	});

	$scope.limit = 10;
	$scope.busy = false;
	$scope.hasMore = true;

	$scope.loadMoreEntries = function() {
		if (!$scope.hasMore)
			return;
		if ($scope.busy)
			return;
		$scope.busy = true;

		var limit = $scope.limit;
		if ($scope.entries.length === 0) {
			$window = angular.element($window);
			limit = $window.height() / 33;
			limit = parseInt(limit, 10) + 5;
		}

		var callback = function(data) {
			for ( var i = 0; i < data.entries.length; i++) {
				$scope.entries.push(data.entries[i]);
			}
			$scope.name = data.name;
			$scope.message = data.message;
			$scope.errorCount = data.errorCount;
			$scope.timestamp = data.timestamp;
			$scope.busy = false;
			$scope.hasMore = data.entries.length == limit;
		};
		if (!$scope.keywords) {
			var service = $scope.selectedType == 'feed' ? FeedService
					: CategoryService;
			service.entries({
				id : $scope.selectedId,
				readType : $scope.settingsService.settings.readingMode,
				order : $scope.settingsService.settings.readingOrder,
				offset : $scope.entries.length,
				limit : limit
			}, callback);
		} else {
			EntryService.search({
				keywords : $scope.keywords,
				offset : $scope.entries.length,
				limit : limit
			}, callback);
		}
	};

	$scope.mark = function(entry, read) {
		if (entry.read != read) {
			entry.read = read;
			$scope.$emit('emitMark', {
				entry : entry
			});
			EntryService.mark({
				id : entry.id,
				read : read
			});
		}
	};
	
	$scope.star = function(entry, star, event) {
		event.preventDefault();
		event.stopPropagation();
		if (entry.starred != star) {
			entry.starred = star;
			EntryService.star({
				id : entry.id,
				starred : star
			});
		}
	};

	$scope.isOpen = false;
	$scope.entryClicked = function(entry, event) {
		if (!event.ctrlKey && event.which != 2) {
			if ($scope.current != entry) {
				$scope.isOpen = true;
			} else {
				$scope.isOpen = !$scope.isOpen;
			}
			if ($scope.isOpen) {
				$scope.mark(entry, true);
			}
			$scope.current = entry;
			event.preventDefault();
			event.stopPropagation();
		} else {
			$scope.mark(entry, true);
		}
	};

	$scope.noop = function(event) {
		if (!event.ctrlKey && event.which != 2) {
			event.preventDefault();
			event.stopPropagation();
		}
	};

	var openNextEntry = function(event) {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index + 1;
			if (index < $scope.entries.length) {
				entry = $scope.entries[index];
			}
		} else if ($scope.entries.length > 0) {
			entry = $scope.entries[0];
		}
		if (entry) {
			$scope.entryClicked(entry, event);
		}
	};

	var openPreviousEntry = function(event) {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index - 1;
			if (index >= 0) {
				entry = $scope.entries[index];
			}
		}
		if (entry) {
			$scope.entryClicked(entry, event);
		}
	};


	Mousetrap.bind('j', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		});
	});
	Mousetrap.bind('k', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		});
	});
	Mousetrap.bind('space', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		});
	});
	Mousetrap.bind('shift+space', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		});
	});
	Mousetrap.bind('o', function(e) {
		$scope.$apply(function() {
			if ($scope.current) {
				$scope.entryClicked($scope.current, e);
			}
		});
	});
	Mousetrap.bind('enter', function(e) {
		$scope.$apply(function() {
			if ($scope.current) {
				$scope.entryClicked($scope.current, e);
			}
		});
	});
	Mousetrap.bind('r', function(e) {
		$scope.$apply(function() {
			$scope.$emit('emitReload');
		});
	});
	Mousetrap.bind('v', function(e) {
		if ($scope.current) {
			window.open($scope.current.url);
		}
	});
	Mousetrap.bind('?', function(e) {
		$scope.$apply(function() {
			$scope.shortcutsModal = true;
		});
	});

	$scope.$on('markAll', function(event, args) {
		var service = $scope.selectedType == 'feed' ? FeedService
				: CategoryService;
		service.mark({
			id : $scope.selectedId,
			olderThan : $scope.timestamp,
			read : true
		}, function() {
			CategoryService.init(function() {
				$scope.$emit('emitReload');
			});
		});
	});

	$scope.$on('reload', function(event, args) {
		$scope.name = null;
		$scope.entries = [];
		$scope.message = null;
		$scope.errorCount = 0;
		$scope.timestamp = 0;
		$scope.busy = false;
		$scope.hasMore = true;
		$scope.loadMoreEntries();
	});
}]);

module.controller('ManageUsersCtrl', ['$scope', '$state', '$location', 'AdminUsersService', 
function($scope, $state, $location,	AdminUsersService) {
	$scope.users = AdminUsersService.getAll();
	$scope.selection = [];
	$scope.gridOptions = {
		data : 'users',
		selectedItems : $scope.selection,
		multiSelect : false,
		afterSelectionChange : function(item) {
			$state.transitionTo('admin.useredit', {
				_id : item.entity.id
			});
		}
	};

	$scope.addUser = function() {
		$state.transitionTo('admin.useradd');
	};
	$scope.back = function() {
		$location.path('/admin');
	};
}]);

module.controller('ManageUserCtrl', ['$scope', '$state', '$stateParams', '$dialog', 'AdminUsersService',
function($scope, $state, $stateParams,	$dialog, AdminUsersService) {
	$scope.user = $stateParams._id ? AdminUsersService.get({
		id : $stateParams._id
	}) : {
		enabled : true
	};
	$scope.alerts = [];
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	var alertFunction = function(data) {
		$scope.alerts.push({
			msg : data.data,
			type : 'error'
		});
	};

	$scope.cancel = function() {
		$state.transitionTo('admin.userlist');
	};
	$scope.save = function() {
		$scope.alerts.splice(0, $scope.alerts.length);
		AdminUsersService.save($scope.user, function() {
			$state.transitionTo('admin.userlist');
		}, alertFunction);
	};
	$scope.remove = function() {
		var title = 'Delete user';
		var msg = 'Delete user ' + $scope.user.name + ' ?';
		var btns = [ {
			result : 'cancel',
			label : 'Cancel'
		}, {
			result : 'ok',
			label : 'OK',
			cssClass : 'btn-primary'
		} ];

		$dialog.messageBox(title, msg, btns).open().then(function(result) {
			if (result == 'ok') {
				AdminUsersService.remove({
					id : $scope.user.id
				}, function() {
					$state.transitionTo('admin.userlist');
				}, alertFunction);
			}
		});
	};
}]);

module.controller('SettingsCtrl', ['$scope', '$location', 'SettingsService', 'AnalyticsService',
function($scope, $location, SettingsService, AnalyticsService) {
	
	AnalyticsService.track();
	
	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings', function(value) {
		$scope.settings = angular.copy(value);
	});

	$scope.cancel = function() {
		SettingsService.init(function() {
			$location.path('/');
		});
	};
	$scope.save = function() {
		SettingsService.settings = $scope.settings;
		SettingsService.save(function() {
			$location.path('/');
		});
	};
}]);

module.controller('ProfileCtrl', ['$scope', '$location', 'ProfileService', 'AnalyticsService',
function($scope, $location, ProfileService, AnalyticsService) {
	
	AnalyticsService.track();
	
	$scope.user = ProfileService.get();

	$scope.cancel = function() {
		$location.path('/');
	};
	$scope.save = function() {
		if (!$scope.profileForm.$valid) {
			return;
		}
		var o = {
			email : $scope.user.email,
			password : $scope.user.password
		};

		ProfileService.save(o, function() {
			$location.path('/');
		});

	};
}]);

module.controller('ManageSettingsCtrl', ['$scope', '$location', '$state', 'AdminSettingsService',
function($scope, $location, $state,	AdminSettingsService) {

	$scope.settings = AdminSettingsService.get();

	$scope.cancel = function() {
		$location.path('/');
	};
	$scope.save = function() {
		AdminSettingsService.save({}, $scope.settings, function() {
			$location.path('/');
		});
	};

	$scope.toUsers = function() {
		$state.transitionTo('admin.userlist');
	};
}]);