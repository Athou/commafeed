var module = angular.module('commafeed.directives', []);

module.directive('focus', ['$timeout', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			scope.$watch(attrs.focus, function(value) {
				if (!value)
					return;
				$timeout(function() {
					$(element).focus();
				});
			});
		}
	};
}]);

module.directive('confirmClick', [function() {
	return {
		priority : -1,
		restrict : 'A',
		link : function(scope, element, attrs) {
			element.bind('click', function(e) {
				var message = scope.$eval(attrs.confirmClick);
				if (message && !confirm(message)) {
					e.stopImmediatePropagation();
					e.preventDefault();
				}
			});
		}
	};
}]);

/**
 * Open a popup window pointing to the url in the href attribute
 */
module.directive('popup', function() {
	var opts = 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=800';
	return {
		link : function(scope, elm, attrs) {
			elm.bind('click', function(event) {
				window.open(this.href, '', opts);
				event.preventDefault();
			});
		}
	};
});

/**
 * entry tag handling
 */
module.directive('tags', function() {
	return {
		restrict : 'E',
		scope : {
			entry : '='
		},
		replace : true,
		templateUrl : 'templates/_tags.html',
		controller : ['$scope', 'EntryService', function($scope, EntryService) {
			$scope.select2Options = {
				'multiple' : true,
				'simple_tags' : true,
				'maximumInputLength' : 40,
				tags : EntryService.tags
			};

			$scope.$watch('entry.tags', function(newValue, oldValue) {
				if (oldValue && newValue != oldValue) {
					var data = {
						entryId : $scope.entry.id,
						tags : []
					};
					if (newValue) {
						data.tags = newValue;
					}
					EntryService.tag(data);
				}
			}, true);
		}]
	};
});

/**
 * Reusable favicon component
 */
module.directive('favicon', function() {
	var tpl = '<img ng-src="{{url}}" class="favicon"></img>';
	return {
		restrict : 'E',
		scope : {
			url : '='
		},
		replace : true,
		template : tpl
	};
});

/**
 * Support for the blur event
 */
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

/**
 * Prevent mousewheel scrolling from propagating to the parent when scrollbar
 * reaches top or bottom
 */
module.directive('mousewheelScrolling', function() {
	return {
		restrict : 'A',
		link : function(scope, elem, attr) {
			elem.bind('mousewheel', function(e, d) {
				var t = $(this);
				if (d > 0 && t.scrollTop() === 0) {
					e.preventDefault();
				} else {
					if (d < 0 && (t.scrollTop() == t.get(0).scrollHeight - t.innerHeight())) {
						e.preventDefault();
					}
				}
			});
		}
	};
});

/**
 * Needed to use recursive directives. Wrap a recursive element with a
 * <recursive> tag
 */
module.directive('recursive', ['$compile', function($compile) {
	return {
		restrict : 'E',
		priority : 100000,
		compile : function(tElement, tAttr) {
			var contents = tElement.contents().remove();
			var compiledContents = null;
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
}]);

/**
 * Reusable category component
 */
module.directive('category', [function() {
	return {
		scope : {
			node : '=',
			level : '=',
			selectedType : '=',
			selectedId : '=',
			showLabel : '=',
			showChildren : '=',
			unreadCount : '&',
			tag : '='
		},
		restrict : 'E',
		replace : true,
		templateUrl : 'templates/_category.html',
		controller : ['$scope', '$state', 'FeedService', 'CategoryService', 'SettingsService', 'MobileService',
				function($scope, $state, FeedService, CategoryService, SettingsService, MobileService) {
					$scope.settingsService = SettingsService;

					$scope.getClass = function(level) {
						if ($scope.showLabel) {
							return 'indent' + level;
						}
					};

					$scope.categoryLabel = function(category) {
						return $scope.showLabel !== true ? $scope.showLabel : category.name;
					};

					$scope.categoryCountLabel = function(category) {
						var count = $scope.unreadCount({
							category : category
						});
						var label = '';
						if (count > 0) {
							label += count;
						}
						return label;
					};

					$scope.feedCountLabel = function(feed) {
						var label = '';
						if (feed.unread > 0) {
							label += feed.unread; 
						}
						return label;
					};

					$scope.feedClicked = function(id, event) {
						// Could be called by a middle click
						if (!event || (!event.ctrlKey && event.which == 1)) {
							MobileService.toggleLeftMenu();
							if ($scope.selectedType == 'feed' && id == $scope.selectedId) {
								$scope.$emit('emitReload');
							} else {
								$state.transitionTo('feeds.view', {
									_type : 'feed',
									_id : id
								});
							}

							if (event) {
								event.preventDefault();
								event.stopPropagation();
							}
						}
					};

					$scope.categoryClicked = function(id, isTag) {
						MobileService.toggleLeftMenu();
						var type = isTag ? 'tag' : 'category';
						if ($scope.selectedType == type && id == $scope.selectedId) {
							$scope.$emit('emitReload');
						} else {
							$state.transitionTo('feeds.view', {
								_type : type,
								_id : id
							});
						}
					};

					$scope.showFeedDetails = function(feed) {
						$state.transitionTo('feeds.feed_details', {
							_id : feed.id
						});
					};

					$scope.showCategoryDetails = function(id, isTag) {
						if (isTag) {
							$state.transitionTo('feeds.tag_details', {
								_id : id
							});
						} else {
							$state.transitionTo('feeds.category_details', {
								_id : id
							});
						}
					};

					$scope.toggleCategory = function(category, event) {
						event.preventDefault();
						event.stopPropagation();
						category.expanded = !category.expanded;
						if (category.id == 'all') {
							return;
						}
						CategoryService.collapse({
							id : category.id,
							collapse : !category.expanded
						});
					};
				}]
	};
}]);

module.directive('draggable', function() {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			element.draggable({
				revert : 'invalid',
				helper : 'clone',
				distance : 10,
				axis : 'y'
			}).data('source', scope.$eval(attrs.draggable));
		}
	};
});

module.directive('droppable', ['CategoryService', 'FeedService', function(CategoryService, FeedService) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			element.droppable({
				tolerance : 'pointer',
				over : function(event, ui) {

				},
				drop : function(event, ui) {
					var draggable = angular.element(ui.draggable);

					var source = draggable.data('source');
					var target = scope.$eval(attrs.droppable);

					if (angular.equals(source, target)) {
						return;
					}

					var data = {
						id : source.id,
						name : source.name,
						filter : source.filter
					};

					if (source.children) {
						// source is a category

					} else {
						// source is a feed

						if (target.children) {
							// target is a category
							data.categoryId = target.id;
							data.position = 0;
						} else {
							// target is a feed
							data.categoryId = target.categoryId;
							data.position = target.position;
						}

						FeedService.modify(data, function() {
							CategoryService.init();
						});
					}
					scope.$apply();
				}
			});
		}
	};
}]);

module.directive('metricMeter', function() {
	return {
		scope : {
			metric : '=',
			label : '='
		},
		restrict : 'E',
		templateUrl : 'templates/_metrics.meter.html'
	};
});

module.directive('metricGauge', function() {
	return {
		scope : {
			metric : '=',
			label : '='
		},
		restrict : 'E',
		templateUrl : 'templates/_metrics.gauge.html'
	};
});

module.directive('metricTimer', function() {
	return {
		scope : {
			metric : '=',
			label : '='
		},
		restrict : 'E',
		templateUrl : 'templates/_metrics.timer.html'
	};
});
