/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import Vue from 'vue'
import iView from 'view-design'
import VueRouter from 'vue-router'
import component from './components'
import App from './dss/view/app.vue'
import router from './router'
import i18n from './i18n'

import API_PATH from './common/config/apiPath.js'
import 'view-design/dist/styles/iview.css'

// Icon
import './components/svgIcon/index.js'

import './dss/module/index.js'
import './apps/scriptis/module/index.js'
import './apps/workflows/module/index.js'
import './apps/streamis/assets/streamisIconFont/iconfont.css'

Vue.use(VueRouter)
Vue.use(component)
Vue.use(iView, {
  i18n: (key, value) => i18n.t(key, value)
})

Vue.config.productionTip = false
Vue.prototype.$Message.config({
  duration: 5
})

// 全局变量
Vue.prototype.$API_PATH = API_PATH;

new Vue({
  router,
  i18n,
  render: (h) => h(App)
}).$mount('#app')
