/*
 * Create references
 */
var gulp = require('gulp');
var pkg = require('./package.json');
//var common = require('./common.js');

/*
 * Auto load all gulp plugins
 */
var gulpLoadPlugins = require("gulp-load-plugins");
var plug = gulpLoadPlugins();

/*
 * Load common utilities for gulp
 */
var gutil = plug.loadUtils(['colors', 'env', 'log', 'date']);

/*
 * Could use a product/development switch.
 * Run `gulp --production`
 */
var type = gutil.env.production ? 'production' : 'development';
gutil.log( 'Building for', gutil.colors.magenta(type) );
gutil.beep();

/*
 * Lint the code
 */
gulp.task('jshint', function () {
    return gulp.src(pkg.paths.source.js)
        .pipe(plug.jshint('jshintrc.json'))
        .pipe(plug.jshint.reporter('jshint-stylish'));
});

/*
 * Minify and bundle the JavaScript
 */
gulp.task('default', ['jshint'], function () {
    var bundlefile = pkg.name + ".min.js";
    var opt = {newLine: ';'};

    return gulp.src(pkg.paths.source.js)
        .pipe(plug.size({showFiles: true}))
        .pipe(plug.uglify())
        .pipe(plug.concat(bundlefile, opt))
        .pipe(gulp.dest(pkg.paths.dest.js))
        .pipe(plug.size({showFiles: true}));

});
