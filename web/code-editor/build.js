const esbuild = require('esbuild');
const path = require('path');
const fs = require('fs');

const isWatch = process.argv.includes('--watch');

// 输出目录
const outDir = path.resolve(
    __dirname,
    '../../feature/code-editor/src/main/assets/code-editor'
);

// 确保输出目录存在
if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
}

// 主编辑器构建配置
const mainBuildOptions = {
    entryPoints: [path.resolve(__dirname, 'editor.src.js')],
    bundle: true,
    outfile: path.resolve(outDir, 'editor.bundle.js'),
    format: 'iife',
    target: ['es2017'],
    minify: !isWatch,
    sourcemap: isWatch,
    define: {
        'process.env.NODE_ENV': isWatch ? '"development"' : '"production"'
    },
    logLevel: 'info',
    nodePaths: [
        path.resolve(__dirname, 'node_modules'),
        path.resolve(__dirname, '../node_modules')
    ],
    loader: {
        '.woff': 'dataurl',
        '.woff2': 'dataurl',
        '.ttf': 'dataurl',
        '.eot': 'dataurl',
    },
    banner: {
        js: `(function(){
if(typeof Object.hasOwn!=='function'){Object.hasOwn=function(obj,prop){return Object.prototype.hasOwnProperty.call(Object(obj),prop);};}
if(typeof Array.prototype.at!=='function'){Object.defineProperty(Array.prototype,'at',{value:function(index){var len=this.length>>>0;var relative=Number(index)||0;var k=relative<0?len+relative:relative;return(k<0||k>=len)?undefined:this[k];},writable:true,configurable:true});}
if(typeof String.prototype.at!=='function'){Object.defineProperty(String.prototype,'at',{value:function(index){var str=String(this);var len=str.length;var relative=Number(index)||0;var k=relative<0?len+relative:relative;return(k<0||k>=len)?undefined:str.charAt(k);},writable:true,configurable:true});}
if(typeof Int8Array!=='undefined'&&typeof Int8Array.prototype.at!=='function'){var typedArrayCtors=[Int8Array,Uint8Array,Uint8ClampedArray,Int16Array,Uint16Array,Int32Array,Uint32Array,Float32Array,Float64Array];if(typeof BigInt64Array!=='undefined')typedArrayCtors.push(BigInt64Array);if(typeof BigUint64Array!=='undefined')typedArrayCtors.push(BigUint64Array);for(var i=0;i<typedArrayCtors.length;i++){var Ctor=typedArrayCtors[i];if(Ctor&&Ctor.prototype&&typeof Ctor.prototype.at!=='function'){Object.defineProperty(Ctor.prototype,'at',{value:function(index){var len=this.length>>>0;var relative=Number(index)||0;var k=relative<0?len+relative:relative;return(k<0||k>=len)?undefined:this[k];},writable:true,configurable:true});}}}
})();`,
    },
};

async function build() {
    try {
        if (isWatch) {
            const ctx = await esbuild.context(mainBuildOptions);
            await ctx.watch();
            console.log('Watching for changes...');
        } else {
            await esbuild.build(mainBuildOptions);
            const stats = fs.statSync(mainBuildOptions.outfile);
            console.log(`✓ Built editor.bundle.js (${(stats.size / 1024).toFixed(2)} KB)`);
        }
    } catch (error) {
        console.error('Build failed:', error);
        process.exit(1);
    }
}

build();
