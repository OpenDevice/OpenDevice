'use strict';


var config = {
    environment: 'development',
    src_folder : 'src/main/webapp',

    development: function () {
        return this.environment === 'development';
    },
    production: function () {
        return this.environment === 'production';
    }
};


var gulp = require('gulp'),
    useref = require('gulp-useref'),
    gulpif = require('gulp-if'),
    rename = require("gulp-rename"),
    uglify = require('gulp-uglify'),
    minifyCss = require('gulp-clean-css');
    /** watch = require('gulp-watch'),
    connect = require('gulp-connect')**/

gulp.task('copy-deps', function() {
     return gulp.src('../opendevice-clients/opendevice-js/dist/js/opendevice.js').pipe(gulp.dest(config.src_folder + "/js"));
});

gulp.task('build', ['copy-deps','generate-service-worker'], function () {
    return gulp.src(config.src_folder+'/index.src.html')
        .pipe(rename('dist/index.html'))
        .pipe(gulpif(config.production(), useref({ searchPath: config.src_folder , newLine : '/* ---------- */'})))
        // .pipe(gulpif(config.production(), gulpif('*.js', uglify())))
        .pipe(gulpif('*.css', minifyCss()))
        .pipe(gulp.dest(config.src_folder));
});


gulp.task('generate-service-worker', function(callback) {
        var swPrecache = require('sw-precache');
        var swConfig = require('./sw-precache-config.js');
        if(config.development()){
            swConfig.handleFetch = false;
            swConfig.staticFileGlobs = [];
        }
        swPrecache.write(config.src_folder+'/service-worker.js', swConfig, callback);
});

gulp.task('build:production', ['set-production', 'build']);

gulp.task('set-production', function () {
    config.environment = 'production';
});


// -- Live reload

/**
 * Live reload server
 */
gulp.task('webserver', function() {
    connect.server({
        root: config.src_folder+'/dist',
        livereload: true,
        port: 8888
    });
});

gulp.task('livereload', function() {
    gulp.src([config.src_folder+'/**/*.*'])
        .pipe(watch([config.src_folder+'/**/*.*']))
        .pipe(connect.reload());
});
