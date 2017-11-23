var    gulp = require('gulp');
var  jshint = require('gulp-jshint');
var    jscs = require('gulp-jscs');
var stylish = require('gulp-jscs-stylish');
var apidoc = require('gulp-apidoc');

gulp.task('default', function() {
  return gulp.src(['./*.js','./**/*.js',
                   '!node_modules/**','!prod_node_modules/**',
                    '!doc/**'])
    .pipe(jshint('.jshintrc'))
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'))
    .pipe(jscs('.jscsrc'))
    .pipe(stylish())
    .pipe(jscs.reporter())
    .pipe(jscs.reporter('fail'));
});

gulp.task('apidoc', function(done) {
    apidoc({
        src: 'routes/',
        dest: 'doc/'
      }, done);
  });
