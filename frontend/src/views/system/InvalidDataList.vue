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
          <el-button @click="resetQuery('dataType')" icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="提示：以下为各业务模块中标记为'已作废'（deleted=1）的数据。恢复后数据将重新在原页面显示。"
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
            <el-button size="small" type="success" link icon="RefreshLeft" @click="executeRestore(scope.row)" v-if="hasPerm('system:invalid:restore')">恢复</el-button>
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
import { ref, reactive } from 'vue'
import request from '@/api/request'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useConfirmAction } from '@/composables/useConfirmAction'

interface InvalidRecord {
  id: number
  dataType: string
  moduleName: string
  displayName: string
}

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

const { hasPerm } = usePermission()

const { loading, list: recordList, total, getList, resetQuery, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<InvalidRecord>('/invalid-data/page', queryParams, { autoFetch: true })

const { execute: executeRestore } = useConfirmAction({
  message: (row: InvalidRecord) => `是否确认恢复"${row.displayName}"？恢复后数据将重新回到原页面显示。`,
  title: '恢复确认',
  confirmText: '确定恢复',
  action: (row: InvalidRecord) => request.put('/invalid-data/restore', null, { params: { dataType: row.dataType, dataId: row.id } }),
  successMessage: '恢复成功',
  onSuccess: getList
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

</style>
