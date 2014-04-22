module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);

  grunt.initConfig({
    // Dev
    connect: {
      options: {
        keepalive: true
      },
      src: {
      },
      dist: {
        options: {
          base: 'dist'
        }
      }
    },

    // Build
    copy: {
      dist: {
        files: [{
          expand: true,
          cwd: 'src',
          dest: 'dist',
          src: [
            'index.html'
          ]
        }]
      }
    },

    useminPrepare: {
      html: 'src/index.html',
      options: {
        dest: 'dist'
      }
    },

    usemin: {
      options: {
        dirs: ['dist']
      },
      html: ['dist/{,*/}*.html'],
      css: ['dist/css/{,*/}*.css']
    }
  });

  grunt.registerTask('default', ['connect:src']);

  // Sadly, Reveal wants to load its own Javascript which doesn't play nice with bower and friends...
  // I decided to give up on that for now.
  //grunt.registerTask('build', ['useminPrepare', 'concat', 'uglify', 'copy', 'usemin']);
}
