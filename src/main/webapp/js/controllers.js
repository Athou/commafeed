var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
	$rootScope.$on('emitReload', function(event, args) {
		$rootScope.$broadcast('reload');
	});
});

module.controller('SubscribeCtrl', function($scope, SubscriptionService) {

	$scope.opts = {
		backdropFade : true,
		dialogFade : true
	};

	$scope.isOpen = false;
	$scope.isOpenImport = false;
	$scope.sub = {};

	$scope.SubscriptionService = SubscriptionService;

	$scope.open = function() {
		$scope.sub = {};
		$scope.isOpen = true;
	};

	$scope.close = function() {
		$scope.isOpen = false;
	};

	$scope.urlChanged = function() {
		if ($scope.sub.url && !$scope.sub.title) {
			$scope.sub.title = 'Loading...';
			SubscriptionService.fetch({
				url : $scope.sub.url
			}, function(data) {
				$scope.sub.title = data.title;
			});
		}
	};

	$scope.save = function() {
		SubscriptionService.subscribe($scope.sub);
		$scope.close();
	};

	$scope.openImport = function() {
		$scope.isOpenImport = true;
	};

	$scope.closeImport = function() {
		$scope.isOpenImport = false;
	};

	$scope.uploadComplete = function(contents, completed) {
		SubscriptionService.init();
		$scope.closeImport();
	};

	$scope.cat = {};

	$scope.openCategory = function() {
		$scope.isOpenCategory = true;
		$scope.cat = {};
	};

	$scope.closeCategory = function() {
		$scope.isOpenCategory = false;
	};

	$scope.saveCategory = function() {
		SubscriptionService.addCategory($scope.cat);
		$scope.closeCategory();
	};
});

module.controller('CategoryTreeCtrl', function($scope, $timeout, $stateParams,
		$location, $state, $route, SubscriptionService) {

	$scope.$on('$stateChangeSuccess', function() {
		$scope.selectedType = $stateParams._type;
		$scope.selectedId = $stateParams._id;
	});

	$timeout(function refreshTree() {
		SubscriptionService.init(function() {
			$timeout(refreshTree, 30000);
		});
	}, 30000);

	$scope.SubscriptionService = SubscriptionService;

	var unreadCount = function(category) {
		var count = 0;
		var i;
		if (category.children) {
			for (i = 0; i < category.children.length; i++) {
				count = count + unreadCount(category.children[i]);
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

	$scope.formatCategoryName = function(category) {
		var count = unreadCount(category);
		var label = category.name;
		if (count > 0) {
			label = label + ' (' + count + ')';
		}
		return label;
	};

	$scope.formatFeedName = function(feed) {
		var label = feed.name;
		if (feed.unread > 0) {
			label = label + ' (' + feed.unread + ')';
		}
		return label;
	};

	$scope.feedClicked = function(id) {
		if ($scope.selectedType == 'feed' && id == $scope.selectedId) {
			$scope.$emit('emitReload');
		} else {
			$state.transitionTo('feeds.view', {
				_type : 'feed',
				_id : id
			});
		}
	};

	$scope.categoryClicked = function(id) {
		if ($scope.selectedType == 'category' && id == $scope.selectedId) {
			$scope.$emit('emitReload');
		} else {
			$state.transitionTo('feeds.view', {
				_type : 'category',
				_id : id
			});
		}
	};

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
		mark($scope.SubscriptionService.subscriptions, args.entry);
	});
});

module.controller('ToolbarCtrl', function($scope, $http, $state, $stateParams,
		$route, $location, SettingsService, EntryService, SubscriptionService,
		SessionService) {

	function totalActiveAjaxRequests() {
		return ($http.pendingRequests.length + $.active);
	}

	$scope.session = SessionService.get();

	$scope.loading = true;
	$scope.$watch(totalActiveAjaxRequests, function() {
		$scope.loading = (totalActiveAjaxRequests() !== 0);
	});

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(newValue,
			oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			SettingsService.save();
		}
	});
	$scope.refresh = function() {
		$scope.$emit('emitReload');
	};
	$scope.markAllAsRead = function() {
		EntryService.mark({
			type : $stateParams._type,
			id : $stateParams._id,
			read : true
		}, function() {
			SubscriptionService.init(function() {
				$scope.$emit('emitReload');
			});
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

	$scope.toAdmin = function() {
		$location.path('admin');
	};
	$scope.toSettings = function() {
		$location.path('settings');
	};
});

module.controller('FeedListCtrl', function($scope, $stateParams, $http, $route,
		$window, EntryService, SettingsService) {

	$scope.selectedType = $stateParams._type;
	$scope.selectedId = $stateParams._id;
	$scope.keywords = $stateParams._keywords;

	$scope.name = null;
	$scope.message = null;
	$scope.entries = [];

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(newValue,
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
			$scope.busy = false;
			$scope.hasMore = data.entries.length == limit;
		};
		if (!$scope.keywords) {
			EntryService.get({
				type : $scope.selectedType,
				id : $scope.selectedId,
				readType : $scope.settingsService.settings.readingMode,
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
				type : 'entry',
				id : entry.id,
				read : read
			});
		}
	};

	$scope.isOpen = false;
	$scope.entryClicked = function(entry, event) {
		$scope.mark(entry, true);
		if (!event.ctrlKey && event.which != 2) {
			if ($scope.current != entry) {
				$scope.isOpen = true;
			} else {
				$scope.isOpen = !$scope.isOpen;
			}
			$scope.current = entry;
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

	Mousetrap.bind('space', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		});
	});
	Mousetrap.bind('j', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		});
	});

	Mousetrap.bind('shift+space', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		});
	});
	Mousetrap.bind('k', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		});
	});

	$scope.$on('reload', function(event, args) {
		$scope.name = null;
		$scope.entries = [];
		$scope.busy = false;
		$scope.hasMore = true;
		$scope.loadMoreEntries();
	});
});

module.controller('ManageUsersCtrl', function($scope, $state, $location,
		AdminUsersService) {
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
		$location.path('/');
	};
});

module.controller('ManageUserCtrl', function($scope, $state, $stateParams,
		$dialog, AdminUsersService) {
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
});

module.controller('SettingsCtrl', function($scope, $location, SettingsService) {
	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings', function(value) {
		$scope.settings = angular.copy(value);
	});
	$scope.codeMirrorConfig = {
		mode : 'css',
		lineNumbers : true
	};
	$scope.cancel = function() {
		SettingsService.init(function() {
			$location.path('/');
		});
	};
	$scope.save = function() {
		SettingsService.settings = $scope.settings;
		SettingsService.save(function() {
			window.location.href = window.location.href.substring(0,
					window.location.href.lastIndexOf('#'));
		});
	};
});