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

      <el-table v-loading="loading" :data="recordList" border>
        <el-table-column prop="id" label="记录ID" width="90" align="center" />
        <el-table-column prop="moduleName" label="原页面" width="140" align="center" />
        <el-table-column prop="displayName" label="作废数据" min-width="280" show-overflow-tooltip />
        <el-table-column prop="deletedByName" label="作废人" width="140" align="center">
          <template #default="scope">
            {{ scope.row.deletedByName || `用户#${scope.row.deletedBy}` }}
          </template>
        </el-table-column>
        <el-table-column prop="deletedTime" label="作废时间" min-width="180" align="center" />
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
  deletedBy: number
  deletedByName?: string
  deletedTime: string
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
    const res: any = await request.get('/invalid-data/page', { params: queryParams })
    if (res.code === 200) {
      recordList.value = res.data.records
      total.value = res.data.total
    }
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
  ElMessageBox.confirm(`是否确认恢复“${row.displayName}”？恢复后数据将重新回到原页面显示。`, '恢复确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/invalid-data/restore/${row.id}`)
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
