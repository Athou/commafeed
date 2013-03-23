var module = angular.module('commafeed.directives', []);

module.directive('category', function($compile) {
	return {
		scope: {
			node: '=',
			selectedType: '=',
			selectedId: '=',
			feedClick: '&',
			categoryClick: '&',
			formatCategoryName: '&',
			formatFeedName: '&'
		},
		restrict : 'E',
		replace: true,
		templateUrl: 'directives/category.html',
		link: function(scope, element) {
            var ul = element.find('ul');
            ul.prepend('<category ng-repeat="child in node.children" node="child" feed-click="feedClick({id:id})" \
            		category-click="categoryClick({id:id})" selected-type="selectedType" selected-id="selectedId" \
            		format-category-name="formatCategoryName({category:category})" format-feed-name="formatFeedName({feed:feed})">\
            		</category>');
            $compile(ul.contents())(scope);
	     }
	};
});

module.directive('toolbar', function(SettingsService) {
	return {
		scope : {},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/toolbar.html',
		controller : function($scope, SettingsService) {
			$scope.settings = SettingsService.settings;
		},
		link : function($scope, element) {
			element.find('button').bind('click', function() {
				SettingsService.save();
			});
		}
	};
});