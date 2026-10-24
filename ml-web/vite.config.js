import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],

    // 配置用户前台项目的IP和端口号
    server: {
        host: 'localhost',
        port: 24108,
    }
})
