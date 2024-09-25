import {createApp} from 'vue'
import 'element-plus/dist/index.css'
import './style.css'
import ElementPlus from 'element-plus'
import App from './App.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/theme-chalk/dark/css-vars.css'
import {zhCn} from "element-plus/es/locale/index";

let app = createApp(App)
app.use(ElementPlus, {
    locale: zhCn,
})
// 引入图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(ElementPlus)
    .mount('#app')
