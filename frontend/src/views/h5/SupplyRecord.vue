<template>
  <div class="h5-supply-page">
    <van-tabs v-model:active="activeTab" class="mb-2">
      <van-tab title="我的物资" name="mine">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadMineData"
          >
            <div
              v-for="item in mineList"
              :key="item.id"
              class="supply-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
            >
              <div class="p-4">
                <div class="flex items-center justify-between mb-3">
                  <div class="flex items-center gap-2">
                    <van-tag :type="getSupplyTagType(item.supplyCode)" size="medium" round>
                      {{ item.supplyName }}
                    </van-tag>
                    <span class="text-sm text-gray-600">x{{ item.quantity }} {{ item.unit }}</span>
                  </div>
                  <van-tag type="success" size="small" plain>已领取</van-tag>
                </div>

                <div class="flex items-center justify-between text-xs text-gray-500">
                  <div class="flex items-center gap-1">
                    <van-icon name="user-o" size="12" />
                    <span>发放人：{{ item.warehouseName || '-' }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <van-icon name="clock-o" size="12" />
                    <span>{{ formatTime(item.issueTime) }}</span>
                  </div>
                </div>

                <div class="mt-2 text-xs text-gray-400">
                  单号：{{ item.recordNo }}
                </div>
              </div>
            </div>

            <div v-if="mineList.length === 0 && !loading && !refreshing" class="text-center py-10 text-gray-400">
              <van-icon name="gift-o" size="48" class="block mx-auto mb-3 text-gray-300" />
              <span>暂无物资领取记录</span>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>

      <van-tab title="防暑说明" name="tips">
        <div class="p-4">
          <van-cell-group inset>
            <van-cell title="盐丸" icon="medal-o" value="2袋/天" label="口服补液盐，补充电解质，防止中暑脱水" />
            <van-cell title="冰袖" icon="shield-o" value="1副" label="防晒冰袖，降温防晒，保护手臂" />
            <van-cell title="饮水券" icon="balance-o" value="4张/天" label="饮用水兑换券，及时补充水分" />
          </van-cell-group>

          <van-cell-group inset class="mt-4">
            <van-cell title="高温休息规则" icon="clock-o" />
            <div class="px-4 py-3 text-sm text-gray-600 leading-7">
              <div>• <span class="text-orange-600 font-semibold">35~37℃</span>：每2小时休息1次，全天约3次</div>
              <div>• <span class="text-orange-600 font-semibold">37~40℃</span>：增加休息频次，全天约4次</div>
              <div>• <span class="text-red-600 font-semibold">40℃以上</span>：停止室外露天作业</div>
            </div>
          </van-cell-group>

          <van-cell-group inset class="mt-4">
            <van-cell title="高温津贴说明" icon="gold-coin-o" />
            <div class="px-4 py-3 text-sm text-gray-600 leading-7">
              <div>• 室外岗位当日气温≥35℃时，计发高温津贴</div>
              <div>• 高温津贴按天单独计发，不与普通工资混合</div>
              <div>• 工资明细中普通工资与高温津贴分开展示</div>
            </div>
          </van-cell-group>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { supplyRecordApi } from '@/api/supplyRecord'
import dayjs from 'dayjs'

const activeTab = ref('mine')
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const mineList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const getSupplyTagType = (code) => {
  if (code && code.startsWith('SALT')) return 'danger'
  if (code && code.startsWith('SLEEVE')) return 'success'
  if (code && code.startsWith('WATER')) return 'primary'
  return 'info'
}

const loadMineData = async () => {
  if (refreshing.value || mineList.value.length === 0) {
    pagination.current = 1
    if (refreshing.value) {
      mineList.value = []
      finished.value = false
    }
  }

  loading.value = true
  try {
    const res = await supplyRecordApi.page({
      current: pagination.current,
      size: pagination.size,
      issueStatus: 'confirmed'
    })
    if (res.code === 200) {
      if (refreshing.value || pagination.current === 1) {
        mineList.value = res.data.records
        refreshing.value = false
      } else {
        mineList.value.push(...res.data.records)
      }
      pagination.total = res.data.total

      if (mineList.value.length >= res.data.total) {
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
  loadMineData()
}

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'

onMounted(() => {
  loadMineData()
})
</script>

<style scoped>
.h5-supply-page {
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}
.supply-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}
</style>
