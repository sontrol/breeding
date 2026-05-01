import dayjs from 'dayjs'

export function formatDate(dateStr?: string, format = 'YYYY/MM/DD HH:mm:ss'): string {
  if (!dateStr) return '-'
  const parsed = dayjs(dateStr)
  return parsed.isValid() ? parsed.format(format) : dateStr
}

export function now(format = 'YYYY/MM/DD HH:mm:ss'): string {
  return dayjs().format(format)
}
