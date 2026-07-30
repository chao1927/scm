import {
  queryMdmCodeRules,
  queryMdmImportTasks,
  queryMdmPublications,
  queryMdmQualityIssues,
  queryMdmRecords,
  queryMdmTemplates,
  queryMdmTypes,
} from '../../api/mdmIam'
import { column, getDetail, simpleResource } from './helpers'

function mdmRecords() {
  return {
    query: queryMdmRecords,
    detail: getDetail('/mdm/v1/master-data-records'),
    rowKey: ['recordNo', 'id'],
    columns: [column('recordNo', '数据编号'), column('typeCode', '主数据类型'), column('dataCode', '业务编码'), column('dataName', '名称'), column('statusName', '状态', { status: true }), column('version', '版本'), column('updatedAt', '更新时间', { date: true })],
  }
}

export const mdmResources = {
  'mdm.types': simpleResource(queryMdmTypes, ['typeCode', 'id'], [['typeCode', '类型编码'], ['typeName', '类型名称'], ['statusName', '状态', 'status'], ['version', '版本'], ['updatedAt', '更新时间', 'date']]),
  'mdm.field-defs': simpleResource(queryMdmTemplates, ['templateNo', 'id'], [['templateNo', '模板编号'], ['typeCode', '主数据类型'], ['templateName', '模板名称'], ['version', '版本'], ['statusName', '状态', 'status']]),
  'mdm.code-rules': simpleResource(queryMdmCodeRules, ['ruleNo', 'id'], [['ruleNo', '规则编号'], ['typeCode', '主数据类型'], ['ruleName', '规则名称'], ['pattern', '编码格式'], ['statusName', '状态', 'status']]),
  'mdm.products': mdmRecords(),
  'mdm.partners': mdmRecords(),
  'mdm.warehouses': mdmRecords(),
  'mdm.imports': simpleResource(queryMdmImportTasks, ['importTaskNo', 'id'], [['importTaskNo', '导入任务'], ['typeCode', '主数据类型'], ['fileName', '文件名'], ['successCount', '成功数量'], ['failedCount', '失败数量'], ['statusName', '状态', 'status']]),
  'mdm.quality-issues': simpleResource(queryMdmQualityIssues, ['issueNo', 'id'], [['issueNo', '问题编号'], ['typeCode', '主数据类型'], ['recordNo', '数据编号'], ['ruleCode', '规则'], ['severityName', '严重程度'], ['statusName', '状态', 'status']]),
  'mdm.publishes': simpleResource(queryMdmPublications, ['publicationNo', 'id'], [['publicationNo', '发布编号'], ['typeCode', '主数据类型'], ['recordNo', '数据编号'], ['targetSystem', '目标系统'], ['statusName', '状态', 'status'], ['publishedAt', '发布时间', 'date']]),
}
