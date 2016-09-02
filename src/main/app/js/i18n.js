var module = angular.module('commafeed.i18n', []);

module.service('LangService', [function() {
	this.langs = {
		'ar': 'العربية',
		'ca': 'Català',
		'en': 'English',
		'es': 'Español',
		'de': 'Deutsch',
		'fa': 'فارسی',
		'fr': 'Français',
		'gl': 'Galician',
		'glk': 'گیلکی',
		'hu': 'Magyar',
		'id': 'Indonesian',
		'ja': '日本語',
		'ko': '한국어',
		'nl': 'Nederlands',
		'nb': 'Norsk (bokmål)',
		'nn': 'Norsk (nynorsk)',
		'pt': 'Português',
		'pl': 'Polski',
		'ru': 'Русский',
		'fi': 'Suomi',
		'sv': 'Svenska',
		'zh': '简体中文',
		'it': 'Italiano',
		'tr': 'Türkçe',
		'cy': 'Cymraeg',
		'sk': 'Slovenčina',
		'da': 'Danish',
		'cs': 'Čeština',
		'ms': 'Bahasa Malaysian'
	}
}]);
