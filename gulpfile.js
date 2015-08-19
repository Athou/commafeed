var gulp = require('gulp');
var rev = require('gulp-rev');
var revReplace = require('gulp-rev-replace');
var minifyCSS = require('gulp-minify-css');
var uglify = require('gulp-uglify');
var filter = require('gulp-filter');
var connect = require('gulp-connect');
var modRewrite = require('connect-modrewrite');
var sass = require('gulp-sass');
var useref = require('gulp-useref');
var templateCache = require('gulp-angular-templatecache');

var SRC_DIR = 'src/main/app/';
var TEMP_DIR = 'target/gulp/'
var BUILD_DIR = 'target/classes/assets/';

gulp.task('images', function() {
	return gulp.src(SRC_DIR + 'images/**/*').pipe(gulp.dest(BUILD_DIR + 'images'));
});

gulp.task('i18n', function() {
	return gulp.src(SRC_DIR + 'i18n/**/*.js').pipe(gulp.dest(BUILD_DIR + 'i18n'));
});

gulp.task('resources', function() {
	var favicons_png = SRC_DIR + '*.png';
	var favicons_ico = SRC_DIR + '*.ico';
	var favicons_svg = SRC_DIR + '*.svg';
	var manifest = SRC_DIR + 'manifest.json';
	return gulp.src([favicons_png, favicons_ico, favicons_svg, manifest]).pipe(gulp.dest(BUILD_DIR));
});

gulp.task('sass', function() {
	return gulp.src(SRC_DIR + 'sass/app.scss').pipe(sass()).pipe(gulp.dest(TEMP_DIR + 'css'));
});

gulp.task('fonts', function() {
	var font_awesome = SRC_DIR + 'lib/font-awesome/font/fontawesome-webfont.*';
	var zocial = SRC_DIR + 'lib/zocial-less/css/zocial-regular-*';
	var readabilicons = SRC_DIR + 'lib/readabilicons/webfont/fonts/readabilicons-*';
	return gulp.src([font_awesome, zocial, readabilicons]).pipe(gulp.dest(BUILD_DIR + 'font'));
});

gulp.task('select2', function() {
	var gif = SRC_DIR + 'lib/select2/*.gif';
	var png = SRC_DIR + 'lib/select2/*.png';
	return gulp.src([gif, png]).pipe(gulp.dest(BUILD_DIR + 'css'));
});

gulp.task('swagger-ui', function() {
	var index_html = SRC_DIR + 'api/index.html';
	var swagger_json = 'target/swagger/swagger.json';
	var lib = SRC_DIR + 'lib/swagger-ui/dist/**/*';
	return gulp.src([lib, index_html, swagger_json]).pipe(gulp.dest(BUILD_DIR + 'api'));
});

gulp.task('template-cache', function() {
	var options = {
		module : 'commafeed.services',
		root : 'templates/'
	};
	return gulp.src(SRC_DIR + 'templates/**/*.html').pipe(templateCache(options)).pipe(gulp.dest(TEMP_DIR + 'js'));
});

gulp.task('build-dev', ['images', 'i18n', 'resources', 'sass', 'fonts', 'select2', 'swagger-ui', 'template-cache'], function() {
	var assets = useref.assets({
		searchPath : [SRC_DIR, TEMP_DIR]
	});
	var jsFilter = filter("**/*.js");
	var cssFilter = filter("**/*.css");
	return gulp.src([SRC_DIR + 'index.html', TEMP_DIR + 'app.css']).pipe(assets).pipe(rev()).pipe(assets.restore()).pipe(useref()).pipe(
			revReplace()).pipe(gulp.dest(BUILD_DIR)).pipe(connect.reload());
});

gulp.task('build', ['images', 'i18n', 'resources', 'sass', 'fonts', 'select2', 'swagger-ui', 'template-cache'], function() {
	var assets = useref.assets({
		searchPath : [SRC_DIR, TEMP_DIR]
	});
	var jsFilter = filter("**/*.js");
	var cssFilter = filter("**/*.css");
	return gulp.src([SRC_DIR + 'index.html', TEMP_DIR + 'app.css']).pipe(assets)

	.pipe(cssFilter).pipe(minifyCSS()).pipe(cssFilter.restore())

	.pipe(jsFilter).pipe(uglify()).pipe(jsFilter.restore())

	.pipe(rev()).pipe(assets.restore()).pipe(useref()).pipe(revReplace()).pipe(gulp.dest(BUILD_DIR));
});

gulp.task('watch', function() {
	gulp.watch(SRC_DIR + 'sass/**/*.scss', ['build-dev']);
	gulp.watch(SRC_DIR + 'js/**/*.js', ['build-dev']);
	gulp.watch(SRC_DIR + 'i18n/**/*.js', ['build-dev']);
	gulp.watch(SRC_DIR + 'templates/**/*.html', ['build-dev']);
});

gulp.task('serve', function() {
	connect.server({
		root : BUILD_DIR,
		port : 8082,
		livereload : true,
		middleware : function() {
			var api = '^/api/(.*)$ http://localhost:8083/rest/$1 [P]';
			var rest = '^/rest/(.*)$ http://localhost:8083/rest/$1 [P]';
			var next = '^/next(.*)$ http://localhost:8083/next$1 [P]';
			var logout = '^/logout$ http://localhost:8083/logout [P]';
			var custom_css = '^/custom_css.css$ http://localhost:8083/custom_css.css [P]';
			var analytics = '^/analytics.js http://localhost:8083/analytics.js [P]';
			return [modRewrite([rest, next, logout, custom_css, analytics])];
		}
	});
});

gulp.task('dev', ['build-dev', 'watch', 'serve']);
gulp.task('default', ['build']);
