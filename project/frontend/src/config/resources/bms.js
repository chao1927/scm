import {
  queryBmsBillingObjects,
  queryBmsChargeSources,
  queryBmsSettlementSummary,
} from '../../api/fulfillmentSettlement'
import { simpleResource } from './helpers'

export const bmsResources = {
  'bms.billing-subjects': simpleResource(queryBmsBillingObjects, ['subjectNo', 'id'], [['subjectNo', '计费对象'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['counterpartyName', '结算对象'], ['statusName', '状态', 'status']]),
  'bms.charge-sources': simpleResource(queryBmsChargeSources, ['sourceNo', 'id'], [['sourceNo', '费用来源'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['amount', '金额', 'number'], ['currency', '币种'], ['statusName', '状态', 'status']]),
  'bms.settlement-reports': simpleResource(queryBmsSettlementSummary, ['periodCode', 'id'], [['periodCode', '结算期间'], ['payableAmount', '应付金额', 'number'], ['receivableAmount', '应收金额', 'number'], ['settledAmount', '已结算', 'number'], ['differenceAmount', '差异金额', 'number']]),
}
