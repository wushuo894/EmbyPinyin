<template>
  <el-dialog v-model="dialogVisible" title="设置" center v-if="dialogVisible" v-loading="loading"
             style="max-width: 800px">
    <el-form label-width="auto">
      <el-form-item label="Host">
        <el-input v-model:model-value="config.host" placeholder="http://192.168.1.x:8096"/>
      </el-form-item>
      <el-form-item label="Key">
        <el-input v-model:model-value="config.key" placeholder="123456789"/>
      </el-form-item>
      <el-form-item label="定时任务">
        <el-switch v-model:model-value="config.cron"/>
        <el-input v-model:model-value="config.cronStr" placeholder="0 1 * * *" :disabled="!config.cron"/>
      </el-form-item>
      <el-form-item label="Debug">
        <el-switch v-model:model-value="config.debug"/>
      </el-form-item>
    </el-form>
    <div style="width: 100%;justify-content: end;display: flex;">
      <el-button bg text @click="ok" :loading="okLoading" icon="Check">确定</el-button>
    </div>
  </el-dialog>
</template>

<script setup>

import {ref} from "vue";
import api from "./api.js";
import {ElMessage} from "element-plus";

const dialogVisible = ref(false)
const loading = ref(false)
const config = ref({
  'host': '',
  'key': '',
  'debug': false,
  'cron': false,
  'cronStr': ''
})

let show = () => {
  dialogVisible.value = true
  loading.value = true
  api.get('/api/config')
      .then(res => {
        config.value = res.data
      })
      .finally(() => {
        loading.value = false
      })
}

let okLoading = ref(false)

let ok = () => {
  okLoading.value = true
  api.post('/api/config', config.value)
      .then(res => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        emit('call-back')
      })
      .finally(() => {
        okLoading.value = false
      })
}

defineExpose({
  show
})

const emit = defineEmits(['call-back'])

</script>