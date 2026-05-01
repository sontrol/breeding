export function getEnumLabel(map: Record<number, string>, key: number | undefined, defaultVal = '未知'): string {
  return key != null ? (map[key] || defaultVal) : defaultVal
}

export const animalStatusMap: Record<number, string> = { 1: '健康', 2: '患病', 3: '隔离', 4: '死亡', 5: '出栏' }
export const animalStatusTypeMap: Record<number, string> = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info', 5: '' }

export const genderMap: Record<number, string> = { 1: '公', 2: '母' }

export const severityMap: Record<number, string> = { 1: '轻微', 2: '中度', 3: '严重' }
export const severityTypeMap: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }

export const diagnosisStatusMap: Record<number, string> = { 0: '治疗中', 1: '已治愈', 2: '死亡' }
export const diagnosisStatusTypeMap: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }

export const inventoryTypeMap: Record<number, string> = { 1: '饲料', 2: '药品', 3: '疫苗', 4: '器械' }
export const inventoryTypeTagMap: Record<number, string> = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info' }

export const alertRuleTypeMap: Record<number, string> = { 1: '体温异常', 2: '未进食异常', 3: '死亡率高', 4: '物品过期' }
export const alertRuleTypeTagMap: Record<number, string> = { 1: 'danger', 2: 'danger', 3: 'danger', 4: 'warning' }

export const registerAuditStatusMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已驳回' }
export const registerAuditStatusTypeMap: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
