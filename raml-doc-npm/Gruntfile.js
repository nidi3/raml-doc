/*global module*/

module.exports = function (grunt) {
    const pkg = grunt.file.readJSON("package.json");
    grunt.initConfig({
        copy: {
            dist: {
                files: [
                    {
                        src: process.env['HOME'] + '/.m2/repository/guru/nidi/raml/raml-doc-standalone/' + pkg.version + '/raml-doc-standalone-' + pkg.version + '.jar',
                        dest: 'bin/raml-doc-standalone.jar'
                    },
                    {
                        cwd: 'src/',
                        expand: true,
                        src:['raml-doc'],
                        dest: 'bin'
                    },{
                        cwd: '..',
                        expand: true,
                        src:['README.md'],
                        dest: '.'
                    }
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-copy');

    grunt.registerTask('default', ['copy']);
};
