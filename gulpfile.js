var gulp = require('gulp');
var minifyCSS = require('gulp-minify-css');
var uglify = require('gulp-uglify');
var filter = require('gulp-filter');
var bower = require('gulp-bower');
var connect = require('gulp-connect');
var modRewrite = require('connect-modrewrite');
var sass = require('gulp-sass');
var useref = require('gulp-useref');
var templateCache = require('gulp-angular-templatecache');

var SRC_DIR = 'src/main/app/';
var TEMP_DIR = 'target/gulp/'
var BUILD_DIR = 'target/classes/assets/';

gulp.task('bower', function() {
	return bower();
});

gulp.task('images', function() {
	return gulp.src(SRC_DIR + 'images/**/*').pipe(gulp.dest(BUILD_DIR + 'images'));
});

gulp.task('favicons', function() {
	var favicons_png = SRC_DIR + '*.png';
	var favicons_ico = SRC_DIR + '*.ico';
	var favicons_svg = SRC_DIR + '*.svg';
	return gulp.src([favicons_png, favicons_ico, favicons_svg]).pipe(gulp.dest(BUILD_DIR));
});

gulp.task('sass', function() {
	return gulp.src(SRC_DIR + 'sass/app.scss').pipe(sass()).pipe(gulp.dest(TEMP_DIR + 'css'));
});

gulp.task('fonts', ['bower'], function() {
	var font_awesome = SRC_DIR + 'lib/font-awesome/font/fontawesome-webfont.*';
	var zocial = SRC_DIR + 'lib/zocial/css/zocial-regular-*';
	var readabilicons = SRC_DIR + 'lib/readabilicons/webfont/fonts/readabilicons-*';
	return gulp.src([font_awesome, zocial, readabilicons]).pipe(gulp.dest(BUILD_DIR + 'font'));
});

gulp.task('template-cache', function() {
	var options = {
		module : 'commafeed.services',
		root : 'templates/'
	};
	return gulp.src(SRC_DIR + 'templates/**/*.html').pipe(templateCache(options)).pipe(gulp.dest(TEMP_DIR + 'js'));
});

gulp.task('build', ['images', 'favicons', 'sass', 'fonts', 'template-cache', 'bower'], function() {
	var assets = useref.assets({
		searchPath : [SRC_DIR, TEMP_DIR]
	});
	var jsFilter = filter("**/*.js");
	var cssFilter = filter("**/*.css");
	return gulp.src([SRC_DIR + 'index.html', TEMP_DIR + 'app.css']).pipe(assets)

	.pipe(cssFilter).pipe(minifyCSS()).pipe(cssFilter.restore())

	.pipe(jsFilter).pipe(uglify()).pipe(jsFilter.restore())

	.pipe(assets.restore()).pipe(useref()).pipe(gulp.dest(BUILD_DIR));
});

gulp.task('watch', function() {
	gulp.watch(SRC_DIR + 'sass/**/*.scss', ['build']);
	gulp.watch(SRC_DIR + 'js/**/*.js', ['build']);
});

gulp.task('serve', function() {
	connect.server({
		root : BUILD_DIR,
		port : 8082,
		middleware : function() {
			var rest = '^/rest/(.*)$ http://localhost:8083/rest/$1 [P]';
			var next = '^/next(.*)$ http://localhost:8083/next$1 [P]';
			var logout = '^/logout(.*)$ http://localhost:8083/logout$1 [P]';
			return [modRewrite([rest, next, logout])];
		}
	});
});

gulp.task('dev', ['build', 'watch', 'serve']);
gulp.task('default', ['build']);