<template>
  <div class="supply-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">防暑物资发放管理</span>
          <el-button type="primary" @click="handleIssue">
            <el-icon><Present /></el-icon>
            发放物资
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="mb-4">
        <el-tab-pane label="发放记录" name="records">
          <el-form :inline="true" :model="searchForm" class="mb-4">
            <el-form-item label="工人">
              <el-select v-model="searchForm.workerId" placeholder="选择工人" clearable style="width: 150px">
                <el-option v-for="w in workerList" :key="w.id" :label="w.nickname" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="物资">
              <el-select v-model="searchForm.supplyId" placeholder="选择物资" clearable style="width: 150px">
                <el-option v-for="s in supplyList" :key="s.id" :label="s.supplyName" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.issueStatus" placeholder="选择状态" clearable style="width: 130px">
                <el-option label="待确认" value="pending" />
                <el-option label="已确认" value="confirmed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="recordData" v-loading="recordLoading" border>
            <el-table-column prop="recordNo" label="发放单号" width="200" />
            <el-table-column prop="workerName" label="领取工人" width="110" />
            <el-table-column prop="supplyName" label="物资名称" width="120">
              <template #default="{ row }">
                <el-tag :type="getSupplyTagType(row.supplyCode)">{{ row.supplyName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发放数量" width="110">
              <template #default="{ row }">
                {{ row.quantity }} {{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="发放人" width="100" />
            <el-table-column prop="issueTime" label="发放时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.issueTime) }}</template>
            </el-table-column>
            <el-table-column prop="issueStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.issueStatus === 'confirmed' ? 'success' : row.issueStatus === 'cancelled' ? 'danger' : 'warning'">
                  {{ row.issueStatus === 'confirmed' ? '已确认' : row.issueStatus === 'cancelled' ? '已取消' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" link @click="handleDeleteRecord(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="recordPagination.current"
              v-model:page-size="recordPagination.size"
              :total="recordPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRecordSizeChange"
              @current-change="handleRecordCurrentChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="物资库存" name="stock">
          <el-table :data="supplyList" v-loading="supplyLoading" border>
            <el-table-column prop="supplyCode" label="物资编码" width="150" />
            <el-table-column prop="supplyName" label="物资名称" width="120">
              <template #default="{ row }">
                <el-tag :type="getSupplyTagType(row.supplyCode)">{{ row.supplyName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="stockQuantity" label="库存数量" width="120">
              <template #default="{ row }">
                <span :class="Number(row.stockQuantity) < 50 ? 'text-red-600 font-bold' : 'text-green-600 font-semibold'">
                  {{ row.stockQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="issueDialogVisible" title="发放防暑物资" width="560px" :close-on-click-modal="false">
      <el-form :model="issueForm" :rules="issueFormRules" ref="issueFormRef" label-width="100px">
        <el-form-item label="关联排班">
          <el-select v-model="issueForm.scheduleId" placeholder="选择排班（可选）" class="w-full" clearable>
            <el-option
              v-for="s in todayOutdoorSchedules"
              :key="s.id"
              :label="s.scheduleDate + ' ' + s.workerName + ' - ' + s.positionName"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工人" prop="workerId">
          <el-select v-model="issueForm.workerId" placeholder="选择工人" class="w-full" @change="onIssueWorkerChange">
            <el-option v-for="w in workerList" :key="w.id" :label="w.nickname" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物资" prop="supplyId">
          <el-select v-model="issueForm.supplyId" placeholder="选择物资" class="w-full" @change="onIssueSupplyChange">
            <el-option
              v-for="s in supplyList"
              :key="s.id"
              :label="s.supplyName + ' (库存:' + s.stockQuantity + s.unit + ')'"
              :value="s.id"
              :disabled="Number(s.stockQuantity) <= 0"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发放数量" prop="quantity">
          <el-input-number v-model="issueForm.quantity" :min="1" :max="issueForm.maxQuantity || 100" />
          <span class="ml-2 text-sm text-gray-500">{{ issueForm.unit || '' }}</span>
        </el-form-item>
        <el-form-item label="发放人">
          <el-select v-model="issueForm.warehouseId" placeholder="选择仓管" class="w-full" @change="onWarehouseChange">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.nickname" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="issueForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="showHeatIssueTips"
        title="建议为室外高温作业工人发放以下物资"
        type="info"
        show-icon
        :closable="false"
        class="mb-4"
      >
        <template #default>
          <div class="flex flex-wrap gap-2 mt-2">
            <el-tag type="danger">盐丸 x2袋/天</el-tag>
            <el-tag type="success">冰袖 x1副</el-tag>
            <el-tag type="primary">饮水券 x4张/天</el-tag>
          </div>
        </template>
      </el-alert>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="issueDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleIssueConfirm" :loading="issueSubmitting">确认发放</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Present } from '@element-plus/icons-vue'
import { supplyRecordApi } from '@/api/supplyRecord'
import { heatSupplyApi } from '@/api/heatSupply'
import { userApi } from '@/api/user'
import { scheduleApi } from '@/api/schedule'
import dayjs from 'dayjs'

const activeTab = ref('records')
const recordLoading = ref(false)
const supplyLoading = ref(false)
const issueSubmitting = ref(false)
const recordData = ref([])
const supplyList = ref([])
const workerList = ref([])
const warehouseList = ref([])
const todayOutdoorSchedules = ref([])
const issueDialogVisible = ref(false)
const issueFormRef = ref(null)

const searchForm = reactive({
  workerId: '',
  supplyId: '',
  issueStatus: ''
})

const recordPagination = reactive({ current: 1, size: 10, total: 0 })

const issueForm = reactive({
  scheduleId: null,
  workerId: null,
  workerName: '',
  supplyId: null,
  supplyName: '',
  supplyCode: '',
  unit: '',
  maxQuantity: 100,
  quantity: 1,
  warehouseId: null,
  warehouseName: '',
  remark: ''
})

const issueFormRules = {
  workerId: [{ required: true, message: '请选择工人', trigger: 'change' }],
  supplyId: [{ required: true, message: '请选择物资', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const showHeatIssueTips = computed(() => {
  const s = todayOutdoorSchedules.value.find(x => x.id === issueForm.scheduleId)
  return s && s.heatWarning === 1
})

const getSupplyTagType = (code) => {
  if (code && code.startsWith('SALT')) return 'danger'
  if (code && code.startsWith('SLEEVE')) return 'success'
  if (code && code.startsWith('WATER')) return 'primary'
  return 'info'
}

const loadRecordData = async () => {
  recordLoading.value = true
  try {
    const params = {
      current: recordPagination.current,
      size: recordPagination.size,
      ...searchForm
    }
    if (!params.workerId) delete params.workerId
    if (!params.supplyId) delete params.supplyId
    if (!params.issueStatus) delete params.issueStatus
    const res = await supplyRecordApi.page(params)
    if (res.code === 200) {
      recordData.value = res.data.records
      recordPagination.total = res.data.total
    }
  } catch (e) { console.error(e); ElMessage.error('加载记录失败') }
  finally { recordLoading.value = false }
}

const loadSupplyList = async () => {
  supplyLoading.value = true
  try {
    const res = await heatSupplyApi.list()
    if (res.code === 200) supplyList.value = res.data
  } catch (e) { console.error(e) }
  finally { supplyLoading.value = false }
}

const loadWorkerList = async () => {
  try {
    const res = await userApi.list({ role: 'worker' })
    if (res.code === 200) workerList.value = res.data
  } catch (e) { console.error(e) }
}

const loadWarehouseList = async () => {
  try {
    const res = await userApi.list({ role: 'warehouse' })
    if (res.code === 200) warehouseList.value = res.data
  } catch (e) { console.error(e) }
}

const loadTodayOutdoorSchedules = async () => {
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const res = await scheduleApi.page({ current: 1, size: 100, scheduleDate: today, positionType: 'outdoor' })
    if (res.code === 200) todayOutdoorSchedules.value = res.data.records
  } catch (e) { console.error(e) }
}

const handleSearch = () => { recordPagination.current = 1; loadRecordData() }
const handleReset = () => {
  Object.assign(searchForm, { workerId: '', supplyId: '', issueStatus: '' })
  handleSearch()
}
const handleRecordSizeChange = (size) => { recordPagination.size = size; loadRecordData() }
const handleRecordCurrentChange = (current) => { recordPagination.current = current; loadRecordData() }

const handleIssue = () => {
  Object.assign(issueForm, {
    scheduleId: null,
    workerId: null,
    workerName: '',
    supplyId: null,
    supplyName: '',
    supplyCode: '',
    unit: '',
    maxQuantity: 100,
    quantity: 1,
    warehouseId: null,
    warehouseName: '',
    remark: ''
  })
  issueDialogVisible.value = true
  if (issueFormRef.value) issueFormRef.value.clearValidate()
}

const onIssueWorkerChange = (id) => {
  const w = workerList.value.find(x => x.id === id)
  if (w) issueForm.workerName = w.nickname
}

const onIssueSupplyChange = (id) => {
  const s = supplyList.value.find(x => x.id === id)
  if (s) {
    issueForm.supplyName = s.supplyName
    issueForm.supplyCode = s.supplyCode
    issueForm.unit = s.unit
    issueForm.maxQuantity = s.stockQuantity
    if (issueForm.quantity > s.stockQuantity) issueForm.quantity = 1
  }
}

const onWarehouseChange = (id) => {
  const w = warehouseList.value.find(x => x.id === id)
  if (w) issueForm.warehouseName = w.nickname
}

const handleIssueConfirm = async () => {
  if (!issueFormRef.value) return
  await issueFormRef.value.validate(async (valid) => {
    if (valid) {
      issueSubmitting.value = true
      try {
        const res = await supplyRecordApi.confirm(issueForm)
        if (res.code === 200) {
          ElMessage.success('发放成功')
          issueDialogVisible.value = false
          loadRecordData()
          loadSupplyList()
        }
      } catch (e) { console.error(e); ElMessage.error('发放失败：' + (e.message || '')) }
      finally { issueSubmitting.value = false }
    }
  })
}

const handleDeleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该发放记录吗？', '提示', { type: 'warning' })
    const res = await supplyRecordApi.delete(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadRecordData(); loadSupplyList() }
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('删除失败') } }
}

const formatDateTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'

onMounted(() => {
  loadRecordData()
  loadSupplyList()
  loadWorkerList()
  loadWarehouseList()
  loadTodayOutdoorSchedules()
})
</script>

<style scoped>
.supply-page { max-width: 1400px; margin: 0 auto; }
</style>
