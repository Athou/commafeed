var module = angular.module('commafeed.controllers', []);

module.run(['$rootScope', function($rootScope) {
	$rootScope.$on('emitPreviousEntry', function(event, args) {
		$rootScope.$broadcast('previousEntry', args);
	});
	$rootScope.$on('emitNextEntry', function(event, args) {
		$rootScope.$broadcast('nextEntry', args);
	});
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
	$rootScope.$on('emitFeedSearch', function(event, args) {
		$rootScope.$broadcast('feedSearch');
	});
}]);

module.controller('SubscribeCtrl', ['$scope', 'FeedService', 'CategoryService', 'MobileService',
function($scope, FeedService, CategoryService, MobileService) {

	$scope.opts = {
		backdropFade : true,
		dialogFade : true
	};

	$scope.isOpen = false;
	$scope.isOpenImport = false;
	$scope.sub = {};

	$scope.CategoryService = CategoryService;
	$scope.MobileService = MobileService;
	
	$scope.search = function() {
		$scope.$emit('emitFeedSearch');
	};

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
			$scope.close();
		}, function(data) {
			$scope.state = 'failed';
			$scope.sub.title = 'ERROR: ' + data.data;
		});
	};

	$scope.openImport = function() {
		$scope.isOpenImport = true;
	};

	$scope.closeImport = function() {
		$scope.isOpenImport = false;
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
	'$location', '$state', '$route', 'CategoryService', 'AnalyticsService',
function($scope, $timeout, $stateParams, $window, $location, $state, $route, CategoryService, AnalyticsService) {

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
		AnalyticsService.track();
		CategoryService.refresh(function() {
			$timeout(refreshTree, 30000);
		}, function() {
			$timeout(refreshTree, 30000);
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
	
	var flat = function() {
		var a = [];
		a.push([ 'all', 'category' ]);
		a.push([ 'starred', 'category' ]);
		for ( var i = 1; i < CategoryService.flatCategories.length; i++) {
			var cat = CategoryService.flatCategories[i];
			a.push([ cat.id, 'category' ]);
			
			var feeds = cat.orig.feeds;
			if (feeds) {
				for ( var j = 0; j < feeds.length; j++) {
					a.push([ feeds[j].id, 'feed' ]);
				}
			}
		}
		return a;
	};
	
	var getCurrentIndex = function (id, type, flat) {
		var index = -1;
		for ( var i = 0; i < flat.length; i++) {
			var node = flat[i];
			if (node[0] == id && node[1] == type) {
				index = i;
				break;
			}
		}
		return index;
	};
	
	var openNextNode = function() {
		var f = flat();
		var current = getCurrentIndex($scope.selectedId, $scope.selectedType, f);
		current++;
		if(current < f.length) {
			$state.transitionTo('feeds.view', {
				_type : f[current][1],
				_id : f[current][0]
			});
		}
	};
	
	var openPreviousNode = function() {
		var f = flat();
		var current = getCurrentIndex($scope.selectedId, $scope.selectedType, f);
		current--;
		if(current >= 0) {
			$state.transitionTo('feeds.view', {
				_type : f[current][1],
				_id : f[current][0]
			});
		}
	};
	
	Mousetrap.bind('shift+j', function(e) {
		$scope.$apply(function() {
			openNextNode();
		});
	});
	Mousetrap.bind('shift+n', function(e) {
		$scope.$apply(function() {
			openNextNode();
		});
	});
	
	Mousetrap.bind('shift+p', function(e) {
		$scope.$apply(function() {
			openPreviousNode();
		});
	});
	Mousetrap.bind('shift+k', function(e) {
		$scope.$apply(function() {
			openPreviousNode();
		});
	});

	$scope.$on('mark', function(event, args) {
		mark($scope.CategoryService.subscriptions, args.entry);
	});
}]);

module.controller('FeedDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', 'ProfileService', '$dialog', 
    function($scope, $state, $stateParams, FeedService, CategoryService, ProfileService, $dialog) {
	
	$scope.CategoryService = CategoryService;
	$scope.user = ProfileService.get();
	
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
			position: sub.position,
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

module.controller('CategoryDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', 'ProfileService', '$dialog', 
                                      function($scope, $state, $stateParams, FeedService, CategoryService, ProfileService, $dialog) {
	$scope.CategoryService = CategoryService;
	$scope.user = ProfileService.get();
	
	$scope.isMeta = function() {
		return parseInt($stateParams._id, 10) != $stateParams._id;
	};
	
	$scope.filterCurrent = function(elem) {
		if (!$scope.category)
			return true;
		return elem.id != $scope.category.id;
	}; 
	
	CategoryService.init(function() {
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
					position: cat.orig.position,
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
			position: cat.position,
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
	'$route', '$location', 'SettingsService', 'EntryService', 'ProfileService', 'AnalyticsService', 'ServerService', 'FeedService', 'MobileService',
function($scope, $http, $state, $stateParams, $route, $location,
		SettingsService, EntryService, ProfileService, AnalyticsService, ServerService, FeedService, MobileService) {

	function totalActiveAjaxRequests() {
		return ($http.pendingRequests.length + $.active);
	}

	$scope.session = ProfileService.get();
	$scope.ServerService = ServerService.get();
	$scope.settingsService = SettingsService;
	$scope.MobileService = MobileService;
	
	$scope.loading = true;
	$scope.$watch(totalActiveAjaxRequests, function() {
		$scope.loading = (totalActiveAjaxRequests() !== 0);
	});
	
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
	$scope.$watch('settingsService.settings.viewMode', function(
			newValue, oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			SettingsService.save();
			$scope.$emit('emitReload');
		}
	});
	
	$scope.previousEntry = function() {
		$scope.$emit('emitPreviousEntry');
	};
	$scope.nextEntry = function() {
		$scope.$emit('emitNextEntry');
	};

	$scope.refresh = function() {
		$scope.$emit('emitReload');
		
	};
	
	var markAll = function(olderThan) {
		$scope.$emit('emitMarkAll', {
			type : $stateParams._type,
			id : $stateParams._id,
			olderThan: olderThan,
			read : true
		});
	};
	
	$scope.markAllAsRead = function() {
		markAll();
	};
	
	$scope.markAllDay = function() {
		markAll(new Date().getTime() - 86400000);
	};
	
	$scope.markAllWeek = function() {
		markAll(new Date().getTime() - 604800000);
	};
	
	$scope.markAllTwoWeeks = function() {
		markAll(new Date().getTime() - 1209600000);
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
		settings.readingOrder = settings.readingOrder == 'asc' ? 'desc' : 'asc';
	};
	
	$scope.toAdmin = function() {
		$location.path('admin');
	};
	$scope.toSettings = function() {
		$state.transitionTo('feeds.settings');
	};
	$scope.toProfile = function() {
		$state.transitionTo('feeds.profile');
	};
	$scope.toHelp = function() {
		$state.transitionTo('feeds.help');
	};
	$scope.toDonate = function() {
		AnalyticsService.track('/donate');
		$state.transitionTo('feeds.help');
	};
}]);

module.controller('FeedSearchCtrl', ['$scope', '$state', '$filter', '$timeout', 'CategoryService',
function($scope, $state, $filter, $timeout, CategoryService) {
	$scope.feedSearchModal = false;
	$scope.filter = null;
	$scope.focus = null;
	$scope.CategoryService = CategoryService;
	
	$scope.$watch('filter', function() {
		$timeout(function() {
			if ($scope.filtered){
				$scope.focus = $scope.filtered[0];
			}
		}, 0);
	});
	
	var getCurrentIndex = function() {
		var index = -1;
		
		if(!$scope.focus) {
			return index;
		}
		
		var filtered = $scope.filtered;
		for (var i = 0; i < filtered.length; i++) {
			if ($scope.focus.id == filtered[i].id) {
				index = i;
				break;
			}
		}
		return index;
	};
	
	$scope.focusPrevious = function(e) {
		var index = getCurrentIndex();
		if (index === 0) {
			return;
		} 
		$scope.focus = $scope.filtered[index - 1];
		
		e.stopPropagation();
		e.preventDefault();
	};
	
	$scope.focusNext = function(e) {
		var index = getCurrentIndex();
		if (index == ($scope.filtered.length - 1)) {
			return;
		} 
		$scope.focus = $scope.filtered[index + 1];
		
		e.stopPropagation();
		e.preventDefault();
	};
	
	$scope.openFocused = function() {
		if (!$scope.focus) {
			return;
		}
		$scope.goToFeed($scope.focus.id);
	};

	$scope.goToFeed = function(id) {		
		$scope.close();
		$state.transitionTo('feeds.view', {
			_type : 'feed', 
			_id : id
		});
	};
	
	$scope.open = function() {
		$scope.filter = null;
		$scope.feedSearchModal = true;
	};
	
	$scope.close = function() {
		$scope.feedSearchModal = false;
	};

	Mousetrap.bind('g u', function(e) {
		$scope.$apply(function() {
			$scope.open();
		});
		return false;
	});
	
	$scope.$on('feedSearch', function() {
		$scope.open();
	});

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
	$scope.font_size = 0;

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

	$scope.limit = SettingsService.settings.viewMode == 'title' ? 10 : 5;
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
			if (SettingsService.settings.viewMode == 'title') {
				limit = $window.height() / 33;
				limit = parseInt(limit, 10) + 5;
			} else {
				limit = $window.height() / 97;
				limit = parseInt(limit, 10) + 1;
			}
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
			$scope.hasMore = data.hasMore;
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
				feedId : entry.feedId,
				read : read
			});
		}
	};
	
	$scope.markAll = function(olderThan) {
		var service = $scope.selectedType == 'feed' ? FeedService
				: CategoryService;
		service.mark({
			id : $scope.selectedId,
			olderThan : olderThan || $scope.timestamp,
			read : true
		}, function() {
			CategoryService.refresh(function() {
				$scope.$emit('emitReload');
			});
		});
	};
	
	$scope.star = function(entry, star, event) {
		if (event) {
			event.preventDefault();
			event.stopPropagation();
		}
		if (entry.starred != star) {
			entry.starred = star;
			EntryService.star({
				id : entry.id,
				feedId : entry.feedId,
				starred : star
			});
		}
	};
	
	var getCurrentIndex = function() {
		var index = -1;
		if ($scope.current) {
			for (var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
		}
		return index;
	};

	var getNextEntry = function() {
		var index = getCurrentIndex();
		if (index >= 0) {
			index = index + 1;
			if (index < $scope.entries.length) {
				return $scope.entries[index];
			} 
		} else if ($scope.entries.length > 0) {
			return $scope.entries[0];
		}
		return null;
	};

	var getPreviousEntry = function() {
		var index = getCurrentIndex();
		if (index >= 1) {
			return $scope.entries[index - 1];
		}
		return null;
	};

	var openNextEntry = function(event) {
		var entry = getNextEntry();
		openEntry(entry, event);
	};

	var openPreviousEntry = function(event) {
		var entry = getPreviousEntry();
		openEntry(entry, event);
	};

	var focusNextEntry = function(event) {
		var entry = getNextEntry();
		
		if (event) {
			event.preventDefault();
			event.stopPropagation();
		}
		
		if (entry) {
			$scope.current = entry;
		}
	};

	var focusPreviousEntry = function(event) {
		var entry = getPreviousEntry();
	
		if (event) {
			event.preventDefault();
			event.stopPropagation();
		}
		
		if (entry) {
			$scope.current = entry;
		}
	};

	$scope.isOpen = SettingsService.settings.viewMode == 'expanded';
	
	var openEntry = function(entry, event) {
		
		if (event) {
			event.preventDefault();
			event.stopPropagation();
		}
		
		if (!entry) {
			return;
		}
		
		if ($scope.current != entry || SettingsService.settings.viewMode == 'expanded') {
			$scope.isOpen = true;
		} else {
			$scope.isOpen = !$scope.isOpen;
		}
		if ($scope.isOpen) {
			$scope.mark(entry, true);
		}
		$scope.current = entry;
			
		if (getCurrentIndex() == $scope.entries.length - 1) {
			$scope.loadMoreEntries();	
		}
	};
	
	$scope.entryClicked = function(entry, event) {
		
		if (event && event.which === 3) {
			// right click
			return;
		}
		if (!event || (!event.ctrlKey && event.which != 2)) {
			$scope.navigationMode = 'click';
			openEntry(entry, event);
		} else {
			$scope.mark(entry, true);
		}
	};
	
	$scope.bodyClicked = function(entry, event) {
		if (SettingsService.settings.viewMode == 'expanded' && $scope.current != entry) {
			$scope.entryClicked(entry, event);
		}
	};

	$scope.noop = function(event) {
		if (!event.ctrlKey && event.which != 2) {
			event.preventDefault();
			event.stopPropagation();
		}
	};

	$scope.onScroll = function(entry) {
		$scope.navigationMode = 'scroll';
		if (SettingsService.settings.viewMode == 'expanded') {
			$scope.current = entry;
			if(SettingsService.settings.scrollMarks) {
				$scope.mark(entry, true);
			}
		}
	};
	
	Mousetrap.bind('j', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			openNextEntry(e);
		});
	});
	Mousetrap.bind('n', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			focusNextEntry(e);
		});
	});
	Mousetrap.bind('k', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			openPreviousEntry(e);
		});
	});
	Mousetrap.bind('p', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			focusPreviousEntry(e);
		});
	});
	Mousetrap.bind('o', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			if ($scope.current) {
				openEntry($scope.current, e);
			}
		});
	});
	Mousetrap.bind('enter', function(e) {
		$scope.$apply(function() {
			$scope.navigationMode = 'keyboard';
			if ($scope.current) {
				openEntry($scope.current, e);
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
			$scope.mark($scope.current, true);
			window.open($scope.current.url);
		}
	});
	Mousetrap.bind('b', function(e) {
		if ($scope.current) {
			$scope.mark($scope.current, true);
			
			var url = $scope.current.url;
			var a = document.createElement('a');
			a.href = url;
			var evt = document
					.createEvent('MouseEvents');
			evt.initMouseEvent('click', true, true,
					window, 0, 0, 0, 0, 0, true, false,
					false, true, 0, null);
			a.dispatchEvent(evt);
		}
	});
	Mousetrap.bind('s', function(e) {
		$scope.$apply(function() {
			if ($scope.current) {
				$scope.star($scope.current, !$scope.current.starred);
			}
		});
	});
	Mousetrap.bind('m', function(e) {
		$scope.$apply(function() {
			if ($scope.current) {
				$scope.mark($scope.current, !$scope.current.read);
			}
		});
	});
	Mousetrap.bind('shift+a', function(e) {
		$scope.$apply(function() {
			$scope.markAll();
		});
	});
	
	Mousetrap.bind('+', function(e) {
		$scope.$apply(function() {
			$scope.font_size = Math.min($scope.font_size + 1, 5); 
		});
	});	
	
	Mousetrap.bind('-', function(e) {
		$scope.$apply(function() {
			$scope.font_size = Math.max($scope.font_size - 1, 0); 
		});
	});	
	
	Mousetrap.bind('space', function(e) {
		if (!$scope.current) {
			$scope.$apply(function() {
				$scope.navigationMode = 'keyboard';
				openNextEntry(e);
			});	
		} else if (!$scope.isOpen) {
			$scope.$apply(function() {
				$scope.navigationMode = 'keyboard';
				if ($scope.current) {
					openEntry($scope.current, e);
				}
			});
		} else {
			var docTop = $(window).scrollTop();
			var docBottom = docTop + $(window).height();

			var elem = $('#entry_' + $scope.current.id);
			var elemTop = elem.offset().top;
			var elemBottom = elemTop + elem.height();
			
			var bottomVisible = elemBottom < docBottom;
			if (bottomVisible) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					openNextEntry(e);
				});	
			}
		}
	});
	
	Mousetrap.bind('shift+space', function(e) {
		if (!$scope.current) {
			return;
		} else if (!$scope.isOpen) {
			$scope.$apply(function() {
				$scope.navigationMode = 'keyboard';
				if ($scope.current) {
					openEntry($scope.current, e);
				}
			});
		} else {
			var docTop = $(window).scrollTop();

			var elem = $('#entry_' + $scope.current.id);
			var elemTop = elem.offset().top;
			
			var topVisible = elemTop > docTop;
			if (topVisible) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					openPreviousEntry(e);
				});	
			}
		}
	});
	
	Mousetrap.bind('f', function(e) {
		$('body').toggleClass('full-screen');
	});
	
	Mousetrap.bind('?', function(e) {
		$scope.$apply(function() {
			$scope.shortcutsModal = true;
		});
	});

	$scope.$on('previousEntry', function(event, args) {
		$scope.navigationMode = 'keyboard';
		openPreviousEntry();
	});
	$scope.$on('nextEntry', function(event, args) {
		$scope.navigationMode = 'keyboard';
		openNextEntry();
	});
	$scope.$on('markAll', function(event, args) {
		$scope.markAll(args.olderThan);
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
		
		if ($scope.selectedType == 'feed'){
			FeedService.refresh({
				id : $stateParams._id
			});
		}
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
		showColumnMenu: true,
		showFilter: true,
		
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

module.controller('SettingsCtrl', ['$scope', '$location', 'SettingsService', 'AnalyticsService', 'ServerService',
function($scope, $location, SettingsService, AnalyticsService, ServerService) {
	
	AnalyticsService.track();
	
	$scope.ServerService = ServerService.get();
	
	$scope.themes = ['default','MRACHINI'];
	
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
			window.location.href = window.location.href.substring(0, window.location.href.lastIndexOf('#'));
		});
	};
}]);

module.controller('ProfileCtrl', ['$scope', '$location', '$dialog', 'ProfileService', 'AnalyticsService',
function($scope, $location, $dialog, ProfileService, AnalyticsService) {
	
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
			password : $scope.user.password,
			newApiKey : $scope.newApiKey
		};

		ProfileService.save(o, function() {
			$location.path('/');
		});
	};
	$scope.deleteAccount = function() {
		var title = 'Delete account';
		var msg = 'Delete your acount? There\'s no turning back!';
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
				ProfileService.deleteAccount({});
				window.location.href = 'logout';
			}
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

module.controller('HelpController', [ '$scope', 'CategoryService',
		'AnalyticsService',
function($scope, CategoryService, AnalyticsService) {

	AnalyticsService.track();
	$scope.CategoryService = CategoryService;
	$scope.categoryId = 'all';

} ]);

module.controller('FooterController', [ '$scope', function($scope) {
	
	var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf('#'));
	var hostname = window.location.hostname;
	$scope.subToMeUrl = baseUrl + 'rest/feed/subscribe?url={feed}';
	$scope.subToMeName = hostname.indexOf('www.commafeed.com') !== -1 ? 'CommaFeed' : 'CommaFeed (' + hostname + ')';

}]);