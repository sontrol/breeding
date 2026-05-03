<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="耳标号">
          <el-input v-model="queryParams.earTag" placeholder="请输入耳标号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="健康" :value="1" />
            <el-option label="患病" :value="2" />
            <el-option label="隔离" :value="3" />
            <el-option label="死亡" :value="4" />
            <el-option label="出栏" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button @click="resetQuery('earTag', 'status')" icon="Refresh">重置</el-button>
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('animal:add')">新增</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table v-loading="loading" :data="animalList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="earTag" label="耳标号" align="center" />
        <el-table-column prop="species" label="物种" align="center" />
        <el-table-column prop="variety" label="品种" align="center" />
        <el-table-column prop="gender" label="性别" align="center">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '公' : '母' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="getEnumLabel(animalStatusTypeMap, scope.row.status, 'info')">{{ getEnumLabel(animalStatusMap, scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="View" @click="handleDetail(scope.row)">详情</el-button>
            <el-button size="small" type="primary" link icon="Edit" @click="handleUpdate(scope.row)" v-if="hasPerm('animal:edit')">修改</el-button>
            <el-button size="small" type="danger" link icon="Delete" @click="handleInvalidate(scope.row)" v-if="hasPerm('animal:invalidate')">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="耳标号" prop="earTag">
          <el-input v-model="form.earTag" placeholder="请输入耳标号" />
        </el-form-item>
        <el-form-item label="物种" prop="species">
          <el-input v-model="form.species" placeholder="请输入物种(如:猪,牛)" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="公" :value="1" />
            <el-option label="母" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属栏舍" prop="shedId">
          <el-select v-model="form.shedId" placeholder="请选择栏舍" filterable clearable>
            <el-option v-for="shed in shedOptions" :key="shed.id" :label="shed.name" :value="shed.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="健康" :value="1" />
            <el-option label="患病" :value="2" />
            <el-option label="隔离" :value="3" />
            <el-option label="死亡" :value="4" />
            <el-option label="出栏" :value="5" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="动物详情" v-model="detailOpen" width="1100px" append-to-body>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailAnimal" :column="3" border>
          <el-descriptions-item label="ID">{{ detailAnimal.id }}</el-descriptions-item>
          <el-descriptions-item label="耳标号">{{ detailAnimal.earTag || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getEnumLabel(animalStatusTypeMap, detailAnimal.status, 'info')">{{ getEnumLabel(animalStatusMap, detailAnimal.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="物种">{{ detailAnimal.species || '-' }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detailAnimal.variety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ getEnumLabel(genderMap, detailAnimal.gender, '-') }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ formatDate(detailAnimal.birthDate, 'YYYY/MM/DD') }}</el-descriptions-item>
          <el-descriptions-item label="栏舍ID">{{ detailAnimal.shedId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(detailAnimal.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(detailAnimal.updateTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-if="detailCanViewDiseaseRecords" class="detail-tabs">
          <el-tab-pane :label="`症状记录 (${symptomList.length})`" name="symptom">
            <el-table v-loading="symptomLoading" :data="symptomList" border>
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="symptomDesc" label="症状描述" min-width="260" show-overflow-tooltip />
              <el-table-column prop="observeTime" label="发现时间" min-width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.observeTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                    {{ scope.row.status === 1 ? '已诊断' : '待诊断' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上报时间" min-width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`治疗记录 (${treatmentList.length})`" name="treatment">
            <el-table v-loading="treatmentLoading" :data="treatmentList" border>
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="diagnosisId" label="诊断ID" width="100" align="center" />
              <el-table-column prop="medicineId" label="药品ID" width="100" align="center" />
              <el-table-column prop="time" label="治疗时间" min-width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.time) }}
                </template>
              </el-table-column>
              <el-table-column prop="result" label="治疗结果" min-width="260" show-overflow-tooltip />
              <el-table-column prop="createTime" label="记录时间" min-width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <el-empty v-else class="detail-empty" description="当前账号暂无疾病记录查看权限" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useCrudDialog } from '@/composables/useCrudDialog'
import { getEnumLabel, animalStatusMap, animalStatusTypeMap, genderMap } from '@/constants/enums'

interface AnimalRecord {
  id: number
  earTag: string
  species: string
  variety: string
  birthDate?: string
  gender: number
  shedId?: number
  status: number
  createTime?: string
  updateTime?: string
}

interface SymptomRecord {
  id: number
  animalId: number
  symptomDesc: string
  observeTime?: string
  status: number
  createTime?: string
}

interface TreatmentRecord {
  id: number
  diagnosisId?: number
  animalId: number
  medicineId?: number
  dosage?: number
  time?: string
  result?: string
  createTime?: string
}

interface AnimalDetailResponse {
  animal: AnimalRecord
  canViewDiseaseRecords: boolean
  symptomList: SymptomRecord[]
  treatmentList: TreatmentRecord[]
}

const shedOptions = ref<any[]>([])
const open = ref(false)
const title = ref('')
const formRef = ref()
const detailOpen = ref(false)
const detailLoading = ref(false)

const loadShedOptions = async () => {
  try {
    const res: any = await request.get('/shed/list')
    if (res.code === 200) {
      shedOptions.value = res.data || []
    }
  } catch (e) {
    console.error('获取栏舍列表失败', e)
  }
}

onMounted(() => {
  loadShedOptions()
})
const detailAnimal = ref<AnimalRecord | null>(null)
const detailCanViewDiseaseRecords = ref(false)
const symptomLoading = ref(false)
const treatmentLoading = ref(false)
const symptomList = ref<SymptomRecord[]>([])
const treatmentList = ref<TreatmentRecord[]>([])

const queryParams = reactive({
  page: 1,
  size: 10,
  earTag: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  earTag: '',
  species: '',
  variety: '',
  gender: 1,
  shedId: undefined as number | undefined,
  status: 1
})

const rules = {
  earTag: [{ required: true, message: '耳标号不能为空', trigger: 'blur' }],
  species: [{ required: true, message: '物种不能为空', trigger: 'blur' }]
}

const { hasPerm } = usePermission()

const { loading, list: animalList, total, getList, resetQuery, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<AnimalRecord>('/animal/page', queryParams, { autoFetch: true })

const { reset, submitForm: crudSubmit } = useCrudDialog('/animal', getList, { addSuccessMessage: '添加成功' })

const handle新增 = () => {
  reset(form, formRef, { id: undefined, earTag: '', species: '', variety: '', gender: 1, shedId: undefined, status: 1 })
  open.value = true
  title.value = '添加动物'
}

const handleUpdate = (row: any) => {
  reset(form, formRef, { id: undefined, earTag: '', species: '', variety: '', gender: 1, shedId: undefined, status: 1 })
  Object.assign(form, row)
  open.value = true
  title.value = '修改动物'
}

const cancel = () => {
  open.value = false
  reset(form, formRef, { id: undefined, earTag: '', species: '', variety: '', gender: 1, shedId: undefined, status: 1 })
}

const handleDetail = async (row: AnimalRecord) => {
  detailOpen.value = true
  detailLoading.value = true
  symptomLoading.value = true
  treatmentLoading.value = true
  try {
    const res: any = await request.get(`/animal/detail/${row.id}`)
    const detail: AnimalDetailResponse = res.data
    detailAnimal.value = detail.animal
    detailCanViewDiseaseRecords.value = detail.canViewDiseaseRecords
    symptomList.value = detail.symptomList || []
    treatmentList.value = detail.treatmentList || []
  } finally {
    symptomLoading.value = false
    treatmentLoading.value = false
    detailLoading.value = false
  }
}

const submitForm = () => {
  crudSubmit(formRef, form)
}

const handleInvalidate = (row: AnimalRecord) => {
  ElMessageBox.confirm(`是否确认作废耳标号为"${row.earTag}"的数据项？作废后仅可由管理员恢复。`, '作废确认', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/animal/invalidate/${row.id}`)
    ElMessage.success('作废成功')
    getList()
  }).catch(() => {})
}

</script>

<style scoped>
.app-container {
  padding: 20px;
}

.detail-tabs {
  margin-top: 20px;
}

.detail-empty {
  margin-top: 20px;
}
</style>
