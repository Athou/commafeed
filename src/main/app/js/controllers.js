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
		// args.all
		$rootScope.$broadcast('reload', args || {});
	});
	$rootScope.$on('emitEntrySearch', function(event, args) {
		// args.keywords
		$rootScope.$broadcast('entrySearch', args);
	});
	$rootScope.$on('emitFeedSearch', function(event, args) {
		$rootScope.$broadcast('feedSearch');
	});
}]);

module.controller('SubscribeCtrl', ['$scope', '$location', 'FeedService', 'CategoryService', 'MobileService',
		function($scope, $location, FeedService, CategoryService, MobileService) {

			$scope.sub = {
				categoryId : 'all'
			};

			$scope.CategoryService = CategoryService;
			$scope.MobileService = MobileService;

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
						$scope.stacktrace = null;
					}, function(data) {
						$scope.state = 'failed';
						$scope.sub.title = 'Loading failed. Invalid feed?';
						$scope.stacktrace = data.data;
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
					$location.path('/');
				}, function(data) {
					$scope.state = 'failed';
					$scope.sub.title = 'ERROR: ' + data.data;
				});
			};

			$scope.back = function() {
				$location.path('/');
			};
		}]);

module.controller('NewCategoryCtrl', ['$scope', '$location', 'FeedService', 'CategoryService', 'MobileService',
		function($scope, $location, FeedService, CategoryService, MobileService) {

			$scope.CategoryService = CategoryService;
			$scope.MobileService = MobileService;

			$scope.cat = {
				parentId : 'all'
			};

			$scope.saveCategory = function() {
				CategoryService.add($scope.cat, function() {
					CategoryService.init();
				});
				$location.path('/');
			};

			$scope.back = function() {
				$location.path('/');
			};
		}]);

module.controller('ImportCtrl', ['$scope', '$location', 'FeedService', 'CategoryService', 'MobileService',
		function($scope, $location, FeedService, CategoryService, MobileService) {

			$scope.back = function() {
				$location.path('/');
			};
		}]);

module.controller('CategoryTreeCtrl', [
		'$scope',
		'$timeout',
		'$stateParams',
		'$window',
		'$location',
		'$state',
		'$route',
		'CategoryService',
		'AnalyticsService',
		'EntryService',
		'MobileService',
		function($scope, $timeout, $stateParams, $window, $location, $state, $route, CategoryService, AnalyticsService, EntryService,
				MobileService) {

			$scope.selectedType = $stateParams._type;
			$scope.selectedId = $stateParams._id;

			$scope.EntryService = EntryService;
			$scope.MobileService = MobileService;

			$scope.starred = {
				id : 'starred',
				name : 'Starred'
			};

			$scope.tags = [];
			$scope.$watch('EntryService.tags', function(newValue, oldValue) {
				if (newValue) {
					$scope.tags = [];
					_.each(newValue, function(e) {
						$scope.tags.push({
							id : e,
							name : e,
							isTag : true
						});
					});
				}
			}, true);

			$scope.$on('$stateChangeSuccess', function() {
				$scope.selectedType = $stateParams._type;
				$scope.selectedId = $stateParams._id;
			});

			$scope.resizeCallback = function(event, ui) {
				$('.main-content').css('margin-left', $(ui.element).outerWidth(true) + 'px');
			};

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
					label = '(' + value + ') ' + label;
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

			var getCurrentIndex = function(id, type, flat) {
				var index = -1;
				for (var i = 0; i < flat.length; i++) {
					var node = flat[i];
					if (node[0] == id && node[1] == type) {
						index = i;
						break;
					}
				}
				return index;
			};

			var openNextNode = function() {
				var f = CategoryService.flatAll;
				var current = getCurrentIndex($scope.selectedId, $scope.selectedType, f);
				current++;
				if (current < f.length) {
					$state.transitionTo('feeds.view', {
						_type : f[current][1],
						_id : f[current][0]
					});
				}
			};

			var openPreviousNode = function() {
				var f = CategoryService.flatAll;
				var current = getCurrentIndex($scope.selectedId, $scope.selectedType, f);
				current--;
				if (current >= 0) {
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
				return false;
			});
			Mousetrap.bind('shift+n', function(e) {
				$scope.$apply(function() {
					openNextNode();
				});
				return false;
			});

			Mousetrap.bind('shift+p', function(e) {
				$scope.$apply(function() {
					openPreviousNode();
				});
				return false;
			});
			Mousetrap.bind('shift+k', function(e) {
				$scope.$apply(function() {
					openPreviousNode();
				});
				return false;
			});

			$scope.$on('mark', function(event, args) {
				mark($scope.CategoryService.subscriptions, args.entry);
			});
		}]);

module.controller('FeedDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', 'ProfileService',
		function($scope, $state, $stateParams, FeedService, CategoryService, ProfileService) {

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
					_id : $stateParams._id,
					_type : 'feed'
				});
			};

			$scope.unsubscribe = function() {
				var sub = $scope.sub;
				var data = {
					id : sub.id
				};
				FeedService.unsubscribe(data, function() {
					CategoryService.init();
				});
				$state.transitionTo('feeds.view', {
					_id : 'all',
					_type : 'category'
				});
			};

			$scope.save = function() {
				var sub = $scope.sub;
				$scope.error = null;
				FeedService.modify({
					id : sub.id,
					name : sub.name,
					position : sub.position,
					categoryId : sub.categoryId,
					filter : sub.filter
				}, function() {
					CategoryService.init();
					$state.transitionTo('feeds.view', {
						_id : 'all',
						_type : 'category'
					});
				}, function(e) {
					$scope.error = e.data;
				});
			};
		}]);

module.controller('CategoryDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', 'ProfileService',
		function($scope, $state, $stateParams, FeedService, CategoryService, ProfileService) {
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
							id : cat.id,
							name : cat.orig.name,
							position : cat.orig.position,
							parentId : cat.orig.parentId
						};
						break;
					}
				}
				if (!$scope.category.parentId)
					$scope.category.parentId = 'all';
			});

			$scope.back = function() {
				$state.transitionTo('feeds.view', {
					_id : $stateParams._id,
					_type : 'category'
				});
			};

			$scope.deleteCategory = function() {
				var category = $scope.category;
				CategoryService.remove({
					id : category.id
				}, function() {
					CategoryService.init();
				});
				$state.transitionTo('feeds.view', {
					_id : 'all',
					_type : 'category'
				});
			};

			$scope.save = function() {
				var cat = $scope.category;
				CategoryService.modify({
					id : cat.id,
					name : cat.name,
					position : cat.position,
					parentId : cat.parentId
				}, function() {
					CategoryService.init();
					$state.transitionTo('feeds.view', {
						_id : 'all',
						_type : 'category'
					});
				});
			};
		}]);

module.controller('TagDetailsCtrl', ['$scope', '$state', '$stateParams', 'FeedService', 'CategoryService', 'ProfileService',
		function($scope, $state, $stateParams, FeedService, CategoryService, ProfileService) {
			$scope.CategoryService = CategoryService;
			$scope.user = ProfileService.get();

			$scope.tag = $stateParams._id;

			$scope.back = function() {
				$state.transitionTo('feeds.view', {
					_id : $scope.tag,
					_type : 'tag'
				});
			};
		}]);

module.controller('ToolbarCtrl', [
		'$scope',
		'$state',
		'$stateParams',
		'$route',
		'$location',
		'SettingsService',
		'EntryService',
		'ProfileService',
		'AnalyticsService',
		'ServerService',
		'FeedService',
		'MobileService',
		function($scope, $state, $stateParams, $route, $location, SettingsService, EntryService, ProfileService, AnalyticsService,
				ServerService, FeedService, MobileService) {

			$scope.keywords = $location.search().q;
			$scope.session = ProfileService.get();
			$scope.ServerService = ServerService.get();
			$scope.settingsService = SettingsService;
			$scope.MobileService = MobileService;

			$scope.$watch('settingsService.settings.readingMode', function(newValue, oldValue) {
				if (newValue && oldValue && newValue != oldValue) {
					SettingsService.save();
				}
			});
			$scope.$watch('settingsService.settings.readingOrder', function(newValue, oldValue) {
				if (newValue && oldValue && newValue != oldValue) {
					SettingsService.save();
				}
			});
			$scope.$watch('settingsService.settings.viewMode', function(newValue, oldValue) {
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

			$scope.refreshAll = function() {
				$scope.$emit('emitReload', {
					all : true
				});
			};

			var markAll = function(olderThan) {
				$scope.$emit('emitMarkAll', {
					type : $stateParams._type,
					id : $stateParams._id,
					olderThan : olderThan,
					keywords : $location.search().q,
					read : true
				});
			};

			$scope.markAllAsRead = function() {
				markAll();
			};

			$scope.markAll12Hours = function() {
				markAll(new Date().getTime() - 43200000);
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

			$scope.search = function() {
				var keywords = this.keywords;
				$location.search('q', keywords);
				$scope.$emit('emitEntrySearch', {
					keywords : keywords
				});
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
					if ($scope.filtered) {
						$scope.focus = $scope.filtered[0];
					}
				}, 0);
			});

			var getCurrentIndex = function() {
				var index = -1;

				if (!$scope.focus) {
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

			Mousetrap.bind('g a', function(e) {
				$scope.$apply(function() {
					$state.transitionTo('feeds.view', {
						_type : 'category',
						_id : 'all'
					});
				});
				return false;
			});

			Mousetrap.bind('g s', function(e) {
				$scope.$apply(function() {
					$state.transitionTo('feeds.view', {
						_type : 'category',
						_id : 'starred'
					});
				});
				return false;
			});

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

module.controller('FeedListCtrl', [
		'$scope',
		'$stateParams',
		'$http',
		'$route',
		'$state',
		'$window',
		'$timeout',
		'$location',
		'EntryService',
		'SettingsService',
		'FeedService',
		'CategoryService',
		'AnalyticsService',
		'MobileService',
		function($scope, $stateParams, $http, $route, $state, $window, $timeout, $location, EntryService, SettingsService, FeedService,
				CategoryService, AnalyticsService, MobileService) {

			$window = angular.element($window);
			AnalyticsService.track();

			$scope.keywords = $location.search().q;

			$scope.selectedType = $stateParams._type;
			$scope.selectedId = $stateParams._id;

			$scope.name = null;
			$scope.message = null;
			$scope.errorCount = 0;
			$scope.timestamp = 0;
			$scope.entries = [];
			$scope.ignored_read_status = false;
			$scope.font_size = 0;

			$scope.settingsService = SettingsService;
			$scope.MobileService = MobileService;
			$scope.$watch('settingsService.settings.readingMode', function(newValue, oldValue) {
				if (newValue && oldValue && newValue != oldValue) {
					$scope.$emit('emitReload');
				}
			});
			$scope.$watch('settingsService.settings.readingOrder', function(newValue, oldValue) {
				if (newValue && oldValue && newValue != oldValue) {
					$scope.$emit('emitReload');
				}
			});

			$scope.$watch('settingsService.settings.readingOrder', function(newValue, oldValue) {
				if (newValue && oldValue && newValue != oldValue) {
					$scope.$emit('emitReload');
				}
			});
			$scope.$watch('settingsService.settings.theme', function(newValue, oldValue) {
				if (newValue) {
					angular.element('html').attr('id', 'theme-' + newValue);
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

				var read_shown = SettingsService.settings.readingMode === 'all' || $scope.ignored_read_status;
				var offset = read_shown ? $scope.entries.length : _.where($scope.entries, {
					read : false
				}).length;
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
					for (var i = 0; i < data.entries.length; i++) {
						var entry = data.entries[i];
						if (!_.some($scope.entries, {
							id : entry.id
						})) {
							$scope.entries.push(entry);
						}
					}
					$scope.name = data.name;
					$scope.message = data.message;
					$scope.errorCount = data.errorCount;
					$scope.timestamp = data.timestamp;
					$scope.busy = false;
					$scope.hasMore = data.hasMore;
					$scope.feedLink = data.feedLink;
					$scope.ignored_read_status = data.ignoredReadStatus;
				};

				var data = {
					id : $scope.selectedId,
					readType : $scope.keywords ? 'all' : $scope.settingsService.settings.readingMode,
					order : $scope.settingsService.settings.readingOrder,
					offset : offset,
					limit : limit,
					keywords : $scope.keywords
				};
				if ($scope.selectedType == 'feed') {
					FeedService.entries(data, callback);
				} else if ($scope.selectedType == 'category') {
					CategoryService.entries(data, callback);
				} else if ($scope.selectedType == 'tag') {
					data.tag = data.id;
					data.id = 'all';
					CategoryService.entries(data, callback);
				}
			};

			var watch_scrolling = true;
			var watch_current = true;

			$scope.$watch('current', function(newValue, oldValue) {
				if (!watch_current) {
					return;
				}
				if (newValue && newValue !== oldValue) {
					var force = $scope.navigationMode == 'keyboard';

					// timeout here to execute after dom update
					$timeout(function() {
						var docTop = $(window).scrollTop();
						var docBottom = docTop + $(window).height();

						var elem = $('#entry_' + newValue.id);
						var elemTop = elem.offset().top;
						var elemBottom = elemTop + elem.height();

						if (!force && (elemTop > docTop) && (elemBottom < docBottom)) {
							// element is entirely visible
							return;
						} else {
							var scrollTop = elemTop - $('#toolbar').outerHeight();
							var speed = SettingsService.settings.scrollSpeed;
							watch_scrolling = false;
							$('html, body').animate({
								scrollTop : scrollTop
							}, speed, 'swing', function() {
								watch_scrolling = true;
							});
						}
					});
				}
			});

			var scrollHandler = function() {
				if (!watch_scrolling || _.size($scope.entries) === 0) {
					return;
				}

				$scope.navigationMode = 'scroll';
				if (SettingsService.settings.viewMode == 'expanded') {
					var w = $(window);
					var docTop = w.scrollTop();

					var current = null;
					for (var i = 0; i < $scope.entries.length; i++) {
						var entry = $scope.entries[i];
						var e = $('#entry_' + entry.id);
						if (e.offset().top + e.height() > docTop + $('#toolbar').outerHeight()) {
							current = entry;
							break;
						}
					}

					var previous = $scope.current;
					$scope.current = current;
					if (previous != current) {
						if (SettingsService.settings.scrollMarks) {
							$scope.mark($scope.current, true);
						}
						watch_current = false;
						$scope.$apply();
						watch_current = true;
					}
				}
			};
			var scrollListener = _.throttle(scrollHandler, 200);
			$window.on('scroll', scrollListener);
			$scope.$on('$destroy', function() {
				return $window.off('scroll', scrollListener);
			});

			$scope.goToFeed = function(id) {
				$state.transitionTo('feeds.view', {
					_type : 'feed',
					_id : id
				});
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

			$scope.markAll = function(olderThan) {
				var service = $scope.selectedType == 'feed' ? FeedService : CategoryService;
				service.mark({
					id : $scope.selectedId,
					olderThan : olderThan || $scope.timestamp,
					keywords : $location.search().q,
					read : true
				}, function() {
					CategoryService.refresh(function() {
						$scope.$emit('emitReload');
					});
				});
			};

			$scope.markUpTo = function(entry) {
				var entries = [];
				for (var i = 0; i < $scope.entries.length; i++) {
					var e = $scope.entries[i];
					if (!e.read) {
						entries.push({
							id : e.id,
							read : true
						});
						e.read = true;
					}
					if (e == entry) {
						break;
					}
				}
				EntryService.markMultiple({
					requests : entries
				}, function() {
					CategoryService.refresh();
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

			Mousetrap.bind('j', function(e) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					openNextEntry(e);
				});
				return false;
			});
			Mousetrap.bind('n', function(e) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					focusNextEntry(e);
				});
				return false;
			});
			Mousetrap.bind('k', function(e) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					openPreviousEntry(e);
				});
				return false;
			});
			Mousetrap.bind('p', function(e) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					focusPreviousEntry(e);
				});
				return false;
			});
			Mousetrap.bind('o', function(e) {
				$scope.$apply(function() {
					$scope.navigationMode = 'keyboard';
					if ($scope.current) {
						openEntry($scope.current, e);
					}
				});
				return false;
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
				return false;
			});
			Mousetrap.bind('v', function(e) {
				if ($scope.current) {
					$scope.mark($scope.current, true);
					window.open($scope.current.url);
				}
				return false;
			});
			Mousetrap.bind('b', function(e) {
				if ($scope.current) {
					$scope.mark($scope.current, true);

					var url = $scope.current.url;
					var a = document.createElement('a');
					a.href = url;
					var evt = document.createEvent('MouseEvents');
					evt.initMouseEvent('click', true, true, window, 0, 0, 0, 0, 0, true, false, false, true, 0, null);
					a.dispatchEvent(evt);
				}
				return false;
			});
			Mousetrap.bind('s', function(e) {
				$scope.$apply(function() {
					if ($scope.current) {
						$scope.star($scope.current, !$scope.current.starred);
					}
				});
				return false;
			});
			Mousetrap.bind('m', function(e) {
				$scope.$apply(function() {
					if ($scope.current) {
						$scope.mark($scope.current, !$scope.current.read);
					}
				});
				return false;
			});
			Mousetrap.bind('shift+a', function(e) {
				$scope.$apply(function() {
					$scope.markAll();
				});
				return false;
			});

			Mousetrap.bind('+', function(e) {
				$scope.$apply(function() {
					$scope.font_size = Math.min($scope.font_size + 1, 5);
				});
				return false;
			});

			Mousetrap.bind('-', function(e) {
				$scope.$apply(function() {
					$scope.font_size = Math.max($scope.font_size - 1, 0);
				});
				return false;
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
				$('.main-content').css('margin-left', '');
				return false;
			});

			Mousetrap.bind('?', function(e) {
				$scope.$apply(function() {
					$scope.shortcutsModal = true;
				});
				return false;
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

			var reload = function(all, keywords) {
				$scope.keywords = keywords;
				$location.search('q', keywords);
				delete $scope.current;
				$scope.name = null;
				$scope.entries = [];
				$scope.message = null;
				$scope.errorCount = 0;
				$scope.timestamp = 0;
				$scope.busy = false;
				$scope.hasMore = true;
				$scope.loadMoreEntries();

				if (all) {
					FeedService.refreshAll();
				} else if ($scope.selectedType == 'feed') {
					FeedService.refresh({
						id : $stateParams._id
					});
				}
			};

			$scope.$on('entrySearch', function(event, args) {
				reload(null, args.keywords);
			});

			$scope.$on('reload', function(event, args) {
				reload(args.all, null);
			});
		}]);

module.controller('ManageUsersCtrl', ['$scope', '$state', '$location', 'AdminUsersService',
		function($scope, $state, $location, AdminUsersService) {
			$scope.users = AdminUsersService.getAll();
			$scope.selection = [];
			$scope.gridOptions = {
				data : 'users',
				selectedItems : $scope.selection,
				multiSelect : false,
				showColumnMenu : true,
				showFilter : true,
				columnDefs : [{
					field : 'id',
					displayName : 'ID'
				}, {
					field : 'name',
					displayName : 'Name'
				}, {
					field : 'email',
					cellClass : 'E-Mail'
				}, {
					field : 'created',
					cellClass : 'Created',
					cellFilter : 'entryDate'
				}, {
					field : 'lastLogin',
					cellClass : 'Last login',
					cellFilter : 'entryDate'
				}, {
					field : 'admin',
					cellClass : 'Admin'
				}, {
					field : 'enabled',
					cellClass : 'Enabled'
				}],

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

module.controller('ManageUserCtrl', ['$scope', '$state', '$stateParams', 'AdminUsersService',
		function($scope, $state, $stateParams, AdminUsersService) {
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
				AdminUsersService.remove({
					id : $scope.user.id
				}, function() {
					$state.transitionTo('admin.userlist');
				}, alertFunction);
			};
		}]);

module.controller('SettingsCtrl', ['$scope', '$location', 'SettingsService', 'AnalyticsService', 'LangService',
		function($scope, $location, SettingsService, AnalyticsService, LangService) {

			AnalyticsService.track();

			$scope.langs = LangService.langs;

			$scope.themes = ['default', 'bootstrap', 'dark', 'ebraminio', 'MRACHINI', 'svetla', 'third'];

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
					password : $scope.user.password,
					newApiKey : $scope.newApiKey
				};

				ProfileService.save(o, function() {
					$location.path('/');
				});
			};
			$scope.deleteAccount = function() {
				ProfileService.deleteAccount({});
				window.location.href = 'logout';
			};
		}]);

module.controller('ManageSettingsCtrl', ['$scope', '$location', '$state', 'AdminSettingsService',
		function($scope, $location, $state, AdminSettingsService) {

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
			$scope.toMetrics = function() {
				$state.transitionTo('admin.metrics');
			};
		}]);

module.controller('HelpController', ['$scope', 'CategoryService', 'AnalyticsService', 'ServerService',
		function($scope, CategoryService, AnalyticsService, ServerService) {

			AnalyticsService.track();
			$scope.CategoryService = CategoryService;
			$scope.infos = ServerService.get();
			$scope.categoryId = 'all';
			$scope.order = 'desc';
			$scope.baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf('#'));

		}]);

module.controller('FooterController', ['$scope', '$sce', function($scope, $sce) {
	var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf('#'));
	var hostname = window.location.hostname;
	var url = baseUrl + 'rest/feed/subscribe?url={feed}';
	var name = hostname.indexOf('www.commafeed.com') !== -1 ? 'CommaFeed' : 'CommaFeed (' + hostname + ')';
	var subToMeUrl = 'https://www.subtome.com/register-no-ui.html?name=' + name + '&url=' + url;
	$scope.subToMeUrl = $sce.trustAsResourceUrl(subToMeUrl);

}]);

module.controller('MetricsCtrl', ['$scope', 'AdminMetricsService', function($scope, AdminMetricsService) {
	$scope.metrics = AdminMetricsService.get();
}]);

module.controller('LoginCtrl', ['$scope', '$location', '$timeout', 'SessionService', 'ServerService',
		function($scope, $location, $timeout, SessionService, ServerService) {
			$scope.model = {};
			$scope.recovery_model = {};
			$scope.recovery = false;
			$scope.recovery_enabled = false;

			ServerService.get(function(data) {
				$scope.recovery_enabled = data.smtpEnabled;
			});

			var login = function(model) {
				var success = function(data) {
					window.location.href = window.location.href.substring(0, window.location.href.lastIndexOf('#'));
				};
				var error = function(data) {
					$scope.message = data.data;
				};
				SessionService.login({
					name : model.name,
					password : model.password
				}, success, error);
			}
			$scope.demoLogin = function() {
				login({
					name : 'demo',
					password : 'demo'
				});
			};

			$scope.login = function() {
				// autofilled fields do not trigger model update, do it manually
				$('input[ng-model]').trigger('input');
				login($scope.model);
			};

			$scope.toggleRecovery = function() {
				$scope.recovery = !$scope.recovery;
			};

			var recovery_success = function(data) {
				$scope.recovery_message = "Email has ben sent. Check your inbox.";
			};
			var recovery_error = function(data) {
				$scope.recovery_message = data.data;
			};
			$scope.recover = function() {
				SessionService.passwordReset({
					email : $scope.recovery_model.email
				}, recovery_success, recovery_error);
			}
		}]);

module.controller('RegisterCtrl', ['$scope', '$location', 'SessionService', 'ServerService',
		function($scope, $location, SessionService, ServerService) {
			$scope.ServerService = ServerService.get();
			$scope.model = {};

			$scope.register = function() {
				var success = function(data) {
					window.location.href = window.location.href.substring(0, window.location.href.lastIndexOf('#'));
				};
				var error = function(data) {
					$scope.messages = data.data.errors;
				};
				SessionService.register($scope.model, success, error);
			}
		}]);
