/*global module*/

module.exports = function (grunt) {
    const pkg = grunt.file.readJSON("package.json");
    grunt.initConfig({
        clean: {
            clean: ['raml-doc-standalone/bin']
        },
        copy: {
            dist: {
                files: [
                    {
                        src: process.env['HOME'] + '/.m2/repository/guru/nidi/raml/raml-doc-standalone/' + pkg.version + '/raml-doc-standalone-' + pkg.version + '.jar',
                        dest: 'raml-doc-standalone/bin/raml-doc-standalone.jar'
                    }]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-copy');

    grunt.registerTask('default', ['copy']);
};
