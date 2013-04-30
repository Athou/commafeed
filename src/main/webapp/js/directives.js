var module = angular.module('commafeed.directives', []);

module.directive('favicon', function() {
	return {
		restrict : 'E',
		scope : {
			url : '='
		},
		replace : true,
		template : '<img ng-src="{{iconUrl()}}" class="favicon" onError="this.src=\'images/default_favicon.gif\'"></img>',
		controller : ['$scope', function($scope) {
			$scope.iconUrl = function() {
				var url = $scope.url;

				var current = window.location.href;
				var baseUrl = current.substring(0, current.lastIndexOf('#'));
				var defaultIcon = baseUrl + 'images/default_favicon.gif';
				if (!url) {
					return defaultIcon;
				}

				var index = Math.max(url.length, url.lastIndexOf('?'));
				var iconUrl = 'http://g.etfv.co/';
				iconUrl += encodeURIComponent(url.substring(0, index));
				iconUrl += '?defaulticon=none';
				return iconUrl;
			};
		}]
	};
});

module.directive('ngBlur', function() {
	return {
		restrict : 'A',
		link : function(scope, elm, attrs) {
			elm.bind('blur', function() {
				scope.$apply(attrs.ngBlur);
			});
		}
	};
});

module.directive('scrollTo', [ '$timeout', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			scope.$watch(attrs.scrollTo, function(value) {
				if (!value)
					return;
				$timeout(function() {
					var docTop = $(window).scrollTop();
					var docBottom = docTop + $(window).height();

					var elemTop = $(element).offset().top;
					var elemBottom = elemTop + $(element).height();

					if ((elemTop > docTop) && (elemBottom < docBottom)) {
						// element is entirely visible
						return;
					} else {
						var offset = parseInt(attrs.scrollToOffset, 10);
						var scrollTop = $(element).offset().top + offset;
						$('html, body').animate({
							scrollTop : scrollTop
						}, 0);
					}
				});
			});
		}
	};
} ]);

module.directive('recursive', [ '$compile', function($compile) {
	return {
		restrict : 'E',
		priority : 100000,
		compile : function(tElement, tAttr) {
			var contents = tElement.contents().remove();
			var compiledContents;
			return function(scope, iElement, iAttr) {
				if (!compiledContents) {
					compiledContents = $compile(contents);
				}
				iElement.append(compiledContents(scope, function(clone) {
					return clone;
				}));
			};
		}
	};
} ]);

module.directive('category', [ function() {
	return {
		scope : {
			node : '=',
			selectedType : '=',
			selectedId : '=',
			showLabel : '=',
			showChildren : '=',
			unreadCount : '&'
		},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/category.html',
		controller : [
				'$scope',
				'$state',
				'$dialog',
				'FeedService',
				'CategoryService',
				'SettingsService',
				function($scope, $state, $dialog, FeedService, CategoryService,
						SettingsService) {
					$scope.settingsService = SettingsService;

					$scope.formatCategoryName = function(category) {
						var count = $scope.unreadCount({
							category : category
						});
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
						if ($scope.selectedType == 'feed'
								&& id == $scope.selectedId) {
							$scope.$emit('emitReload');
						} else {
							$state.transitionTo('feeds.view', {
								_type : 'feed',
								_id : id
							});
						}
					};

					$scope.categoryClicked = function(id) {
						if ($scope.selectedType == 'category'
								&& id == $scope.selectedId) {
							$scope.$emit('emitReload');
						} else {
							$state.transitionTo('feeds.view', {
								_type : 'category',
								_id : id
							});
						}
					};
					
					$scope.showFeedDetails = function(feed) {
						$state.transitionTo('feeds.feed_details', {
							_id: feed.id
						});
					};
					
					$scope.showCategoryDetails = function(category) {
						$state.transitionTo('feeds.category_details', {
							_id: category.id
						});
					};

					$scope.toggleCategory = function(category) {
						category.expanded = !category.expanded;
						if (category.id == 'all') {
							return;
						}
						CategoryService.collapse({
							id : category.id,
							collapse : !category.expanded
						});
					};
				} ]
	};
} ]);

module.directive('spinner', function() {
	return {
		scope : {
			shown : '='
		},
		restrict : 'A',
		link : function($scope, element) {
			element.addClass('spinner');
			var opts = {
				lines : 11, // The number of lines to draw
				length : 5, // The length of each line
				width : 3, // The line thickness
				radius : 8, // The radius of the inner circle
				corners : 1, // Corner roundness (0..1)
				rotate : 0, // The rotation offset
				color : '#000', // #rgb or #rrggbb
				speed : 1.3, // Rounds per second
				trail : 60, // Afterglow percentage
				shadow : false, // Whether to render a shadow
				hwaccel : true, // Whether to use hardware acceleration
				zIndex : 2e9, // The z-index (defaults to 2000000000)
				top : 'auto', // Top position relative to parent in px
				left : 'auto' // Left position relative to parent in px
			};
			var spinner = new Spinner(opts);
			$scope.$watch('shown', function(shown) {
				if (shown) {
					spinner.spin();
					element.append(spinner.el);
				} else {
					spinner.stop();
				}
			});
		}
	};
});