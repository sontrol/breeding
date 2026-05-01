<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="动物ID" prop="animalId">
        <el-input v-model="queryParams.animalId" placeholder="请输入动物ID" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="诊断ID" prop="diagnosisId">
        <el-input v-model="queryParams.diagnosisId" placeholder="请输入诊断ID" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery('animalId', 'diagnosisId')">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-if="hasPerm('treatment:add')">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="treatmentList" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="diagnosisId" label="诊断ID" align="center" />
      <el-table-column prop="animalId" label="动物ID" align="center" />
      <el-table-column label="使用物品" align="center" min-width="200">
        <template #default="scope">
          <div v-if="scope.row.items && scope.row.items.length > 0">
            <el-tag v-for="(item, idx) in scope.row.items" :key="idx" size="small" style="margin: 2px;">
              {{ item.itemName }} × {{ item.dosage }}
            </el-tag>
          </div>
          <span v-else-if="scope.row.medicineId">药品ID: {{ scope.row.medicineId }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="time" label="治疗时间" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.time, 'YYYY-MM-DD HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column prop="result" label="治疗结果" align="center" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button size="small" type="danger" link icon="Delete" @click="handleInvalidate(scope.row)" v-if="hasPerm('treatment:invalidate')">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.page" v-model:limit="queryParams.size" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="诊断ID" prop="diagnosisId">
          <el-input v-model="form.diagnosisId" placeholder="请输入诊断ID" />
        </el-form-item>
        <el-form-item label="动物ID" prop="animalId">
          <el-input v-model="form.animalId" placeholder="请输入动物ID" />
        </el-form-item>
        <el-form-item label="单品用量" prop="dosage" v-if="form.items.length === 0">
          <el-input-number v-model="form.dosage" :min="0.01" :precision="2" :step="0.5" placeholder="单药用量，多物品请在下方添加" style="width: 100%;" />
        </el-form-item>
        
        <el-divider content-position="left">使用物品（可添加多个）</el-divider>
        
        <div v-for="(item, index) in form.items" :key="index" class="item-row">
          <el-form-item :label="'物品' + (index + 1)" style="margin-bottom: 10px;">
            <div style="display: flex; gap: 8px; align-items: flex-start;">
              <el-select v-model="item.inventoryId" placeholder="选择库存物品" filterable style="width: 280px;" @change="(val: number) => handleItemChange(index, val)">
                <el-option-group label="药品" v-if="medicineList.length > 0">
                  <el-option
                    v-for="inv in medicineList"
                    :key="inv.id"
                    :label="`${inv.itemName} (批次:${inv.batchNumber}) - 库存:${inv.quantity}${inv.unit}`"
                    :value="inv.id"
                    :disabled="inv.quantity <= 0"
                  />
                </el-option-group>
                <el-option-group label="疫苗" v-if="vaccineList.length > 0">
                  <el-option
                    v-for="inv in vaccineList"
                    :key="inv.id"
                    :label="`${inv.itemName} (批次:${inv.batchNumber}) - 库存:${inv.quantity}${inv.unit}`"
                    :value="inv.id"
                    :disabled="inv.quantity <= 0"
                  />
                </el-option-group>
                <el-option-group label="器械" v-if="equipmentList.length > 0">
                  <el-option
                    v-for="inv in equipmentList"
                    :key="inv.id"
                    :label="`${inv.itemName} (批次:${inv.batchNumber}) - 库存:${inv.quantity}${inv.unit}`"
                    :value="inv.id"
                    :disabled="inv.quantity <= 0"
                  />
                </el-option-group>
              </el-select>
              <el-input-number v-model="item.dosage" :min="0.01" :precision="2" :step="0.5" placeholder="用量" style="width: 120px;" />
              <el-button type="danger" icon="Delete" circle size="small" @click="removeItem(index)" />
            </div>
          </el-form-item>
        </div>
        
        <el-button type="primary" plain icon="Plus" @click="addItem" style="margin-bottom: 15px;">添加物品</el-button>
        
        <el-form-item label="治疗时间" prop="time">
          <el-date-picker v-model="form.time" type="datetime" placeholder="选择治疗时间" value-format="YYYY/MM/DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="治疗状态" prop="diagnosisStatus">
          <el-select v-model="form.diagnosisStatus" placeholder="请选择治疗后状态">
            <el-option :value="0" label="继续治疗" />
            <el-option :value="1" label="已治愈" />
            <el-option :value="2" label="死亡" />
          </el-select>
        </el-form-item>
        <el-form-item label="治疗结果" prop="result">
          <el-input v-model="form.result" type="textarea" :rows="3" placeholder="请输入治疗结果或观察说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { now } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useInvalidate } from '@/composables/useInvalidate'
import { useCurrentUserId } from '@/composables/useCurrentUser'

const route = useRoute()
const currentUserId = useCurrentUserId()
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined
})

const form = reactive({
  diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  operatorId: currentUserId,
  medicineId: undefined as number | undefined,
  dosage: 0,
  time: now(),
  result: '',
  items: [] as any[],
  diagnosisStatus: 0
})

const inventoryList = ref<any[]>([])
const medicineList = ref<any[]>([])
const vaccineList = ref<any[]>([])
const equipmentList = ref<any[]>([])

const itemTypeMap: Record<number, string> = {
  1: '饲料',
  2: '药品',
  3: '疫苗',
  4: '器械'
}

const rules = {
  diagnosisId: [{ required: true, message: '请输入诊断ID', trigger: 'blur' }],
  animalId: [{ required: true, message: '请输入动物ID', trigger: 'blur' }],
  time: [{ required: true, message: '请选择治疗时间', trigger: 'change' }]
}

const { hasPerm } = usePermission()

const { loading, list: treatmentList, total, getList, resetQuery, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/treatment/page', queryParams, { autoFetch: true })

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const payload: any = {
        diagnosisId: form.diagnosisId,
        animalId: form.animalId,
        operatorId: form.operatorId,
        medicineId: form.medicineId,
        dosage: form.dosage,
        time: form.time,
        result: form.result,
        diagnosisStatus: form.diagnosisStatus,
        items: form.items.filter((item: any) => item.inventoryId)
      }
      await request.post('/treatment', payload)
      ElMessage.success('治疗记录保存成功')
      open.value = false
      getList()
    }
  })
}

const { handleInvalidate } = useInvalidate('/treatment', '治疗', getList)

const getInventoryList = async () => {
  try {
    const res: any = await request.get('/inventory/page', { params: { page: 1, size: 200 } })
    if (res.code === 200) {
      const all = res.data.records || []
      inventoryList.value = all
      medicineList.value = all.filter((i: any) => i.itemType === 2)
      vaccineList.value = all.filter((i: any) => i.itemType === 3)
      equipmentList.value = all.filter((i: any) => i.itemType === 4)
    }
  } catch (e) {
    console.error('获取库存列表失败', e)
  }
}

const addItem = () => {
  form.items.push({
    inventoryId: undefined,
    itemName: '',
    itemType: undefined,
    dosage: 0
  })
}

const removeItem = (index: number) => {
  form.items.splice(index, 1)
}

const handleItemChange = (index: number, inventoryId: number) => {
  const inv = inventoryList.value.find(i => i.id === inventoryId)
  if (inv && form.items[index]) {
    form.items[index].itemName = inv.itemName
    form.items[index].itemType = inv.itemType
  }
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = '新增治疗'
}

const reset = () => {
  Object.assign(form, {
    diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined,
    animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
    operatorId: currentUserId,
    medicineId: undefined,
    dosage: 0,
    time: now(),
    result: '',
    items: [],
    diagnosisStatus: 0
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  getInventoryList()
  if ((route.query.diagnosisId || route.query.animalId) && hasPerm('treatment:add')) {
    handleAdd()
  }
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.item-row {
  border: 1px dashed #dcdfe6;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
}
</style>
