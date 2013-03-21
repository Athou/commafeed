var module = angular.module('commafeed.directives', []);

module.directive('category', function($compile) {
	return {
		scope: {
			node: '=',
			selectedType: '=',
			selectedId: '=',
			feedClick: '&',
			categoryClick: '&'
		},
		restrict : 'E',
		replace: true,
		templateUrl: 'directives/category.html',
		link: function(scope, element) {
            if (scope.node.children) {
                var ul = element.find('ul');
                ul.prepend('<category ng-repeat="child in node.children" node="child" feed-click="feedClick({id:id})" category-click="categoryClick({id:id})" selected-type="selectedType" selected-id="selectedId"></category>');
                $compile(ul.contents())(scope);
            }
	     }
	};
});