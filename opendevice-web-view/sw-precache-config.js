var path = "src/main/webapp/";

module.exports = {

  verbose: true,

  handleFetch: true, // Set false to Development

  directoryIndex : false,

  stripPrefix : path,

  navigateFallback: '/index.html',

  dynamicUrlToDependencies: {
    '/login': [path+'login.html']
  },

  staticFileGlobs: [
    path+'index.html',
    path+'login.html',
    path+'login.html',
    path+'manifest.json',
    path+'pages/**.html',
    path+'pages/subpages/**.html',
    path+'images/**.*',
    path+'images/devices/**.*',
    path+'images/boards/**.*',
    path+'fonts/fontawesome-webfont.*',
    path+'fonts/Source-Sans-*/*.ttf',
    path+'css/normalize.css',
    path+'css/login.css',
    path+'js/jquery-*.min.js',
    path+'dist/css/**.*',
    path+'dist/js/**.*',
  ]
};