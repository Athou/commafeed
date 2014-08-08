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

gulp.task('sass', function() {
	return gulp.src(SRC_DIR + 'sass/app.scss').pipe(sass()).pipe(gulp.dest(TEMP_DIR + 'css'));
});

gulp.task('fonts', function() {
	gulp.src(SRC_DIR + 'lib/font-awesome/font/fontawesome-webfont.*').pipe(gulp.dest(BUILD_DIR + 'font'));
	gulp.src(SRC_DIR + 'lib/readabilicons/webfont/fonts/readabilicons-*').pipe(gulp.dest(BUILD_DIR + 'font'));
	return gulp.src(SRC_DIR + 'lib/zocial/css/zocial-regular-*').pipe(gulp.dest(BUILD_DIR + 'font'));
});

gulp.task('template-cache', function() {
	var options = {
		module : 'commafeed.services',
		root : 'templates/'
	};
	return gulp.src(SRC_DIR + 'templates/**/*.html').pipe(templateCache(options)).pipe(gulp.dest(TEMP_DIR + 'js'));
});

gulp.task('build', ['images', 'sass', 'fonts', 'template-cache'], function() {
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
		port : 8083,
		middleware : function() {
			return [modRewrite(['^/rest/(.*)$ http://localhost:8082/rest/$1 [P]'])];
		}
	});
});

gulp.task('dev', ['build', 'watch', 'serve']);
gulp.task('default', ['build']);