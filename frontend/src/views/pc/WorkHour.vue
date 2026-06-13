<template>
  <div class="work-hour-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">工时工资结算</span>
          <div class="flex gap-2">
            <el-button type="warning" @click="handleSettleSingle" v-if="searchForm.workerId">
              <el-icon><Coin /></el-icon>
              结算该工人
            </el-button>
            <el-button type="success" @click="handleSettleMonth">
              <el-icon><Money /></el-icon>
              按月批量结算
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="结算月份" prop="settleMonth">
          <el-date-picker
            v-model="searchForm.settleMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="工人">
          <el-select v-model="searchForm.workerId" placeholder="选择工人" clearable style="width: 150px">
            <el-option v-for="w in workerList" :key="w.id" :label="w.nickname" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.settleStatus" placeholder="选择状态" clearable style="width: 130px">
            <el-option label="待结算" value="pending" />
            <el-option label="已结算" value="settled" />
            <el-option label="已发放" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="工资明细说明：普通工资按工时×时薪计算；高温津贴按室外高温作业天数单独计发，不与普通工时混合"
        type="info"
        show-icon
        :closable="false"
        class="mb-4"
      />

      <el-table :data="tableData" v-loading="loading" border class="rounded-lg overflow-hidden" :summary-method="getSummaries" show-summary>
        <el-table-column prop="settleMonth" label="月份" width="100" />
        <el-table-column prop="workerName" label="工人" width="100" />
        <el-table-column label="普通工时" width="120" align="right">
          <template #default="{ row }">
            <span class="text-blue-600 font-semibold">{{ row.normalHours || 0 }} h</span>
          </template>
        </el-table-column>
        <el-table-column label="普通工资" width="130" align="right">
          <template #default="{ row }">
            <span class="text-blue-600 font-bold">¥{{ row.normalWage || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="高温天数" width="100" align="right">
          <template #default="{ row }">
            <span class="text-orange-600">{{ row.heatDays || 0 }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="高温工时" width="120" align="right">
          <template #default="{ row }">
            <span class="text-orange-600 font-semibold">{{ row.heatHours || 0 }} h</span>
          </template>
        </el-table-column>
        <el-table-column label="高温津贴" width="130" align="right">
          <template #default="{ row }">
            <span class="text-orange-600 font-bold">¥{{ row.heatAllowanceTotal || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工资合计" width="140" align="right">
          <template #default="{ row }">
            <span class="text-red-600 text-lg font-bold">¥{{ row.totalWage || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工资构成" min-width="280">
          <template #default="{ row }">
            <div class="wage-breakdown">
              <div class="flex items-center gap-2 mb-1">
                <div class="w-3 h-3 bg-blue-500 rounded-sm"></div>
                <span class="text-sm text-gray-700">
                  普通工资：{{ row.normalHours || 0 }}h × 时薪 = ¥{{ row.normalWage || '0.00' }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-3 h-3 bg-orange-500 rounded-sm"></div>
                <span class="text-sm text-gray-700">
                  高温津贴：{{ row.heatDays || 0 }}天 × 日津贴 = ¥{{ row.heatAllowanceTotal || '0.00' }}
                  <span class="text-gray-400 ml-1">(室外高温作业单独计发)</span>
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="settleStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.settleStatus === 'paid' ? 'success' : row.settleStatus === 'settled' ? 'primary' : 'warning'">
              {{ row.settleStatus === 'paid' ? '已发放' : row.settleStatus === 'settled' ? '已结算' : '待结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">明细</el-button>
            <el-button type="success" size="small" link @click="handleMarkPaid(row)" v-if="row.settleStatus !== 'paid'">标记已发</el-button>
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

    <el-dialog v-model="detailVisible" title="工资明细" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="结算月份">{{ currentRow.settleMonth }}</el-descriptions-item>
        <el-descriptions-item label="工人姓名">{{ currentRow.workerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.settleStatus === 'paid' ? 'success' : currentRow.settleStatus === 'settled' ? 'primary' : 'warning'">
            {{ currentRow.settleStatus === 'paid' ? '已发放' : currentRow.settleStatus === 'settled' ? '已结算' : '待结算' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label=""></el-descriptions-item>

        <el-descriptions-item label="【普通工时】" class="text-blue-600 font-semibold" :span="2"></el-descriptions-item>
        <el-descriptions-item label="普通工时">{{ currentRow.normalHours || 0 }} 小时</el-descriptions-item>
        <el-descriptions-item label="普通工资"><span class="text-blue-600 font-bold">¥{{ currentRow.normalWage || '0.00' }}</span></el-descriptions-item>

        <el-descriptions-item label="【高温津贴】" class="text-orange-600 font-semibold" :span="2"></el-descriptions-item>
        <el-descriptions-item label="高温作业天数">{{ currentRow.heatDays || 0 }} 天</el-descriptions-item>
        <el-descriptions-item label="高温工时">{{ currentRow.heatHours || 0 }} 小时</el-descriptions-item>
        <el-descriptions-item label="高温津贴合计" :span="2">
          <span class="text-orange-600 font-bold">¥{{ currentRow.heatAllowanceTotal || '0.00' }}</span>
          <span class="ml-2 text-sm text-gray-500">（室外高温作业单独计发，与普通工资分离）</span>
        </el-descriptions-item>

        <el-descriptions-item label="【工资合计】" class="text-red-600 font-bold" :span="2">
          <span class="text-red-600 text-xl font-bold">¥{{ currentRow.totalWage || '0.00' }}</span>
          <span class="ml-2 text-sm text-gray-500">= 普通工资 + 高温津贴</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, Coin } from '@element-plus/icons-vue'
import { workHourApi } from '@/api/workHour'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const workerList = ref([])
const detailVisible = ref(false)
const currentRow = ref(null)

const defaultMonth = dayjs().format('YYYY-MM')

const searchForm = reactive({
  settleMonth: defaultMonth,
  workerId: '',
  settleStatus: ''
})

const pagination = reactive({ current: 1, size: 10, total: 0 })

const getSummaries = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) { sums[index] = '合计'; return }
    if (column.label === '普通工资') {
      const val = data.reduce((s, r) => s + Number(r.normalWage || 0), 0)
      sums[index] = '¥' + val.toFixed(2)
      return
    }
    if (column.label === '高温津贴') {
      const val = data.reduce((s, r) => s + Number(r.heatAllowanceTotal || 0), 0)
      sums[index] = '¥' + val.toFixed(2)
      return
    }
    if (column.label === '工资合计') {
      const val = data.reduce((s, r) => s + Number(r.totalWage || 0), 0)
      sums[index] = '¥' + val.toFixed(2)
      return
    }
    if (column.label === '普通工时') {
      sums[index] = data.reduce((s, r) => s + Number(r.normalHours || 0), 0).toFixed(2) + ' h'
      return
    }
    if (column.label === '高温工时') {
      sums[index] = data.reduce((s, r) => s + Number(r.heatHours || 0), 0).toFixed(2) + ' h'
      return
    }
    if (column.label === '高温天数') {
      sums[index] = data.reduce((s, r) => s + Number(r.heatDays || 0), 0) + ' 天'
      return
    }
    sums[index] = ''
  })
  return sums
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
    if (!params.settleStatus) delete params.settleStatus
    const res = await workHourApi.page(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (e) { console.error(e); ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

const loadWorkerList = async () => {
  try {
    const res = await userApi.list({ role: 'worker' })
    if (res.code === 200) workerList.value = res.data
  } catch (e) { console.error(e) }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => {
  Object.assign(searchForm, { settleMonth: defaultMonth, workerId: '', settleStatus: '' })
  handleSearch()
}
const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handleCurrentChange = (current) => { pagination.current = current; loadData() }

const handleSettleMonth = async () => {
  if (!searchForm.settleMonth) { ElMessage.warning('请先选择结算月份'); return }
  try {
    await ElMessageBox.confirm(
      `确定要结算 ${searchForm.settleMonth} 月所有工人的工资吗？`,
      '确认结算', { type: 'warning' }
    )
    loading.value = true
    const res = await workHourApi.settleMonth(searchForm.settleMonth)
    if (res.code === 200) { ElMessage.success('结算成功'); loadData() }
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('结算失败') } }
  finally { loading.value = false }
}

const handleSettleSingle = async () => {
  if (!searchForm.settleMonth || !searchForm.workerId) {
    ElMessage.warning('请先选择结算月份和工人'); return
  }
  try {
    loading.value = true
    const res = await workHourApi.settleWorker(searchForm.settleMonth, searchForm.workerId)
    if (res.code === 200) { ElMessage.success('结算成功'); loadData() }
  } catch (e) { console.error(e); ElMessage.error('结算失败') }
  finally { loading.value = false }
}

const handleViewDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleMarkPaid = async (row) => {
  try {
    await ElMessageBox.confirm(`确认将 ${row.workerName} ${row.settleMonth} 的工资标记为已发放？`, '提示', { type: 'warning' })
    const res = await workHourApi.update(row.id, { ...row, settleStatus: 'paid' })
    if (res.code === 200) { ElMessage.success('标记成功'); loadData() }
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('操作失败') } }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该结算记录吗？', '提示', { type: 'warning' })
    const res = await workHourApi.delete(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('删除失败') } }
}

onMounted(() => { loadWorkerList(); loadData() })
</script>

<style scoped>
.work-hour-page { max-width: 1500px; margin: 0 auto; }
.wage-breakdown { padding: 4px 0; }
</style>
