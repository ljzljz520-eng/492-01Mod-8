<template>
  <div class="schedule-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">排班管理</span>
          <div class="flex gap-2">
            <el-button type="success" @click="loadHeatConfig">
              <el-icon><Sunny /></el-icon>
              高温配置
            </el-button>
            <el-button type="primary" @click="handleAdd" class="rounded-lg shadow-sm">
              <el-icon><Plus /></el-icon>
              新增排班
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="排班日期">
          <el-date-picker v-model="searchForm.scheduleDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" clearable />
        </el-form-item>
        <el-form-item label="工人">
          <el-select v-model="searchForm.workerId" placeholder="选择工人" clearable style="width: 150px">
            <el-option v-for="w in workerList" :key="w.id" :label="w.nickname" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位类型">
          <el-select v-model="searchForm.positionType" placeholder="选择类型" clearable style="width: 130px">
            <el-option label="室内" value="indoor" />
            <el-option label="室外" value="outdoor" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.scheduleStatus" placeholder="选择状态" clearable style="width: 130px">
            <el-option label="待执行" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="todayHeatWarning"
        :title="'今日高温预警！当前气温 ' + todayTemperature + '℃，室外岗位请注意防暑降温'"
        type="error"
        show-icon
        :closable="false"
        class="mb-4"
      />

      <el-table :data="tableData" v-loading="loading" border class="rounded-lg overflow-hidden">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="scheduleDate" label="日期" width="110" />
        <el-table-column prop="positionName" label="岗位" width="130">
          <template #default="{ row }">
            <el-tag :type="row.positionType === 'outdoor' ? 'warning' : 'info'" size="small">
              {{ row.positionName }}
              <span class="ml-1">({{ row.positionType === 'outdoor' ? '室外' : '室内' }})</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerName" label="工人" width="110" />
        <el-table-column label="工作时段" width="240">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            <span class="text-gray-500 ml-1">({{ row.workHours }}h)</span>
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="气温" width="90">
          <template #default="{ row }">
            <span v-if="row.temperature" :class="isHighTemp(row.temperature) ? 'text-red-600 font-bold' : ''">
              {{ row.temperature }}℃
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="高温预警" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.heatWarning === 1" type="danger" effect="dark">高温</el-tag>
            <el-tag v-else type="info">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="高温津贴" width="110">
          <template #default="{ row }">
            <span class="text-orange-600 font-semibold" v-if="row.heatAllowance && Number(row.heatAllowance) > 0">
              ¥{{ row.heatAllowance }}/天
            </span>
            <span v-else class="text-gray-400">¥0.00</span>
          </template>
        </el-table-column>
        <el-table-column label="休息频次" width="100">
          <template #default="{ row }">
            <span v-if="row.restFrequency">{{ row.restFrequency }}次/天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="scheduleStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.scheduleStatus)" size="small">
              {{ getStatusText(row.scheduleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewHeatInfo(row)" v-if="row.heatWarning === 1">
              高温详情
            </el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker v-model="formData.scheduleDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="岗位" prop="positionId">
          <el-select v-model="formData.positionId" placeholder="选择岗位" class="w-full" @change="onPositionChange">
            <el-option v-for="p in positionList" :key="p.id" :label="p.positionName + '(' + (p.positionType==='outdoor'?'室外':'室内') + ')'" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工人" prop="workerId">
          <el-select v-model="formData.workerId" placeholder="选择工人" class="w-full" @change="onWorkerChange">
            <el-option v-for="w in workerList" :key="w.id" :label="w.nickname" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上班时间" prop="startTime">
          <el-date-picker v-model="formData.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" @change="calcHours" />
        </el-form-item>
        <el-form-item label="下班时间" prop="endTime">
          <el-date-picker v-model="formData.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" @change="calcHours" />
        </el-form-item>
        <el-form-item label="预计工时">
          <span>{{ formData.workHours || 0 }} 小时</span>
        </el-form-item>
        <el-form-item label="当日气温(℃)" prop="temperature">
          <el-input-number v-model="formData.temperature" :precision="1" :step="0.5" :min="-20" :max="50" @change="onTemperatureChange" />
          <span class="ml-3 text-sm text-gray-500">高温阈值：{{ heatThreshold }}℃</span>
        </el-form-item>

        <el-alert
          v-if="formData.positionType === 'outdoor' && isHighTemp(formData.temperature)"
          :title="'高温预警！当前气温 ' + formData.temperature + '℃'"
          type="warning"
          show-icon
          :closable="false"
          class="mb-4"
        />

        <div v-if="formData.positionType === 'outdoor'" class="heat-info-panel bg-orange-50 rounded-lg p-4 mb-4">
          <div class="text-base font-semibold text-orange-700 mb-3 flex items-center">
            <el-icon class="mr-1"><Warning /></el-icon>
            高温作业保障提示
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="高温津贴">
              <span class="text-orange-600 font-bold">¥{{ formData.heatAllowance || 0 }}/天</span>
            </el-descriptions-item>
            <el-descriptions-item label="休息频次">
              <span class="text-orange-600 font-bold">{{ formData.restFrequency || 0 }}次/天</span>
              <span class="ml-2 text-sm text-gray-500">（建议每2小时休息1次）</span>
            </el-descriptions-item>
            <el-descriptions-item label="防暑物资">
              <div class="flex flex-wrap gap-2">
                <el-tag type="danger" effect="plain">盐丸 x2袋</el-tag>
                <el-tag type="success" effect="plain">冰袖 x1副</el-tag>
                <el-tag type="primary" effect="plain">饮水券 x4张</el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="温馨提示" v-if="Number(formData.temperature) >= 40">
              <span class="text-red-600 font-bold">气温超过40℃，建议停止室外作业！</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-form-item label="状态" prop="scheduleStatus">
          <el-select v-model="formData.scheduleStatus" class="w-full">
            <el-option label="待执行" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="heatInfoVisible" title="高温作业保障详情" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="排班日期">{{ currentRow.scheduleDate }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ currentRow.positionName }} (室外)</el-descriptions-item>
        <el-descriptions-item label="工人">{{ currentRow.workerName }}</el-descriptions-item>
        <el-descriptions-item label="当日气温">
          <span class="text-red-600 font-bold">{{ currentRow.temperature }}℃</span>
        </el-descriptions-item>
        <el-descriptions-item label="高温津贴">
          <span class="text-orange-600 font-bold">¥{{ currentRow.heatAllowance || 0 }}/天</span>
        </el-descriptions-item>
        <el-descriptions-item label="休息频次">
          <span class="text-orange-600 font-bold">{{ currentRow.restFrequency || 0 }}次/天</span>
        </el-descriptions-item>
        <el-descriptions-item label="防暑物资发放">
          <div class="flex flex-wrap gap-2">
            <el-tag type="danger">盐丸</el-tag>
            <el-tag type="success">冰袖</el-tag>
            <el-tag type="primary">饮水券</el-tag>
          </div>
          <div class="mt-2 text-sm text-gray-500">请到【物资发放管理】确认发放</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sunny, Warning } from '@element-plus/icons-vue'
import { scheduleApi } from '@/api/schedule'
import { positionApi } from '@/api/position'
import { userApi } from '@/api/user'
import { heatConfigApi } from '@/api/heatConfig'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const heatInfoVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref(null)
const positionList = ref([])
const workerList = ref([])
const heatThreshold = ref(35)
const currentRow = ref({})

const todayTemperature = ref(36)
const todayHeatWarning = computed(() => Number(todayTemperature.value) >= Number(heatThreshold.value))

const searchForm = reactive({
  scheduleDate: '',
  workerId: '',
  positionType: '',
  scheduleStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  scheduleDate: '',
  positionId: null,
  positionName: '',
  positionType: '',
  workerId: null,
  workerName: '',
  startTime: '',
  endTime: '',
  workHours: 0,
  temperature: null,
  heatWarning: 0,
  heatAllowance: 0,
  restFrequency: 0,
  scheduleStatus: 'pending',
  remark: ''
})

const formRules = {
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  workerId: [{ required: true, message: '请选择工人', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择上班时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择下班时间', trigger: 'change' }],
  temperature: [{ required: true, message: '请输入当日气温', trigger: 'blur' }],
  scheduleStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const isHighTemp = (t) => t != null && Number(t) >= Number(heatThreshold.value)

const loadHeatConfig = async () => {
  try {
    const res = await heatConfigApi.getAll()
    if (res.code === 200 && res.data) {
      heatThreshold.value = Number(res.data.heat_threshold || 35)
    }
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    if (!params.workerId) delete params.workerId
    const res = await scheduleApi.page(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadPositionList = async () => {
  try {
    const res = await positionApi.list()
    if (res.code === 200) positionList.value = res.data
  } catch (e) { console.error(e) }
}

const loadWorkerList = async () => {
  try {
    const res = await userApi.list({ role: 'worker' })
    if (res.code === 200) workerList.value = res.data
  } catch (e) { console.error(e) }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, { scheduleDate: '', workerId: '', positionType: '', scheduleStatus: '' })
  handleSearch()
}

const onPositionChange = (id) => {
  const p = positionList.value.find(x => x.id === id)
  if (p) {
    formData.positionName = p.positionName
    formData.positionType = p.positionType
    applyHeatRule()
  }
}

const onWorkerChange = (id) => {
  const w = workerList.value.find(x => x.id === id)
  if (w) formData.workerName = w.nickname
}

const onTemperatureChange = () => {
  applyHeatRule()
}

const calcHours = () => {
  if (formData.startTime && formData.endTime) {
    const s = dayjs(formData.startTime)
    const e = dayjs(formData.endTime)
    const mins = e.diff(s, 'minute')
    formData.workHours = (mins / 60).toFixed(2)
  }
}

const applyHeatRule = () => {
  if (formData.positionType !== 'outdoor') {
    formData.heatWarning = 0
    formData.heatAllowance = 0
    formData.restFrequency = 0
    return
  }
  const t = Number(formData.temperature)
  if (isNaN(t) || t < Number(heatThreshold.value)) {
    formData.heatWarning = 0
    formData.heatAllowance = 0
    formData.restFrequency = 0
    return
  }
  formData.heatWarning = 1
  const p = positionList.value.find(x => x.id === formData.positionId)
  formData.heatAllowance = p && p.heatAllowance ? Number(p.heatAllowance) : 30
  if (t >= 40) formData.restFrequency = 6
  else if (t >= 37) formData.restFrequency = 4
  else formData.restFrequency = 3
}

const handleAdd = () => {
  dialogTitle.value = '新增排班'
  Object.assign(formData, {
    id: null,
    scheduleDate: dayjs().format('YYYY-MM-DD'),
    positionId: null,
    positionName: '',
    positionType: '',
    workerId: null,
    workerName: '',
    startTime: dayjs().hour(8).minute(0).second(0).format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs().hour(17).minute(0).second(0).format('YYYY-MM-DD HH:mm:ss'),
    workHours: 9,
    temperature: 36,
    heatWarning: 0,
    heatAllowance: 0,
    restFrequency: 0,
    scheduleStatus: 'pending',
    remark: ''
  })
  calcHours()
  applyHeatRule()
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑排班'
  try {
    const res = await scheduleApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        scheduleDate: res.data.scheduleDate,
        positionId: res.data.positionId,
        positionName: res.data.positionName,
        positionType: res.data.positionType,
        workerId: res.data.workerId,
        workerName: res.data.workerName,
        startTime: res.data.startTime || '',
        endTime: res.data.endTime || '',
        workHours: res.data.workHours || 0,
        temperature: res.data.temperature != null ? Number(res.data.temperature) : null,
        heatWarning: res.data.heatWarning || 0,
        heatAllowance: res.data.heatAllowance || 0,
        restFrequency: res.data.restFrequency || 0,
        scheduleStatus: res.data.scheduleStatus || 'pending',
        remark: res.data.remark || ''
      })
      dialogVisible.value = true
      if (formRef.value) formRef.value.clearValidate()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  }
}

const handleViewHeatInfo = (row) => {
  currentRow.value = row
  heatInfoVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await scheduleApi.update(formData.id, formData)
        } else {
          res = await scheduleApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '新增失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', { type: 'warning' })
    const res = await scheduleApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const getStatusType = (status) => ({
  pending: 'info', in_progress: 'warning', completed: 'success', cancelled: 'danger'
}[status] || 'info')

const getStatusText = (status) => ({
  pending: '待执行', in_progress: '进行中', completed: '已完成', cancelled: '已取消'
}[status] || status)

const formatTime = (t) => t ? dayjs(t).format('HH:mm') : '-'

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handleCurrentChange = (current) => { pagination.current = current; loadData() }

onMounted(() => {
  loadHeatConfig()
  loadPositionList()
  loadWorkerList()
  loadData()
})
</script>

<style scoped>
.schedule-page { max-width: 1500px; margin: 0 auto; }
.heat-info-panel { border: 1px solid #fed7aa; }
</style>
