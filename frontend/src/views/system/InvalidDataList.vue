<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="数据类型">
          <el-select v-model="queryParams.dataType" placeholder="请选择数据类型" clearable style="width: 180px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button @click="resetQuery" icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="提示：作废记录功能已调整，现通过各业务模块的软删除标记(deleted=1)来识别作废数据。如需恢复数据，请直接在对应模块操作或联系管理员。"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      />

      <el-table v-loading="loading" :data="recordList" border>
        <el-table-column prop="id" label="数据ID" width="90" align="center" />
        <el-table-column prop="moduleName" label="原页面" width="140" align="center" />
        <el-table-column prop="displayName" label="作废数据" min-width="280" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button size="small" type="success" link icon="RefreshLeft" @click="handleRestore(scope.row)" v-if="hasPerm('system:invalid:restore')">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

interface InvalidRecord {
  id: number
  dataId: number
  dataType: string
  moduleName: string
  displayName: string
}

const userStore = useUserStore()
const loading = ref(true)
const recordList = ref<InvalidRecord[]>([])
const total = ref(0)

const typeOptions = [
  { label: '动物档案', value: 'animal' },
  { label: '饲养计划', value: 'feeding_plan' },
  { label: '饲养执行记录', value: 'feeding_record' },
  { label: '症状上报', value: 'symptom' },
  { label: '诊断记录', value: 'diagnosis' },
  { label: '治疗记录', value: 'treatment' },
  { label: '库存列表', value: 'inventory' },
  { label: '预警消息', value: 'alert' }
]

const queryParams = reactive({
  page: 1,
  size: 10,
  dataType: undefined as string | undefined
})

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const getList = async () => {
  loading.value = true
  try {
    const records: InvalidRecord[] = []
    const type = queryParams.dataType
    const typesToQuery = type ? [type] : typeOptions.map(o => o.value)

    for (const dt of typesToQuery) {
      const meta = typeOptions.find(o => o.value === dt)
      if (!meta) continue
      try {
        const res: any = await request.get(`/${dt}/page`, { params: { page: 1, size: 1000 } })
        if (res.code === 200 && res.data.records) {
          const deletedItems = res.data.records
            .filter((r: any) => r.deleted === 1)
            .map((r: any) => ({
              id: r.id,
              dataId: r.id,
              dataType: dt,
              moduleName: meta.label,
              displayName: r.displayName || `${meta.label} #${r.id}`
            }))
          records.push(...deletedItems)
        }
      } catch (e) {
        console.warn(`获取 ${dt} 作废数据失败`, e)
      }
    }

    const start = (queryParams.page - 1) * queryParams.size
    const end = start + queryParams.size
    recordList.value = records.slice(start, end)
    total.value = records.length
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const resetQuery = () => {
  queryParams.dataType = undefined
  handleQuery()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

const handleRestore = (row: InvalidRecord) => {
  ElMessageBox.confirm(`是否确认恢复"${row.displayName}"？恢复后数据将重新回到原页面显示。`, '恢复确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put('/invalid-data/restore', null, { params: { dataType: row.dataType, dataId: row.dataId } })
    ElMessage.success('恢复成功')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
