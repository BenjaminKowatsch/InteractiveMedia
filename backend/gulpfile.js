const    gulp = require('gulp');
const  jshint = require('gulp-jshint');
const    jscs = require('gulp-jscs');
const stylish = require('gulp-jscs-stylish');
const apidoc = require('gulp-apidoc');

gulp.task('static-code-analysis', function() {
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
