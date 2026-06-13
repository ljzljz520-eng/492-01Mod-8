<template>
  <div class="h5-schedule-page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <div
          v-for="item in list"
          :key="item.id"
          class="schedule-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <van-icon name="calendar-o" size="16" class="text-gray-500" />
                <span class="text-sm font-semibold text-gray-800">{{ item.scheduleDate }}</span>
              </div>
              <van-tag :type="getStatusType(item.scheduleStatus)" size="medium" round>
                {{ getStatusText(item.scheduleStatus) }}
              </van-tag>
            </div>

            <div class="flex items-center justify-between mb-3">
              <div>
                <div class="text-base font-semibold text-gray-800">{{ item.positionName }}</div>
                <van-tag :type="item.positionType === 'outdoor' ? 'warning' : 'primary'" size="small" plain class="mt-1">
                  {{ item.positionType === 'outdoor' ? '室外岗位' : '室内岗位' }}
                </van-tag>
              </div>
              <div class="text-right">
                <div class="text-sm text-gray-600">
                  <van-icon name="clock-o" /> {{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}
                </div>
                <div class="text-xs text-gray-500 mt-1">共 {{ item.workHours }} 小时</div>
              </div>
            </div>

            <div v-if="item.heatWarning === 1" class="heat-warning rounded-lg p-3 bg-red-50 border border-red-200 mb-3">
              <div class="flex items-center gap-2 mb-2">
                <van-icon name="warning-o" size="18" color="#ef4444" />
                <span class="text-sm font-semibold text-red-600">高温作业预警</span>
              </div>
              <div class="space-y-1">
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600">当日气温</span>
                  <span class="text-red-600 font-bold">{{ item.temperature }}℃</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600">高温津贴</span>
                  <span class="text-orange-600 font-bold">¥{{ item.heatAllowance }}/天</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600">休息频次</span>
                  <span class="text-orange-600 font-semibold">{{ item.restFrequency }}次/天</span>
                </div>
              </div>
            </div>

            <div v-else class="text-sm text-gray-500 mb-3">
              <van-icon name="balance-o" size="14" /> 气温 {{ item.temperature || '-' }}℃，正常作业
            </div>

            <div v-if="item.remark" class="text-xs text-gray-400 border-t pt-2">
              备注：{{ item.remark }}
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { scheduleApi } from '@/api/schedule'
import dayjs from 'dayjs'

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  if (refreshing.value || list.value.length === 0) {
    pagination.current = 1
    if (refreshing.value) {
      list.value = []
      finished.value = false
    }
  }

  loading.value = true
  try {
    const res = await scheduleApi.page({
      current: pagination.current,
      size: pagination.size
    })
    if (res.code === 200) {
      if (refreshing.value || pagination.current === 1) {
        list.value = res.data.records
        refreshing.value = false
      } else {
        list.value.push(...res.data.records)
      }
      pagination.total = res.data.total

      if (list.value.length >= res.data.total) {
        finished.value = true
      } else {
        pagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  refreshing.value = true
  loadData()
}

const getStatusType = (status) => ({
  pending: 'primary', in_progress: 'warning', completed: 'success', cancelled: 'danger'
}[status] || 'primary')

const getStatusText = (status) => ({
  pending: '待执行', in_progress: '进行中', completed: '已完成', cancelled: '已取消'
}[status] || status)

const formatTime = (t) => t ? dayjs(t).format('HH:mm') : '-'

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.h5-schedule-page {
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}
.schedule-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}
.heat-warning {
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}
</style>
