var path = require('path')
var utils = require('./utils')
var config = require('../config')
var vueLoaderConfig = require('./vue-loader.conf')

function resolve(dir) {
    return path.join(__dirname, '..', dir)
}

module.exports = {
    entry: {
        app: './src/main.js'
    },
    output: {
        path: config.build.assetsRoot,
        filename: '[name].js',
        publicPath: process.env.NODE_ENV === 'production' ?
            config.build.assetsPublicPath : config.dev.assetsPublicPath
    },
    resolve: {
        extensions: ['.js', '.vue', '.json'],
        alias: {
            'vue$': 'vue/dist/vue.esm.js',
            '@': resolve('src')
        }
    },
    module: {
        rules: [{
                test: /\.vue$/,
                loader: 'vue-loader',
                options: vueLoaderConfig
            },
            {
                test: /\.js$/,
                loader: 'string-replace-loader',
                query: {
                    multiple: [
                        { search: '${JWT_SIMPLE_SECRET}', replace: process.env.JWT_SIMPLE_SECRET },
                        //  { search: '${WEB_SERVICE_PROD_URL}', replace: process.env.WEB_SERVICE_PROD_URL },
                        { search: '${WEB_SERVICE_URL}', replace: process.env.WEB_SERVICE_URL },
                        { search: '${FACEBOOK_API_VERSION}', replace: process.env.FACEBOOK_API_VERSION },
                        { search: '${ORIGIN_URL}', replace: process.env.ORIGIN_URL },
                        { search: '${CLIENT_ID}', replace: process.env.CLIENT_ID },
                        { search: '${APP_ID}', replace: process.env.APP_ID }                  
                    ]
                }
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                include: [resolve('src'), resolve('test')]
            },
            {
                test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
                loader: 'url-loader',
                query: {
                    limit: 10000,
                    name: utils.assetsPath('img/[name].[hash:7].[ext]')
                }
            },
            {
                test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
                loader: 'url-loader',
                query: {
                    limit: 10000,
                    name: utils.assetsPath('fonts/[name].[hash:7].[ext]')
                }
            }
        ]
    }
}