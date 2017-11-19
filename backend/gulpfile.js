var    gulp = require('gulp');
var  jshint = require('gulp-jshint');
var    jscs = require('gulp-jscs');
var stylish = require('gulp-jscs-stylish');

gulp.task('default', function() {
  return gulp.src(['./*.js','./**/*.js',
                   '!node_modules/**','!prod_node_modules/**'])
    .pipe(jshint('.jshintrc'))
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'))
    .pipe(jscs('.jscsrc'))
    .pipe(stylish())
    .pipe(jscs.reporter())
    .pipe(jscs.reporter('fail'));
});
