+function () {
    const webpack = require('webpack');
    const CompressionPlugin = require("compression-webpack-plugin");
    const ContextReplacementPlugin = webpack.ContextReplacementPlugin;

    config.optimization = {
        usedExports: true,
        splitChunks: {
            chunks: 'all',
            filename: 'modules.js'
        }
    };
    config.plugins.push(new CompressionPlugin({
        algorithm: "gzip"
    }));
    config.plugins.push(new ContextReplacementPlugin(/moment[\/\\]locale$/, /en\-gb/));

    const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
    config.plugins.push(new BundleAnalyzerPlugin({
        analyzerMode: 'static',
        reportFilename: '../../../../reports/webpack/BookmarkSync/BookmarkSync.js.report.html',
        generateStatsFile: true,
        statsFilename: '../../../../reports/webpack/BookmarkSync/BookmarkSync.js.stats.json',
        openAnalyzer: false
    }));
}()
