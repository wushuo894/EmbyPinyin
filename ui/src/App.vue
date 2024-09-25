<template>
  <Config ref="configRef" @call-back="getViews"/>
  <Logs ref="logsRef"/>
  <el-card style="width: 900px;">
    <div>
      <div class="auto" style="justify-content: space-between;width: 100%;padding-bottom: 12px;">
        <div v-loading="cronLoading" class="auto">
          <el-button bg text :disabled="!selectViews.length" @click="start" icon="Promotion" type="primary">开始
          </el-button>
          <el-button bg text icon="RefreshRight" @click="getViews">刷新</el-button>
          <div style="margin: 6px;"></div>
          <div>
            <el-button bg text :disabled="!selectViews.filter(it => it.cron).length" @click="cron(false)" icon="Minus">
              取消定时任务
            </el-button>
            <el-button bg text :disabled="!selectViews.filter(it => !it.cron).length" @click="cron(true)" icon="Plus">
              设置定时任务
            </el-button>
          </div>
        </div>
        <div class="auto">
          <div style="margin: 6px;"></div>
          <el-button bg text @click="logsRef?.show" icon="Tickets">日志</el-button>
          <el-button bg text @click="configRef?.show" icon="Operation">设置</el-button>
        </div>
      </div>
      <el-table
          width="900px"
          height="400px"
          v-model:data="views"
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column label="id" prop="id" width="180"/>
        <el-table-column label="媒体库名称" prop="name"/>
        <el-table-column label="定时任务" prop="cron"/>
      </el-table>
      <div style="margin-top: 16px">
        <div>
          <el-text class="mx-1" size="small">
            {{ status.current }} / {{ status.total }}
          </el-text>
        </div>
        <el-progress :percentage="progress" :indeterminate="status.loading"
                     :status="progress === 100 ? 'success':''"/>
      </div>
    </div>
  </el-card>

</template>

<script setup>

import api from "./api.js";
import {onMounted, ref} from "vue";
import Config from "./Config.vue";
import {ElMessage} from "element-plus";
import Logs from "./Logs.vue";
import {useDark} from "@vueuse/core";

const views = ref([])
const configRef = ref()
const logsRef = ref()
const selectViews = ref([])
const getViewsLoading = ref(false)

let getViews = () => {
  getViewsLoading.value = true
  api.get('api/views')
      .then(res => {
        views.value = res.data
      })
      .finally(() => {
        getViewsLoading.value = false
      })
}

let handleSelectionChange = (selectViewsValue) => {
  selectViews.value = selectViewsValue
}

let startLoading = ref(false)

let start = () => {
  startLoading.value = true
  api.post('api/pinyin', selectViews.value)
      .then(res => {
        ElMessage.success(res.message)
      })
      .finally(() => {
        startLoading.value = false
      })
}

let cronLoading = ref(false)

let cron = (add) => {
  cronLoading.value = true
  for (let valueElement of selectViews.value) {
    valueElement.cron = add
  }
  let cronIds = selectViews.value.filter(it => it['cron']).map(it => it['id'])
  setCron(cronIds)
      .finally(() => {
        cronLoading.value = false
        getViews()
      })
}

let setCron = async (ids) => {
  let res = await api.get('api/config')
  res.data.cronIds = ids
  res = await api.post('api/config', res.data)
  ElMessage.success(res.message)
}


let status = ref({
  'total': 0,
  'current': 0,
  'loading': false,
  'start': false
})

let progress = ref(0.0)

onMounted(() => {
  getViews()
  setInterval(() => {
    try {
      api.get('api/status')
          .then(res => {
            status.value = res.data
            let {current, total, loading} = status.value
            if (current === 0 && total === 0) {
              if (loading) {
                progress.value = 50
              }
              return
            }
            progress.value = Number.parseFloat((current / total * 100).toFixed(2))
          })
    } catch (e) {
      console.log(e);
    }
  }, 1000)
})


useDark()
</script>
